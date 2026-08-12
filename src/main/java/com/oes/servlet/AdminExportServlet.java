package com.oes.servlet;

import com.oes.dao.AdminDao;
import com.oes.model.User;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Exports subject-wise results for admins (CSV or PDF).
 */
public class AdminExportServlet extends HttpServlet {

    private final AdminDao adminDao = new AdminDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (!"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String format = req.getParameter("format");
        if (format == null) {
            format = "csv";
        }
        format = format.trim().toLowerCase();
        adminDao.ensureUsersStudentCodeColumn();
        List<Map<String, Object>> rows = adminDao.getSubjectWiseResultsDetail();

        if ("pdf".equals(format)) {
            exportPdf(resp, rows);
        } else {
            exportCsv(resp, rows);
        }
    }

    private void exportCsv(HttpServletResponse resp, List<Map<String, Object>> rows) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Content-Disposition", "attachment; filename=\"subject-wise-results.csv\"");
        try (PrintWriter w = resp.getWriter()) {
            w.println("Student Name,Subject,Exam,Score,Out of,Percentage %,Status");
            for (Map<String, Object> row : rows) {
                w.print(csvEscape(row.get("student_name")));
                w.print(',');
                w.print(csvEscape(row.get("subject_name")));
                w.print(',');
                w.print(csvEscape(row.get("exam_title")));
                w.print(',');
                w.print(csvEscape(row.get("score")));
                w.print(',');
                w.print(csvEscape(row.get("out_of")));
                w.print(',');
                w.print(csvEscape(row.get("pct")));
                w.print(',');
                w.print(csvEscape(row.get("status")));
                w.println();
            }
        }
    }

    private static String csvEscape(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        if (s.contains("\"") || s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void exportPdf(HttpServletResponse resp, List<Map<String, Object>> rows) throws IOException {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"subject-wise-results.pdf\"");
        try {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.add(new Paragraph("Subject-wise results (all students)", titleFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.8f, 2.2f, 0.9f, 0.9f, 0.9f, 1.1f});

            addHeaderCell(table, "Student", headFont);
            addHeaderCell(table, "Subject", headFont);
            addHeaderCell(table, "Exam", headFont);
            addHeaderCell(table, "Score", headFont);
            addHeaderCell(table, "Out of", headFont);
            addHeaderCell(table, "%", headFont);
            addHeaderCell(table, "Status", headFont);

            for (Map<String, Object> row : rows) {
                table.addCell(textCell(row.get("student_name"), cellFont));
                table.addCell(textCell(row.get("subject_name"), cellFont));
                table.addCell(textCell(row.get("exam_title"), cellFont));
                table.addCell(textCell(row.get("score"), cellFont));
                table.addCell(textCell(row.get("out_of"), cellFont));
                Object pct = row.get("pct");
                String pctStr = pct == null ? "-" : formatNumber(pct);
                table.addCell(textCell(pctStr, cellFont));
                table.addCell(textCell(row.get("status"), cellFont));
            }
            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new IOException("PDF export failed", e);
        }
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(6);
        table.addCell(c);
    }

    private static PdfPCell textCell(Object value, Font font) {
        String s = value == null ? "" : String.valueOf(value);
        PdfPCell c = new PdfPCell(new Phrase(s, font));
        c.setPadding(4);
        return c;
    }

    private static String formatNumber(Object pct) {
        if (pct instanceof BigDecimal) {
            return ((BigDecimal) pct).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(pct);
    }
}
