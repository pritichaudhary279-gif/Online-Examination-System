package com.oes.servlet;

import com.oes.dao.AdminDao;
import com.oes.dao.NotificationDao;
import com.oes.service.StudentExamService;
import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DashboardServlet extends HttpServlet {
    private final AdminDao adminDao = new AdminDao();
    private final TeacherDao teacherDao = new TeacherDao();
    private final StudentExamService studentExamService = new StudentExamService();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        String role = user.getRoleName();
        if ("ADMIN".equalsIgnoreCase(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/panel?section=students");
            return;
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            req.setAttribute("subjects", teacherDao.getSubjectsByTeacherScope(user.getUserId()));
            req.setAttribute("teacherExams", teacherDao.getTeacherExams(user.getUserId()));
            req.setAttribute("userNotifications", notificationDao.getUserNotifications(user.getUserId()));
            req.getRequestDispatcher("/views/teacher/dashboard.jsp").forward(req, resp);
        } else {
            req.setAttribute("exams", studentExamService.getAvailableExams(user.getUserId()));
            req.setAttribute("results", studentExamService.getStudentResults(user.getUserId()));
            req.setAttribute("userNotifications", notificationDao.getUserNotifications(user.getUserId()));
            req.getRequestDispatcher("/views/student/dashboard.jsp").forward(req, resp);
        }
    }
}
