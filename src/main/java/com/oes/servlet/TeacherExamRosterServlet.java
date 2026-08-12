package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TeacherExamRosterServlet extends HttpServlet {

    private final TeacherDao teacherDao = new TeacherDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User teacher = (User) session.getAttribute("currentUser");
        if (!"TEACHER".equalsIgnoreCase(teacher.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        long examId = Long.parseLong(req.getParameter("examId"));
        req.setAttribute("examId", examId);
        req.setAttribute("roster", teacherDao.getExamRoster(examId, teacher.getUserId()));
        req.setAttribute("examMeta", teacherDao.getExamMeta(examId, teacher.getUserId()));
        req.getRequestDispatcher("/views/teacher/exam-roster.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User teacher = (User) session.getAttribute("currentUser");
        if (!"TEACHER".equalsIgnoreCase(teacher.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        long examId = Long.parseLong(req.getParameter("examId"));
        long resultId = Long.parseLong(req.getParameter("resultId"));
        String feedback = req.getParameter("feedback");
        if (feedback == null) {
            feedback = "";
        }
        teacherDao.updateResultFeedback(resultId, teacher.getUserId(), feedback.trim());
        resp.sendRedirect(req.getContextPath() + "/teacher/exam/roster?examId=" + examId);
    }
}
