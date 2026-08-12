package com.oes.servlet;

import com.oes.dao.TeacherDao;
import com.oes.model.User;
import com.oes.util.TeacherBulkImportUtil;
import com.oes.util.TeacherPdfExtract;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Bulk import questions from CSV or PDF (text) into the bank or an exam.
 */
public class TeacherQuestionImportServlet extends HttpServlet {

    private final TeacherDao teacherDao = new TeacherDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
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

        String target = req.getParameter("target");
        String fileType = req.getParameter("fileType");
        if (fileType == null) {
            fileType = "csv";
        }
        Part filePart = req.getPart("dataFile");
        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(redirectBack(req, target, "missing"));
            return;
        }

        String body;
        if ("pdf".equalsIgnoreCase(fileType)) {
            try {
                body = TeacherPdfExtract.extractText(filePart.getInputStream());
            } catch (Exception e) {
                resp.sendRedirect(redirectBack(req, target, "pdf"));
                return;
            }
        } else {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            body = sb.toString();
        }

        int count = 0;
        try {
            if ("bank".equals(target)) {
                long subjectId = Long.parseLong(req.getParameter("subjectId"));
                count = importLines(teacher.getUserId(), subjectId, 0L, true, body);
                resp.sendRedirect(req.getContextPath() + "/teacher/bank?subjectId=" + subjectId + "&imported=" + count);
            } else if ("exam".equals(target)) {
                long examId = Long.parseLong(req.getParameter("examId"));
                count = importLines(teacher.getUserId(), 0L, examId, false, body);
                resp.sendRedirect(req.getContextPath() + "/teacher/exam/questions?examId=" + examId + "&imported=" + count);
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            }
        } catch (Exception e) {
            resp.sendRedirect(redirectBack(req, target, "err"));
        }
    }

    private String redirectBack(HttpServletRequest req, String target, String code) {
        if ("bank".equals(target)) {
            String sid = req.getParameter("subjectId");
            return req.getContextPath() + "/teacher/bank?subjectId=" + sid + "&err=" + code;
        }
        String eid = req.getParameter("examId");
        return req.getContextPath() + "/teacher/exam/questions?examId=" + eid + "&err=" + code;
    }

    private int importLines(long teacherId, long subjectId, long examId, boolean toBank, String body) {
        int count = 0;
        teacherDao.ensureTeacherSchema();
        for (String raw : body.split("\\r?\\n")) {
            TeacherBulkImportUtil.ParsedQuestion pq = TeacherBulkImportUtil.parseLine(raw);
            if (pq == null) {
                continue;
            }
            if ("MCQ".equals(pq.type)) {
                if (pq.options.size() < 2) {
                    continue;
                }
                if (pq.correctIndex1Based < 1 || pq.correctIndex1Based > pq.options.size()) {
                    continue;
                }
            }
            if (toBank) {
                long bq = teacherDao.insertBankQuestion(teacherId, subjectId, pq.questionText, pq.type, pq.marks,
                        pq.modelAnswer, pq.keywords);
                if ("MCQ".equals(pq.type)) {
                    for (int i = 0; i < pq.options.size(); i++) {
                        boolean ok = (i + 1) == pq.correctIndex1Based;
                        teacherDao.insertBankOption(bq, pq.options.get(i), ok);
                    }
                }
            } else {
                long qid = teacherDao.addQuestion(examId, pq.questionText, pq.type, pq.marks,
                        pq.modelAnswer, pq.keywords);
                if ("MCQ".equals(pq.type)) {
                    for (int i = 0; i < pq.options.size(); i++) {
                        boolean ok = (i + 1) == pq.correctIndex1Based;
                        teacherDao.addOption(qid, pq.options.get(i), ok);
                    }
                }
            }
            count++;
        }
        return count;
    }
}
