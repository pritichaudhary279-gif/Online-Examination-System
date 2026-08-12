package com.oes.servlet;

import com.oes.dao.AnalyticsDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AdminAnalyticsServlet extends HttpServlet {
    private final AnalyticsDao analyticsDao = new AnalyticsDao();

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
        req.setAttribute("subjectWise", analyticsDao.getSubjectWisePerformance());
        req.setAttribute("topStudents", analyticsDao.getTopStudents());
        req.setAttribute("passRate", analyticsDao.getExamPassRate());
        req.getRequestDispatcher("/views/admin/analytics.jsp").forward(req, resp);
    }
}
