package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TeacherExamStatusServlet extends HttpServlet {
    private final TeacherDao teacherDao = new TeacherDao();

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
        String status = req.getParameter("status");
        if (!"PUBLISHED".equalsIgnoreCase(status) && !"CLOSED".equalsIgnoreCase(status) && !"DRAFT".equalsIgnoreCase(status)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        teacherDao.updateExamStatus(examId, teacher.getUserId(), status);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
