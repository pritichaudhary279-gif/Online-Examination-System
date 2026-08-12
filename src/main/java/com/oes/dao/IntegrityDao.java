package com.oes.dao;

import com.oes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Exam integrity / proctoring violation aggregates for admins and teachers.
 */
public class IntegrityDao {

    private static final String VIOLATION_FILTER =
            "pl.event_type IN ('TAB_SWITCH','WINDOW_BLUR','VIOLATION_TAB_SWITCH','VIOLATION_WINDOW_BLUR','PAGE_HIDE')";

    public void ensureProctoringSchema() {
        ensureProctoringLogsTable();
        tryAddColumn("ALTER TABLE proctoring_logs ADD COLUMN logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        tryAddColumn("ALTER TABLE proctoring_logs ADD COLUMN exam_id BIGINT NULL");
        tryAddColumn("ALTER TABLE proctoring_logs ADD COLUMN student_id BIGINT NULL");
    }

    private void ensureProctoringLogsTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS proctoring_logs ("
                + "log_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "result_id BIGINT NOT NULL,"
                + "exam_id BIGINT NULL,"
                + "student_id BIGINT NULL,"
                + "event_type VARCHAR(64) NOT NULL,"
                + "severity VARCHAR(16) NOT NULL,"
                + "logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_proctoring_result (result_id),"
                + "KEY idx_proctoring_exam_student (exam_id, student_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(ddl);
        } catch (Exception e) {
            throw new RuntimeException("Unable to ensure proctoring_logs table", e);
        }
    }

    private void tryAddColumn(String ddl) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                String m = e.getMessage() == null ? "" : e.getMessage();
                if (!m.contains("Duplicate column name")) {
                    throw new RuntimeException("Integrity schema migration failed", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Integrity schema migration failed", e);
        }
    }

    static String humanEventLabel(String eventType) {
        if (eventType == null) {
            return "Unknown";
        }
        switch (eventType.toUpperCase(Locale.ROOT)) {
            case "TAB_SWITCH":
            case "VIOLATION_TAB_SWITCH":
                return "Switched tab / hid exam";
            case "WINDOW_BLUR":
            case "VIOLATION_WINDOW_BLUR":
                return "Left exam window (blur)";
            case "PAGE_HIDE":
                return "Left or closed page";
            default:
                return eventType;
        }
    }

    /**
     * @param teacherId null = all exams (admin); non-null = that teacher's exams only
     */
    public List<Map<String, Object>> loadViolationSummary(Long teacherId) {
        ensureProctoringSchema();
        String sql = "SELECT COALESCE(pl.student_id, r.student_id) AS student_id, u.full_name AS student_name, u.email, "
                + "COALESCE(pl.exam_id, r.exam_id) AS exam_id, e.title AS exam_title, "
                + "COALESCE(c.course_name, '—') AS course_name, COALESCE(s.subject_name, '—') AS subject_name, "
                + "SUM(CASE WHEN pl.event_type IN ('TAB_SWITCH','VIOLATION_TAB_SWITCH') THEN 1 ELSE 0 END) AS tab_switches, "
                + "SUM(CASE WHEN pl.event_type IN ('WINDOW_BLUR','VIOLATION_WINDOW_BLUR') THEN 1 ELSE 0 END) AS window_blurs, "
                + "SUM(CASE WHEN pl.event_type = 'PAGE_HIDE' THEN 1 ELSE 0 END) AS page_hides, "
                + "COUNT(*) AS total_violations "
                + "FROM proctoring_logs pl "
                + "LEFT JOIN results r ON r.result_id = pl.result_id "
                + "INNER JOIN users u ON u.user_id = COALESCE(pl.student_id, r.student_id) "
                + "INNER JOIN exams e ON e.exam_id = COALESCE(pl.exam_id, r.exam_id) "
                + "LEFT JOIN subjects s ON s.subject_id = e.subject_id "
                + "LEFT JOIN courses c ON c.course_id = s.course_id "
                + "WHERE " + VIOLATION_FILTER + " "
                + "AND COALESCE(pl.student_id, r.student_id) IS NOT NULL "
                + "AND COALESCE(pl.exam_id, r.exam_id) IS NOT NULL ";
        if (teacherId != null) {
            sql += "AND e.teacher_id = ? ";
        }
        sql += "GROUP BY COALESCE(pl.student_id, r.student_id), u.full_name, u.email, "
                + "COALESCE(pl.exam_id, r.exam_id), e.title, c.course_name, s.subject_name "
                + "ORDER BY COUNT(*) DESC, u.full_name, e.title";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (teacherId != null) {
                ps.setLong(1, teacherId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("student_id", rs.getLong("student_id"));
                    row.put("student_name", rs.getString("student_name"));
                    row.put("email", rs.getString("email"));
                    row.put("exam_id", rs.getLong("exam_id"));
                    row.put("exam_title", rs.getString("exam_title"));
                    row.put("course_name", rs.getString("course_name"));
                    row.put("subject_name", rs.getString("subject_name"));
                    row.put("tab_switches", rs.getLong("tab_switches"));
                    row.put("window_blurs", rs.getLong("window_blurs"));
                    row.put("page_hides", rs.getLong("page_hides"));
                    row.put("total_violations", rs.getLong("total_violations"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load integrity summary", e);
        }
        return list;
    }

    public List<Map<String, Object>> loadRecentViolationEvents(Long teacherId, int limit) {
        ensureProctoringSchema();
        int cap = Math.min(Math.max(limit, 1), 500);
        String sql = "SELECT pl.logged_at, pl.event_type, pl.severity, u.full_name AS student_name, "
                + "COALESCE(pl.exam_id, r.exam_id) AS exam_id, e.title AS exam_title "
                + "FROM proctoring_logs pl "
                + "LEFT JOIN results r ON r.result_id = pl.result_id "
                + "INNER JOIN users u ON u.user_id = COALESCE(pl.student_id, r.student_id) "
                + "INNER JOIN exams e ON e.exam_id = COALESCE(pl.exam_id, r.exam_id) "
                + "WHERE " + VIOLATION_FILTER + " "
                + "AND COALESCE(pl.student_id, r.student_id) IS NOT NULL "
                + "AND COALESCE(pl.exam_id, r.exam_id) IS NOT NULL ";
        if (teacherId != null) {
            sql += "AND e.teacher_id = ? ";
        }
        sql += "ORDER BY pl.logged_at DESC LIMIT " + cap;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (teacherId != null) {
                ps.setLong(1, teacherId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("logged_at", rs.getTimestamp("logged_at"));
                    String et = rs.getString("event_type");
                    row.put("event_type", et);
                    row.put("event_label", humanEventLabel(et));
                    row.put("severity", rs.getString("severity"));
                    row.put("student_name", rs.getString("student_name"));
                    row.put("exam_id", rs.getLong("exam_id"));
                    row.put("exam_title", rs.getString("exam_title"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load integrity event log", e);
        }
        return list;
    }

    /** Total raw rows in proctoring_logs (any event), for diagnostics on the dashboard. */
    public long countAllProctoringRows() {
        ensureProctoringSchema();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM proctoring_logs")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to count proctoring logs", e);
        }
        return 0;
    }
}
