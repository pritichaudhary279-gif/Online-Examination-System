package com.oes.servlet;

import com.oes.dao.AuthDao;
import com.oes.model.User;
import com.oes.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    private final AuthDao authDao = new AuthDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("registered"))) {
            req.setAttribute("message", "Registration successful. Please login.");
        }
        req.getRequestDispatcher("/views/common/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String identifier = req.getParameter("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = req.getParameter("email");
        }
        String password = req.getParameter("password");
        identifier = identifier == null ? "" : identifier.trim();
        password = password == null ? "" : password.trim();

        User user = authDao.login(identifier, PasswordUtil.sha256(password), PasswordUtil.md5(password), password);
        if (user == null) {
            req.setAttribute("error", "Invalid credentials.");
            req.getRequestDispatcher("/views/common/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(30 * 60);
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
