package com.oes.servlet;

import com.oes.dao.IntegrityDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

/**
 * Integrity dashboard for admins (all exams) and teachers (own exams only).
 */
public class IntegrityDashboardServlet extends HttpServlet {

    private final IntegrityDao integrityDao = new IntegrityDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        String role = user.getRoleName();
        Long teacherId = null;
        if ("TEACHER".equalsIgnoreCase(role)) {
            teacherId = user.getUserId();
        } else if (!"ADMIN".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        req.setAttribute("isAdmin", "ADMIN".equalsIgnoreCase(role));
        try {
            req.setAttribute("summaryRows", integrityDao.loadViolationSummary(teacherId));
            req.setAttribute("recentRows", integrityDao.loadRecentViolationEvents(teacherId, 200));
            req.setAttribute("rawLogCount", integrityDao.countAllProctoringRows());
        } catch (RuntimeException e) {
            req.setAttribute("integrityError", e.getMessage());
            req.setAttribute("summaryRows", Collections.emptyList());
            req.setAttribute("recentRows", Collections.emptyList());
            req.setAttribute("rawLogCount", 0L);
        }
        req.getRequestDispatcher("/views/integrity/integrity-dashboard.jsp").forward(req, resp);
    }
}
