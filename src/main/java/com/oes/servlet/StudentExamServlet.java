package com.oes.servlet;

import com.oes.model.ExamQuestion;
import com.oes.model.ExamResultSummary;
import com.oes.model.ExamSessionState;
import com.oes.model.User;
import com.oes.service.AsyncExamNotificationService;
import com.oes.service.StudentExamService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller: student takes exam one question at a time.
 */
public class StudentExamServlet extends HttpServlet {

    private final StudentExamService studentExamService = new StudentExamService();
    private final AsyncExamNotificationService examMail = new AsyncExamNotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User student = (User) session.getAttribute("currentUser");
        if (!"STUDENT".equalsIgnoreCase(student.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        long examId = Long.parseLong(req.getParameter("examId"));
        int duration = req.getParameter("duration") == null ? 60 : Integer.parseInt(req.getParameter("duration"));

        Object previousExamSession = session.getAttribute(studentExamService.sessionKey(examId));
        ExamSessionState state = studentExamService.startOrResumeExam(session, student.getUserId(), examId, duration);
        if (state == null) {
            req.setAttribute("error", "You have already submitted this exam. Re-attempt is not allowed.");
            forwardDashboard(req, resp, student.getUserId());
            return;
        }

        if (state.getTotalQuestions() == 0) {
            req.setAttribute("error", "This exam has no questions yet.");
            studentExamService.clearSession(session, examId);
            forwardDashboard(req, resp, student.getUserId());
            return;
        }

        if (studentExamService.isExamTimeExpired(state)) {
            finishAndShowResult(req, resp, session, student.getUserId(), examId, state);
            return;
        }

        if (previousExamSession == null) {
            examMail.scheduleExamStarted(examId, student.getUserId());
        }

        bindExamView(req, state);
        req.getRequestDispatcher("/views/student/exam-single.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User student = (User) session.getAttribute("currentUser");
        if (!"STUDENT".equalsIgnoreCase(student.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        long examId = Long.parseLong(req.getParameter("examId"));
        ExamSessionState state = studentExamService.getSession(session, examId);
        if (state == null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if (studentExamService.isExamTimeExpired(state)) {
            finishAndShowResult(req, resp, session, student.getUserId(), examId, state);
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            action = "submit";
        }

        ExamQuestion current = state.getQuestions().get(state.getCurrentIndex());
        String answer = req.getParameter("answer");
        studentExamService.applyCurrentAnswer(state, current.getQuestionId(), answer);
        studentExamService.persistDraftAnswers(student.getUserId(), examId, state);

        if ("submit".equalsIgnoreCase(action) || "finish".equalsIgnoreCase(action)) {
            finishAndShowResult(req, resp, session, student.getUserId(), examId, state);
            return;
        }

        if ("next".equalsIgnoreCase(action)) {
            studentExamService.goNext(state);
        } else if ("prev".equalsIgnoreCase(action)) {
            studentExamService.goPrev(state);
        }

        bindExamView(req, state);
        req.getRequestDispatcher("/views/student/exam-single.jsp").forward(req, resp);
    }

    private void bindExamView(HttpServletRequest req, ExamSessionState state) {
        ExamQuestion q = state.getQuestions().get(state.getCurrentIndex());
        req.setAttribute("examId", state.getExamId());
        req.setAttribute("durationMinutes", state.getDurationMinutes());
        req.setAttribute("serverEndMs", state.getServerEndTimeEpochMs());
        req.setAttribute("question", q);
        req.setAttribute("questionIndex", state.getCurrentIndex() + 1);
        req.setAttribute("totalQuestions", state.getTotalQuestions());
        String saved = state.getAnswers().get(q.getQuestionId());
        req.setAttribute("savedValue", saved);
    }

    private void finishAndShowResult(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
                                     long studentId, long examId, ExamSessionState state) throws ServletException, IOException {
        try {
            ExamResultSummary summary = studentExamService.submitExam(studentId, examId, state);
            examMail.scheduleExamSubmitted(examId, studentId, summary);
            studentExamService.clearSession(session, examId);
            req.setAttribute("result", summary);
            req.getRequestDispatcher("/views/student/result.jsp").forward(req, resp);
        } catch (IllegalStateException ex) {
            req.setAttribute("error", ex.getMessage());
            forwardDashboard(req, resp, studentId);
        }
    }

    private void forwardDashboard(HttpServletRequest req, HttpServletResponse resp, long studentId) throws ServletException, IOException {
        req.setAttribute("exams", studentExamService.getAvailableExams(studentId));
        req.setAttribute("results", studentExamService.getStudentResults(studentId));
        req.getRequestDispatcher("/views/student/dashboard.jsp").forward(req, resp);
    }
}
