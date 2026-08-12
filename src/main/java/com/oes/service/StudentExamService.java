package com.oes.service;

import com.oes.model.ExamQuestion;
import com.oes.model.ExamResultSummary;
import com.oes.model.ExamSessionState;
import com.oes.model.ResultView;
import com.oes.model.StudentExamView;
import com.oes.repository.StudentRepository;
import com.oes.repository.impl.StudentRepositoryImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student exam workflow (Service layer).
 */
public class StudentExamService {

    private static final String SESSION_KEY_PREFIX = "oes.exam.session.";

    private final StudentRepository studentRepository = new StudentRepositoryImpl();

    public List<StudentExamView> getAvailableExams(long studentId) {
        return studentRepository.findAvailableExams(studentId);
    }

    public List<ResultView> getStudentResults(long studentId) {
        return studentRepository.findResultsByStudent(studentId);
    }

    public String sessionKey(long examId) {
        return SESSION_KEY_PREFIX + examId;
    }

    public ExamSessionState startOrResumeExam(HttpSession httpSession, long studentId, long examId, int durationMinutes) {
        if (studentRepository.hasEvaluatedAttempt(examId, studentId)) {
            return null;
        }
        studentRepository.ensureInProgressResult(examId, studentId);

        @SuppressWarnings("unchecked")
        ExamSessionState existing = (ExamSessionState) httpSession.getAttribute(sessionKey(examId));
        if (existing != null && existing.getExamId() == examId) {
            return existing;
        }

        List<ExamQuestion> questions = studentRepository.findQuestionsByExam(examId);
        Map<Long, String> saved = studentRepository.findSavedAnswers(examId, studentId);

        ExamSessionState state = new ExamSessionState();
        state.setExamId(examId);
        state.setDurationMinutes(durationMinutes);
        state.setQuestions(questions);
        state.setCurrentIndex(0);
        state.getAnswers().putAll(saved);
        long end = System.currentTimeMillis() + (long) durationMinutes * 60_000L;
        state.setServerEndTimeEpochMs(end);

        httpSession.setAttribute(sessionKey(examId), state);
        return state;
    }

    public ExamSessionState getSession(HttpSession httpSession, long examId) {
        return (ExamSessionState) httpSession.getAttribute(sessionKey(examId));
    }

    public void clearSession(HttpSession httpSession, long examId) {
        httpSession.removeAttribute(sessionKey(examId));
    }

    public void persistDraftAnswers(long studentId, long examId, ExamSessionState state) {
        long resultId = studentRepository.ensureInProgressResult(examId, studentId);
        studentRepository.replaceAnswersForResult(resultId, examId, new HashMap<>(state.getAnswers()));
    }

    public void applyCurrentAnswer(ExamSessionState state, long questionId, String value) {
        if (value == null || value.isBlank()) {
            state.getAnswers().remove(questionId);
        } else {
            state.getAnswers().put(questionId, value);
        }
    }

    public void goNext(ExamSessionState state) {
        if (state.getCurrentIndex() < state.getTotalQuestions() - 1) {
            state.setCurrentIndex(state.getCurrentIndex() + 1);
        }
    }

    public void goPrev(ExamSessionState state) {
        if (state.getCurrentIndex() > 0) {
            state.setCurrentIndex(state.getCurrentIndex() - 1);
        }
    }

    public ExamResultSummary submitExam(long studentId, long examId, ExamSessionState state) {
        if (studentRepository.hasEvaluatedAttempt(examId, studentId)) {
            throw new IllegalStateException("Attempt already submitted for this exam.");
        }
        long resultId = studentRepository.ensureInProgressResult(examId, studentId);
        Map<Long, String> answerMap = new HashMap<>(state.getAnswers());
        BigDecimal total = studentRepository.finalizeExamGrading(examId, studentId, resultId, answerMap);

        int totalMarks = studentRepository.findExamTotalMarks(examId);
        Integer passingMarks = studentRepository.findExamPassingMarks(examId);
        BigDecimal passLine;
        if (passingMarks != null && passingMarks >= 0) {
            passLine = BigDecimal.valueOf(passingMarks).setScale(2, RoundingMode.HALF_UP);
        } else {
            passLine = BigDecimal.valueOf(totalMarks).multiply(BigDecimal.valueOf(0.4)).setScale(2, RoundingMode.HALF_UP);
        }
        boolean passed = total.compareTo(passLine) >= 0;

        ExamResultSummary summary = new ExamResultSummary();
        summary.setScoreObtained(total);
        summary.setTotalMarks(totalMarks);
        summary.setPassed(passed);
        summary.setMessage(passed ? "Pass" : "Fail");
        summary.setPassThresholdDescription(passingMarks != null
                ? ("Pass mark: " + passingMarks + " / " + totalMarks)
                : ("Pass mark: 40% of total (" + passLine + " / " + totalMarks + ")"));
        return summary;
    }

    public boolean isExamTimeExpired(ExamSessionState state) {
        return System.currentTimeMillis() > state.getServerEndTimeEpochMs();
    }
}
