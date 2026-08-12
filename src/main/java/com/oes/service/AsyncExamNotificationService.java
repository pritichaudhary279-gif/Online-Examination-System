package com.oes.service;

import com.oes.dao.ExamMailDao;
import com.oes.mail.SynchronousMailSender;
import com.oes.model.ExamNotificationContext;
import com.oes.model.ExamResultSummary;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Schedules exam-related emails on a background pool so HTTP threads return immediately.
 */
public class AsyncExamNotificationService {

    private static final Logger LOGGER = Logger.getLogger(AsyncExamNotificationService.class.getName());
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private static final ExecutorService MAIL_POOL = Executors.newFixedThreadPool(2, runnable -> {
        Thread t = new Thread(runnable, "oes-exam-mail");
        t.setDaemon(true);
        return t;
    });

    private final ExamMailDao examMailDao = new ExamMailDao();

    public static void shutdown() {
        MAIL_POOL.shutdown();
        try {
            if (!MAIL_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                MAIL_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            MAIL_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Exam session just attached (first entry this browser session).
     */
    public void scheduleExamStarted(long examId, long studentId) {
        MAIL_POOL.execute(() -> {
            try {
                sendExamStarted(examId, studentId);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exam started mail task failed examId=" + examId, e);
            }
        });
    }

    /**
     * After successful grading: notify teacher of submission; student receives score and pass/fail.
     */
    public void scheduleExamSubmitted(long examId, long studentId, ExamResultSummary summary) {
        MAIL_POOL.execute(() -> {
            try {
                sendExamSubmitted(examId, studentId, summary);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exam submitted mail task failed examId=" + examId, e);
            }
        });
    }

    private void sendExamStarted(long examId, long studentId) {
        Optional<ExamNotificationContext> opt = examMailDao.loadNotificationContext(examId, studentId);
        if (opt.isEmpty()) {
            LOGGER.fine("No mail context for exam started examId=" + examId);
            return;
        }
        ExamNotificationContext c = opt.get();
        String when = TS.format(ZonedDateTime.now());
        SynchronousMailSender mail = new SynchronousMailSender();

        if (c.hasStudentEmail()) {
            String subj = "Exam entry logged: " + safe(c.getExamTitle());
            String body = "Hello " + safe(c.getStudentName()) + ",\n\n"
                    + "This confirms you have started the exam \"" + safe(c.getExamTitle()) + "\".\n"
                    + "Entry logged at: " + when + "\n\n"
                    + "Stay in the exam window until you submit. Good luck.\n";
            mail.sendPlain(c.getStudentEmail().trim(), subj, body);
        }

        if (c.hasTeacherEmail()) {
            String subj = "Student started exam: " + safe(c.getExamTitle());
            String body = "Hello " + safe(c.getTeacherName()) + ",\n\n"
                    + "Student " + safe(c.getStudentName())
                    + (c.hasStudentEmail() ? " <" + c.getStudentEmail().trim() + ">" : "")
                    + " has started the exam \"" + safe(c.getExamTitle()) + "\".\n"
                    + "Logged at: " + when + "\n";
            mail.sendPlain(c.getTeacherEmail().trim(), subj, body);
        }
    }

    private void sendExamSubmitted(long examId, long studentId, ExamResultSummary summary) {
        Optional<ExamNotificationContext> opt = examMailDao.loadNotificationContext(examId, studentId);
        if (opt.isEmpty()) {
            LOGGER.fine("No mail context for exam submitted examId=" + examId);
            return;
        }
        ExamNotificationContext c = opt.get();
        String when = TS.format(ZonedDateTime.now());
        SynchronousMailSender mail = new SynchronousMailSender();

        String scoreLine = summary.getScoreObtained() != null
                ? summary.getScoreObtained().stripTrailingZeros().toPlainString()
                : "—";
        String resultWord = summary.isPassed() ? "PASS" : "FAIL";

        if (c.hasTeacherEmail()) {
            String subj = "Exam submitted: " + safe(c.getExamTitle()) + " — " + safe(c.getStudentName());
            String body = "Hello " + safe(c.getTeacherName()) + ",\n\n"
                    + "Student " + safe(c.getStudentName())
                    + (c.hasStudentEmail() ? " <" + c.getStudentEmail().trim() + ">" : "")
                    + " has successfully submitted the exam \"" + safe(c.getExamTitle()) + "\".\n"
                    + "Submitted at: " + when + "\n\n"
                    + "Score: " + scoreLine + " / " + summary.getTotalMarks() + "\n"
                    + "Outcome: " + resultWord + "\n";
            mail.sendPlain(c.getTeacherEmail().trim(), subj, body);
        }

        if (c.hasStudentEmail()) {
            String subj = "Exam result: " + safe(c.getExamTitle()) + " — " + resultWord;
            String body = "Hello " + safe(c.getStudentName()) + ",\n\n"
                    + "Your answers for \"" + safe(c.getExamTitle()) + "\" were received and graded.\n"
                    + "Submitted at: " + when + "\n\n"
                    + "--- Your result ---\n"
                    + "Score obtained: " + scoreLine + " / " + summary.getTotalMarks() + "\n"
                    + "Result: " + resultWord + " (" + safe(summary.getMessage()) + ")\n"
                    + safe(summary.getPassThresholdDescription()) + "\n\n"
                    + "You can also review this outcome in your student dashboard.\n";
            mail.sendPlain(c.getStudentEmail().trim(), subj, body);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
