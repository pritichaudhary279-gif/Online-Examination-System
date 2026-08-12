package com.oes.dao;

import com.oes.util.DBConnection;
import com.oes.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDao {
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> map = new HashMap<>();
        map.put("users", getCount("SELECT COUNT(*) FROM users"));
        map.put("students", getCount("SELECT COUNT(*) FROM users u JOIN roles r ON r.role_id = u.role_id WHERE r.role_name = 'STUDENT'"));
        map.put("exams", getCount("SELECT COUNT(*) FROM exams"));
        map.put("active_users", getCount("SELECT COUNT(*) FROM users WHERE is_active = 1"));
        return map;
    }

    public List<Map<String, Object>> getRecentResults() {
        String sql = "SELECT u.full_name, e.title, s.subject_name, r.score_obtained, r.status " +
                "FROM results r " +
                "JOIN users u ON u.user_id = r.student_id " +
                "JOIN exams e ON e.exam_id = r.exam_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "ORDER BY r.result_id DESC LIMIT 10";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("full_name", rs.getString("full_name"));
                row.put("title", rs.getString("title"));
                row.put("subject_name", rs.getString("subject_name"));
                row.put("score_obtained", rs.getBigDecimal("score_obtained"));
                row.put("status", rs.getString("status"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load recent results", e);
        }
        return list;
    }

    public List<Map<String, Object>> getUsers() {
        String sql = "SELECT u.user_id, u.full_name, u.email, u.is_active, r.role_name " +
                "FROM users u JOIN roles r ON r.role_id = u.role_id ORDER BY u.user_id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("user_id", rs.getLong("user_id"));
                row.put("full_name", rs.getString("full_name"));
                row.put("email", rs.getString("email"));
                row.put("is_active", rs.getBoolean("is_active"));
                row.put("role_name", rs.getString("role_name"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load users", e);
        }
        return list;
    }

    public List<Map<String, Object>> getRoles() {
        String sql = "SELECT role_id, role_name FROM roles ORDER BY role_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("role_id", rs.getLong("role_id"));
                row.put("role_name", rs.getString("role_name"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load roles", e);
        }
        return list;
    }

    public List<Map<String, Object>> getCourses() {
        String sql = "SELECT course_id, course_name, description FROM courses ORDER BY course_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("course_id", rs.getLong("course_id"));
                row.put("course_name", rs.getString("course_name"));
                row.put("description", rs.getString("description"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load courses", e);
        }
        return list;
    }

    public List<Map<String, Object>> getSubjects() {
        String sql = "SELECT s.subject_id, s.subject_name, c.course_name FROM subjects s " +
                "JOIN courses c ON c.course_id = s.course_id ORDER BY c.course_name, s.subject_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("subject_id", rs.getLong("subject_id"));
                row.put("subject_name", rs.getString("subject_name"));
                row.put("course_name", rs.getString("course_name"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load subjects", e);
        }
        return list;
    }

    public void createUser(long roleId, String fullName, String email, String plainPassword) {
        String sql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) VALUES (?, ?, ?, ?, 1)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, PasswordUtil.sha256(plainPassword));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create user", e);
        }
    }

    public void createCourse(String courseName, String description) {
        String sql = "INSERT INTO courses (course_name, description) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, courseName);
            ps.setString(2, description);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create course", e);
        }
    }

    public void createSubject(long courseId, String subjectName) {
        String sql = "INSERT INTO subjects (course_id, subject_name) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setString(2, subjectName);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create subject", e);
        }
    }

    public void createNotification(String title, String message) {
        ensureNotificationTable();
        ensureUserNotificationsTable();
        String sql = "INSERT INTO notifications (title, message_body) VALUES (?, ?)";
        String mapSql = "INSERT INTO user_notifications (notification_id, user_id, is_read) " +
                "SELECT ?, user_id, 0 FROM users WHERE is_active = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
             PreparedStatement mapPs = con.prepareStatement(mapSql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long notificationId = keys.getLong(1);
                    mapPs.setLong(1, notificationId);
                    mapPs.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create notification", e);
        }
    }

    public List<Map<String, Object>> getRecentNotifications() {
        ensureNotificationTable();
        String sql = "SELECT notification_id, title, message_body, created_at FROM notifications ORDER BY notification_id DESC LIMIT 10";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("notification_id", rs.getLong("notification_id"));
                row.put("title", rs.getString("title"));
                row.put("message_body", rs.getString("message_body"));
                row.put("created_at", rs.getTimestamp("created_at"));
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load notifications", e);
        }
        return list;
    }

    private long getCount(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load admin stats", e);
        }
        return 0L;
    }

    private void ensureNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                "notification_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(200) NOT NULL," +
                "message_body TEXT NOT NULL," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize notification table", e);
        }
    }

    private void ensureUserNotificationsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_notifications (" +
                "user_notification_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "notification_id BIGINT UNSIGNED NOT NULL," +
                "user_id BIGINT UNSIGNED NOT NULL," +
                "is_read TINYINT(1) NOT NULL DEFAULT 0," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "CONSTRAINT fk_un_notification FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE," +
                "CONSTRAINT fk_un_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize user notifications table", e);
        }
    }

    /**
     * Optional external student identifier (roll / admission id). Added at runtime if missing.
     */
    public void ensureUsersStudentCodeColumn() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE users ADD COLUMN student_code VARCHAR(64) NULL");
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                String m = e.getMessage() == null ? "" : e.getMessage();
                if (!m.contains("Duplicate column name")) {
                    throw new RuntimeException("Unable to ensure users.student_code column", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to ensure users.student_code column", e);
        }
    }

    public long getRoleIdByName(String roleName) {
        String sql = "SELECT role_id FROM roles WHERE role_name = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("role_id");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve role", e);
        }
        throw new IllegalStateException("Role not found: " + roleName);
    }

    public void createStudentWithEnrollment(String fullName, String studentCode, long courseId, String username, String plainPassword) {
        ensureUsersStudentCodeColumn();
        String name = fullName == null ? "" : fullName.trim();
        String code = studentCode == null ? "" : studentCode.trim();
        String user = username == null ? "" : username.trim();
        String pass = plainPassword == null ? "" : plainPassword.trim();
        if (name.isBlank() || user.isBlank() || pass.isBlank()) {
            throw new IllegalArgumentException("Name, username, and password are required.");
        }
        long studentRoleId = getRoleIdByName("STUDENT");
        String insertUser = "INSERT INTO users (role_id, full_name, student_code, email, password_hash, is_active) VALUES (?, ?, ?, ?, ?, 1)";
        String insertEnroll = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        String existsEnroll = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_id = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, studentRoleId);
                ps.setString(2, name);
                if (code.isBlank()) {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(3, code);
                }
                ps.setString(4, user);
                ps.setString(5, PasswordUtil.sha256(pass));
                ps.executeUpdate();
                long studentId;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Unable to read new user id.");
                    }
                    studentId = keys.getLong(1);
                }
                boolean hasEnroll = false;
                try (PreparedStatement ex = con.prepareStatement(existsEnroll)) {
                    ex.setLong(1, studentId);
                    ex.setLong(2, courseId);
                    try (ResultSet rs = ex.executeQuery()) {
                        hasEnroll = rs.next();
                    }
                }
                if (!hasEnroll) {
                    try (PreparedStatement en = con.prepareStatement(insertEnroll)) {
                        en.setLong(1, studentId);
                        en.setLong(2, courseId);
                        en.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                if (e.getErrorCode() == 1062) {
                    throw new IllegalArgumentException("Username (email) already exists. Choose another.");
                }
                throw new RuntimeException("Unable to create student", e);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create student", e);
        }
    }

    public List<Map<String, Object>> getStudentsAdminOverview() {
        ensureUsersStudentCodeColumn();
        String sql = "SELECT u.user_id, u.full_name, u.student_code, u.email AS username, " +
                "GROUP_CONCAT(DISTINCT c.course_name ORDER BY c.course_name SEPARATOR ', ') AS enrolled_courses, " +
                "GROUP_CONCAT(DISTINCT CASE WHEN UPPER(TRIM(r.status)) = 'EVALUATED' " +
                "THEN CONCAT(s.subject_name, ' — ', e.title) END SEPARATOR '; ') AS exams_taken " +
                "FROM users u " +
                "JOIN roles rr ON rr.role_id = u.role_id AND rr.role_name = 'STUDENT' " +
                "LEFT JOIN enrollments en ON en.student_id = u.user_id " +
                "LEFT JOIN courses c ON c.course_id = en.course_id " +
                "LEFT JOIN results r ON r.student_id = u.user_id " +
                "LEFT JOIN exams e ON e.exam_id = r.exam_id " +
                "LEFT JOIN subjects s ON s.subject_id = e.subject_id " +
                "GROUP BY u.user_id, u.full_name, u.student_code, u.email " +
                "ORDER BY u.user_id DESC";
        return queryMapList(sql);
    }

    public List<Map<String, Object>> getAllTeacherExamsAdmin() {
        String sql = "SELECT e.exam_id, e.title, e.status, e.duration_minutes, e.total_marks, " +
                "t.full_name AS teacher_name, s.subject_name, c.course_name " +
                "FROM exams e " +
                "JOIN users t ON t.user_id = e.teacher_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "JOIN courses c ON c.course_id = s.course_id " +
                "ORDER BY e.exam_id DESC";
        return queryMapList(sql);
    }

    public List<Map<String, Object>> getSubjectWiseResultsDetail() {
        String sql = "SELECT u.full_name AS student_name, s.subject_name, e.title AS exam_title, " +
                "r.score_obtained AS score, e.total_marks AS out_of, r.status, " +
                "CASE WHEN UPPER(TRIM(r.status)) = 'EVALUATED' AND e.total_marks > 0 AND r.score_obtained IS NOT NULL " +
                "THEN ROUND(100.0 * r.score_obtained / e.total_marks, 2) ELSE NULL END AS pct " +
                "FROM results r " +
                "JOIN users u ON u.user_id = r.student_id " +
                "JOIN roles rr ON rr.role_id = u.role_id AND rr.role_name = 'STUDENT' " +
                "JOIN exams e ON e.exam_id = r.exam_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "ORDER BY s.subject_name, u.full_name, r.result_id DESC";
        return queryMapList(sql);
    }

    private List<Map<String, Object>> queryMapList(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.sql.ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to run admin query", e);
        }
        return list;
    }
}
