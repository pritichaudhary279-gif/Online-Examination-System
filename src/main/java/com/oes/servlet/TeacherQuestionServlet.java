package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TeacherQuestionServlet extends HttpServlet {
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
        Map<String, Object> meta = teacherDao.getExamMeta(examId, teacher.getUserId());
        if (meta == null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        long subjectId = ((Number) meta.get("subject_id")).longValue();
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("teacherQuestionToken", formToken);
        req.setAttribute("examId", examId);
        req.setAttribute("examMeta", meta);
        req.setAttribute("formToken", formToken);
        req.setAttribute("questions", teacherDao.getQuestionsByExam(examId, teacher.getUserId()));
        req.setAttribute("bankQuestions", teacherDao.listQuestionBank(teacher.getUserId(), subjectId));
        req.getRequestDispatcher("/views/teacher/manage-questions.jsp").forward(req, resp);
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
        String formAction = req.getParameter("formAction");
        if (formAction == null) {
            formAction = "addQuestion";
        }
        String requestToken = req.getParameter("formToken");
        String sessionToken = (String) session.getAttribute("teacherQuestionToken");
        if (requestToken == null || sessionToken == null || !requestToken.equals(sessionToken)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        long examId = Long.parseLong(req.getParameter("examId"));

        if ("addFromBank".equals(formAction)) {
            String[] ids = req.getParameterValues("bankQuestionId");
            if (ids != null) {
                for (String id : ids) {
                    if (id != null && !id.isBlank()) {
                        teacherDao.copyBankQuestionToExam(Long.parseLong(id.trim()), examId, teacher.getUserId());
                    }
                }
            }
            resp.sendRedirect(req.getContextPath() + "/teacher/exam/questions?examId=" + examId);
            return;
        }

        session.removeAttribute("teacherQuestionToken");

        String questionText = req.getParameter("questionText");
        String questionType = req.getParameter("questionType");
        int marks = Integer.parseInt(req.getParameter("marks"));
        String modelAnswer = req.getParameter("modelAnswer");
        String expectedKeywords = req.getParameter("expectedKeywords");

        long questionId = teacherDao.addQuestion(examId, questionText, questionType, marks, modelAnswer, expectedKeywords);
        if ("MCQ".equalsIgnoreCase(questionType)) {
            for (int i = 1; i <= 4; i++) {
                String optionText = req.getParameter("option" + i);
                String correct = req.getParameter("correctOption");
                if (optionText != null && !optionText.isBlank()) {
                    boolean isCorrect = String.valueOf(i).equals(correct);
                    teacherDao.addOption(questionId, optionText, isCorrect);
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/teacher/exam/questions?examId=" + examId);
    }
}
