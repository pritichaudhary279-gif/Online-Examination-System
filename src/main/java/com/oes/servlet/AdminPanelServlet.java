package com.oes.servlet;

import com.oes.dao.AdminDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Admin workspace: Students, Exams, Results, Reports.
 */
public class AdminPanelServlet extends HttpServlet {

    private static final Set<String> SECTIONS = new HashSet<>();

    static {
        SECTIONS.add("students");
        SECTIONS.add("exams");
        SECTIONS.add("results");
        SECTIONS.add("reports");
    }

    private final AdminDao adminDao = new AdminDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String section = req.getParameter("section");
        if (section == null || section.isBlank() || !SECTIONS.contains(section)) {
            section = "students";
        }
        loadPanelData(req, section);
        req.getRequestDispatcher("/views/admin/admin-panel.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("addStudent".equals(action)) {
                long courseId = Long.parseLong(req.getParameter("courseId"));
                adminDao.createStudentWithEnrollment(
                        req.getParameter("fullName"),
                        req.getParameter("studentCode"),
                        courseId,
                        req.getParameter("username"),
                        req.getParameter("password"));
                resp.sendRedirect(req.getContextPath() + "/admin/panel?section=students");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/panel?section=students");
        } catch (IllegalArgumentException ex) {
            req.setAttribute("error", ex.getMessage());
            loadPanelData(req, "students");
            req.getRequestDispatcher("/views/admin/admin-panel.jsp").forward(req, resp);
        } catch (Exception ex) {
            req.setAttribute("error", ex.getMessage());
            loadPanelData(req, "students");
            req.getRequestDispatcher("/views/admin/admin-panel.jsp").forward(req, resp);
        }
    }

    private void loadPanelData(HttpServletRequest req, String section) {
        adminDao.ensureUsersStudentCodeColumn();
        req.setAttribute("section", section);
        req.setAttribute("stats", adminDao.getDashboardStats());
        req.setAttribute("courses", adminDao.getCourses());
        req.setAttribute("studentsOverview", adminDao.getStudentsAdminOverview());
        req.setAttribute("teacherExams", adminDao.getAllTeacherExamsAdmin());
        req.setAttribute("subjectWiseResults", adminDao.getSubjectWiseResultsDetail());
    }

    private boolean ensureAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return false;
        }
        return true;
    }
}
