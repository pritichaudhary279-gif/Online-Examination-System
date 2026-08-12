package com.oes.servlet;

import com.oes.dao.IntegrityDao;
import com.oes.model.User;
import com.oes.repository.StudentRepository;
import com.oes.repository.impl.StudentRepositoryImpl;
import com.oes.service.ProctoringService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ProctoringEventServlet extends HttpServlet {

    private final StudentRepository studentRepository = new StudentRepositoryImpl();
    private final ProctoringService proctoringService = new ProctoringService();
    private final IntegrityDao integrityDao = new IntegrityDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"STUDENT".equalsIgnoreCase(user.getRoleName())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String examParam = req.getParameter("examId");
        String eventType = req.getParameter("eventType");
        if (examParam == null || eventType == null || eventType.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long examId = Long.parseLong(examParam);
        integrityDao.ensureProctoringSchema();
        String severity = proctoringService.normalizeSeverity(eventType);
        long resultId = studentRepository.ensureInProgressResult(examId, user.getUserId());
        studentRepository.insertProctoringLog(resultId, examId, user.getUserId(), eventType, severity);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"logged\"}");
    }
}
