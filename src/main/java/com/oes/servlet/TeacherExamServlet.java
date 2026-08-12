package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;

public class TeacherExamServlet extends HttpServlet {
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
        req.setAttribute("subjects", teacherDao.getSubjectsByTeacherScope(teacher.getUserId()));
        req.getRequestDispatcher("/views/teacher/create-exam.jsp").forward(req, resp);
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
        long subjectId = Long.parseLong(req.getParameter("subjectId"));
        String title = req.getParameter("title");
        int duration = Integer.parseInt(req.getParameter("duration"));
        int totalMarks = Integer.parseInt(req.getParameter("totalMarks"));
        Integer passingMarks = null;
        String pm = req.getParameter("passingMarks");
        if (pm != null && !pm.isBlank()) {
            passingMarks = Integer.parseInt(pm.trim());
        }
        Timestamp from = parseTs(req.getParameter("availableFrom"));
        Timestamp until = parseTs(req.getParameter("availableUntil"));
        teacherDao.createExam(teacher.getUserId(), subjectId, title, duration, totalMarks, passingMarks, from, until);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    private static Timestamp parseTs(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        String s = v.trim().replace('T', ' ');
        if (s.length() == 16) {
            s = s + ":00";
        }
        try {
            return Timestamp.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }
}
