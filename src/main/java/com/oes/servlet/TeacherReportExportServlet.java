package com.oes.servlet;

import com.oes.model.User;
import com.oes.util.DBConnection;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeacherReportExportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"TEACHER".equalsIgnoreCase(user.getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        long examId = Long.parseLong(req.getParameter("examId"));
        String format = req.getParameter("format");
        if (format == null) {
            format = "csv";
        }
        format = format.trim().toLowerCase();

        new com.oes.dao.TeacherDao().ensureTeacherSchema();

        String sql = "SELECT u.full_name, u.email, s.subject_name, e.title AS exam_title, e.total_marks, " +
                "r.score_obtained, r.status, r.submitted_at, r.teacher_feedback " +
                "FROM results r JOIN users u ON u.user_id = r.student_id " +
                "JOIN exams e ON e.exam_id = r.exam_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "WHERE r.exam_id = ? AND e.teacher_id = ? ORDER BY u.full_name";

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("full_name", rs.getString("full_name"));
                    row.put("email", rs.getString("email"));
                    row.put("subject_name", rs.getString("subject_name"));
                    row.put("exam_title", rs.getString("exam_title"));
                    row.put("total_marks", rs.getInt("total_marks"));
                    row.put("score_obtained", rs.getBigDecimal("score_obtained"));
                    row.put("status", rs.getString("status"));
                    row.put("submitted_at", rs.getTimestamp("submitted_at"));
                    row.put("teacher_feedback", rs.getString("teacher_feedback"));
                    int tot = rs.getInt("total_marks");
                    BigDecimal score = rs.getBigDecimal("score_obtained");
                    BigDecimal pct = null;
                    if (tot > 0 && score != null && "EVALUATED".equalsIgnoreCase(rs.getString("status"))) {
                        pct = score.multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(tot), 2, RoundingMode.HALF_UP);
                    }
                    row.put("pct", pct);
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to export report", e);
        }

        if ("pdf".equals(format)) {
            exportPdf(resp, examId, rows);
        } else {
            exportCsv(resp, examId, rows);
        }
    }

    private void exportCsv(HttpServletResponse resp, long examId, List<Map<String, Object>> rows) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Content-Disposition", "attachment; filename=exam_" + examId + "_report.csv");
        try (PrintWriter out = resp.getWriter()) {
            out.println("Full Name,Email,Subject,Exam,Score,Out Of,Percentage %,Status,Submitted At,Teacher Feedback");
            for (Map<String, Object> r : rows) {
                out.print(csv(r.get("full_name")));
                out.print(',');
                out.print(csv(r.get("email")));
                out.print(',');
                out.print(csv(r.get("subject_name")));
                out.print(',');
                out.print(csv(r.get("exam_title")));
                out.print(',');
                out.print(csv(r.get("score_obtained")));
                out.print(',');
                out.print(csv(r.get("total_marks")));
                out.print(',');
                out.print(csv(r.get("pct")));
                out.print(',');
                out.print(csv(r.get("status")));
                out.print(',');
                out.print(csv(r.get("submitted_at")));
                out.print(',');
                out.print(csv(r.get("teacher_feedback")));
                out.println();
            }
        }
    }

    private static String csv(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        if (s.contains("\"") || s.contains(",") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void exportPdf(HttpServletResponse resp, long examId, List<Map<String, Object>> rows) throws IOException {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=exam_" + examId + "_report.pdf");
        try {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            doc.add(new Paragraph("Exam results report (Exam ID: " + examId + ")", titleFont));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.6f, 1.8f, 1.2f, 1.4f, 0.7f, 0.7f, 0.6f, 2f});
            addH(table, "Student", headFont);
            addH(table, "Email", headFont);
            addH(table, "Subject", headFont);
            addH(table, "Score", headFont);
            addH(table, "Out of", headFont);
            addH(table, "%", headFont);
            addH(table, "Status", headFont);
            addH(table, "Feedback", headFont);
            for (Map<String, Object> r : rows) {
                addC(table, r.get("full_name"), cellFont);
                addC(table, r.get("email"), cellFont);
                addC(table, r.get("subject_name"), cellFont);
                addC(table, r.get("score_obtained"), cellFont);
                addC(table, r.get("total_marks"), cellFont);
                addC(table, r.get("pct") == null ? "-" : r.get("pct"), cellFont);
                addC(table, r.get("status"), cellFont);
                addC(table, r.get("teacher_feedback"), cellFont);
            }
            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new IOException("PDF export failed", e);
        }
    }

    private static void addH(PdfPTable table, String t, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(t, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(5);
        table.addCell(c);
    }

    private static void addC(PdfPTable table, Object v, Font f) {
        String s = v == null ? "" : String.valueOf(v);
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setPadding(4);
        table.addCell(c);
    }
}
