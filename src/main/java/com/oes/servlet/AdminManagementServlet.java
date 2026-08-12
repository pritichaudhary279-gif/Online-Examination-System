package com.oes.servlet;

import com.oes.dao.AdminDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AdminManagementServlet extends HttpServlet {
    private final AdminDao adminDao = new AdminDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        loadData(req);
        req.getRequestDispatcher("/views/admin/manage.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("createUser".equals(action)) {
                long roleId = Long.parseLong(req.getParameter("roleId"));
                adminDao.createUser(roleId, req.getParameter("fullName"), req.getParameter("email"), req.getParameter("password"));
            } else if ("createCourse".equals(action)) {
                adminDao.createCourse(req.getParameter("courseName"), req.getParameter("description"));
            } else if ("createSubject".equals(action)) {
                long courseId = Long.parseLong(req.getParameter("courseId"));
                adminDao.createSubject(courseId, req.getParameter("subjectName"));
            } else if ("createNotification".equals(action)) {
                adminDao.createNotification(req.getParameter("title"), req.getParameter("message"));
            }
            resp.sendRedirect(req.getContextPath() + "/admin/manage");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            loadData(req);
            req.getRequestDispatcher("/views/admin/manage.jsp").forward(req, resp);
        }
    }

    private void loadData(HttpServletRequest req) {
        req.setAttribute("roles", adminDao.getRoles());
        req.setAttribute("users", adminDao.getUsers());
        req.setAttribute("courses", adminDao.getCourses());
        req.setAttribute("subjects", adminDao.getSubjects());
        req.setAttribute("notifications", adminDao.getRecentNotifications());
    }
}
