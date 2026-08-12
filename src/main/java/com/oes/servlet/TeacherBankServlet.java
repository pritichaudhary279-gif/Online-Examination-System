package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

public class TeacherBankServlet extends HttpServlet {

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
        String sid = req.getParameter("subjectId");
        long subjectId = 0;
        if (sid != null && !sid.isBlank()) {
            subjectId = Long.parseLong(sid);
        } else {
            var subs = teacherDao.getSubjectsByTeacherScope(teacher.getUserId());
            if (!subs.isEmpty()) {
                subjectId = ((Number) subs.get(0).get("subject_id")).longValue();
            }
        }
        req.setAttribute("selectedSubjectId", subjectId);
        if (subjectId > 0) {
            req.setAttribute("bankRows", teacherDao.listQuestionBank(teacher.getUserId(), subjectId));
        }
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("teacherBankToken", formToken);
        req.setAttribute("formToken", formToken);
        req.getRequestDispatcher("/views/teacher/question-bank.jsp").forward(req, resp);
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
        String tok = req.getParameter("formToken");
        String sess = (String) session.getAttribute("teacherBankToken");
        if (tok == null || sess == null || !tok.equals(sess)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        session.removeAttribute("teacherBankToken");
        long subjectId = Long.parseLong(req.getParameter("subjectId"));
        String qText = req.getParameter("questionText");
        String qType = req.getParameter("questionType");
        int marks = Integer.parseInt(req.getParameter("marks"));
        String model = req.getParameter("modelAnswer");
        String kw = req.getParameter("expectedKeywords");
        long bq = teacherDao.insertBankQuestion(teacher.getUserId(), subjectId, qText, qType, marks, model, kw);
        if ("MCQ".equalsIgnoreCase(qType)) {
            for (int i = 1; i <= 4; i++) {
                String opt = req.getParameter("option" + i);
                String correct = req.getParameter("correctOption");
                if (opt != null && !opt.isBlank()) {
                    teacherDao.insertBankOption(bq, opt, String.valueOf(i).equals(correct));
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/teacher/bank?subjectId=" + subjectId);
    }
}
