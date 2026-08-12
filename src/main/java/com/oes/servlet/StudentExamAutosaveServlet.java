package com.oes.servlet;

import com.oes.model.ExamSessionState;
import com.oes.model.User;
import com.oes.service.StudentExamService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Persists draft answers during exam (Controller).
 */
public class StudentExamAutosaveServlet extends HttpServlet {

    private final StudentExamService studentExamService = new StudentExamService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User student = (User) session.getAttribute("currentUser");
        if (!"STUDENT".equalsIgnoreCase(student.getRoleName())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        long examId = Long.parseLong(req.getParameter("examId"));
        ExamSessionState state = studentExamService.getSession(session, examId);
        if (state == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String qidStr = req.getParameter("currentQuestionId");
        String answer = req.getParameter("answer");
        if (qidStr != null && !qidStr.isBlank()) {
            long qid = Long.parseLong(qidStr);
            studentExamService.applyCurrentAnswer(state, qid, answer);
            studentExamService.persistDraftAnswers(student.getUserId(), examId, state);
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"saved\"}");
    }
}
