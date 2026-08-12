package com.oes.dao;

import com.oes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDao {

    public void ensureTeacherSchema() {
        tryAddColumn("ALTER TABLE exams ADD COLUMN passing_marks INT NULL");
        tryAddColumn("ALTER TABLE exams ADD COLUMN available_from DATETIME NULL");
        tryAddColumn("ALTER TABLE exams ADD COLUMN available_until DATETIME NULL");
        tryAddColumn("ALTER TABLE results ADD COLUMN teacher_feedback TEXT NULL");
        ensureTeacherSubjectsTable();
        ensureQuestionBankTables();
    }

    private void tryAddColumn(String ddl) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                String m = e.getMessage() == null ? "" : e.getMessage();
                if (!m.contains("Duplicate column name")) {
                    throw new RuntimeException("Schema migration failed: " + ddl, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Schema migration failed: " + ddl, e);
        }
    }

    private void ensureTeacherSubjectsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS teacher_subjects (" +
                "teacher_id BIGINT UNSIGNED NOT NULL," +
                "subject_id BIGINT UNSIGNED NOT NULL," +
                "PRIMARY KEY (teacher_id, subject_id)," +
                "CONSTRAINT fk_ts_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE," +
                "CONSTRAINT fk_ts_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create teacher_subjects", e);
        }
    }

    private void ensureQuestionBankTables() {
        String bank = "CREATE TABLE IF NOT EXISTS question_bank (" +
                "bank_question_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "subject_id BIGINT UNSIGNED NOT NULL," +
                "teacher_id BIGINT UNSIGNED NOT NULL," +
                "question_text TEXT NOT NULL," +
                "question_type VARCHAR(20) NOT NULL," +
                "marks INT NOT NULL," +
                "model_answer TEXT," +
                "expected_keywords VARCHAR(500)," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "CONSTRAINT fk_qb_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE," +
                "CONSTRAINT fk_qb_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";
        String opts = "CREATE TABLE IF NOT EXISTS question_bank_options (" +
                "bank_option_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "bank_question_id BIGINT UNSIGNED NOT NULL," +
                "option_text VARCHAR(500) NOT NULL," +
                "is_correct TINYINT(1) NOT NULL DEFAULT 0," +
                "CONSTRAINT fk_qbo_q FOREIGN KEY (bank_question_id) REFERENCES question_bank(bank_question_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(bank);
            st.executeUpdate(opts);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create question bank tables", e);
        }
    }

    public List<Map<String, Object>> getSubjectsByTeacherScope(long teacherId) {
        ensureTeacherSchema();
        String sql = "SELECT s.subject_id, c.course_name, s.subject_name " +
                "FROM subjects s " +
                "JOIN courses c ON c.course_id = s.course_id " +
                "WHERE EXISTS (SELECT 1 FROM teacher_subjects ts WHERE ts.teacher_id = ? AND ts.subject_id = s.subject_id) " +
                "OR NOT EXISTS (SELECT 1 FROM teacher_subjects ts2 WHERE ts2.teacher_id = ?) " +
                "ORDER BY c.course_name, s.subject_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("subject_id", rs.getLong("subject_id"));
                    row.put("course_name", rs.getString("course_name"));
                    row.put("subject_name", rs.getString("subject_name"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load subjects", e);
        }
        return list;
    }

    public void createExam(long teacherId, long subjectId, String title, int durationMinutes, int totalMarks,
                           Integer passingMarks, Timestamp availableFrom, Timestamp availableUntil) {
        ensureTeacherSchema();
        String sql = "INSERT INTO exams (subject_id, teacher_id, title, duration_minutes, total_marks, passing_marks, available_from, available_until, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PUBLISHED')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, subjectId);
            ps.setLong(2, teacherId);
            ps.setString(3, title);
            ps.setInt(4, durationMinutes);
            ps.setInt(5, totalMarks);
            if (passingMarks == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, passingMarks);
            }
            if (availableFrom == null) {
                ps.setNull(7, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, availableFrom);
            }
            if (availableUntil == null) {
                ps.setNull(8, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(8, availableUntil);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create exam", e);
        }
    }

    public List<Map<String, Object>> getTeacherExams(long teacherId) {
        ensureTeacherSchema();
        String sql = "SELECT e.exam_id, e.title, e.duration_minutes, e.total_marks, e.passing_marks, e.available_from, e.available_until, e.status, " +
                "c.course_name, s.subject_name " +
                "FROM exams e " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "JOIN courses c ON c.course_id = s.course_id " +
                "WHERE e.teacher_id = ? ORDER BY e.exam_id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("exam_id", rs.getLong("exam_id"));
                    row.put("title", rs.getString("title"));
                    row.put("duration_minutes", rs.getInt("duration_minutes"));
                    row.put("total_marks", rs.getInt("total_marks"));
                    row.put("passing_marks", rs.getObject("passing_marks"));
                    row.put("available_from", rs.getTimestamp("available_from"));
                    row.put("available_until", rs.getTimestamp("available_until"));
                    row.put("status", rs.getString("status"));
                    row.put("course_name", rs.getString("course_name"));
                    row.put("subject_name", rs.getString("subject_name"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load teacher exams", e);
        }
        return list;
    }

    public Map<String, Object> getExamMeta(long examId, long teacherId) {
        ensureTeacherSchema();
        String sql = "SELECT e.exam_id, e.subject_id, s.subject_name, e.title FROM exams e " +
                "JOIN subjects s ON s.subject_id = e.subject_id WHERE e.exam_id = ? AND e.teacher_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> row = new HashMap<>();
                row.put("exam_id", rs.getLong("exam_id"));
                row.put("subject_id", rs.getLong("subject_id"));
                row.put("subject_name", rs.getString("subject_name"));
                row.put("title", rs.getString("title"));
                return row;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load exam meta", e);
        }
    }

    public void updateExamStatus(long examId, long teacherId, String status) {
        String sql = "UPDATE exams SET status = ? WHERE exam_id = ? AND teacher_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, examId);
            ps.setLong(3, teacherId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to update exam status", e);
        }
    }

    public long addQuestion(long examId, String questionText, String questionType, int marks, String modelAnswer, String expectedKeywords) {
        String checkSql = "SELECT question_id FROM questions WHERE exam_id = ? AND question_text = ? AND question_type = ? LIMIT 1";
        String sql = "INSERT INTO questions (exam_id, question_text, question_type, marks, model_answer, expected_keywords) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSql);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            check.setLong(1, examId);
            check.setString(2, questionText);
            check.setString(3, questionType);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("question_id");
                }
            }
            ps.setLong(1, examId);
            ps.setString(2, questionText);
            ps.setString(3, questionType);
            ps.setInt(4, marks);
            ps.setString(5, modelAnswer);
            ps.setString(6, expectedKeywords);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to add question", e);
        }
        throw new RuntimeException("Unable to get question id");
    }

    public void addOption(long questionId, String optionText, boolean isCorrect) {
        String checkSql = "SELECT option_id FROM options WHERE question_id = ? AND option_text = ? LIMIT 1";
        String sql = "INSERT INTO options (question_id, option_text, is_correct) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSql);
             PreparedStatement ps = con.prepareStatement(sql)) {
            check.setLong(1, questionId);
            check.setString(2, optionText);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
            ps.setLong(1, questionId);
            ps.setString(2, optionText);
            ps.setBoolean(3, isCorrect);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to add option", e);
        }
    }

    public List<Map<String, Object>> getQuestionsByExam(long examId, long teacherId) {
        String sql = "SELECT q.question_id, q.question_text, q.question_type, q.marks, " +
                "GROUP_CONCAT(o.option_text ORDER BY o.option_id SEPARATOR ' | ') AS options_text, " +
                "GROUP_CONCAT(CASE WHEN o.is_correct = 1 THEN o.option_text END ORDER BY o.option_id SEPARATOR ', ') AS correct_options " +
                "FROM questions q " +
                "JOIN exams e ON e.exam_id = q.exam_id " +
                "LEFT JOIN options o ON o.question_id = q.question_id " +
                "WHERE q.exam_id = ? AND e.teacher_id = ? " +
                "GROUP BY q.question_id, q.question_text, q.question_type, q.marks " +
                "ORDER BY q.question_id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("question_id", rs.getLong("question_id"));
                    row.put("question_text", rs.getString("question_text"));
                    row.put("question_type", rs.getString("question_type"));
                    row.put("marks", rs.getInt("marks"));
                    row.put("options_text", rs.getString("options_text"));
                    row.put("correct_options", rs.getString("correct_options"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load exam questions", e);
        }
        return list;
    }

    public List<Map<String, Object>> listQuestionBank(long teacherId, long subjectId) {
        ensureTeacherSchema();
        String sql = "SELECT bank_question_id, question_text, question_type, marks, created_at FROM question_bank " +
                "WHERE teacher_id = ? AND subject_id = ? ORDER BY bank_question_id DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            ps.setLong(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("bank_question_id", rs.getLong("bank_question_id"));
                    row.put("question_text", rs.getString("question_text"));
                    row.put("question_type", rs.getString("question_type"));
                    row.put("marks", rs.getInt("marks"));
                    row.put("created_at", rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load question bank", e);
        }
        return list;
    }

    public long insertBankQuestion(long teacherId, long subjectId, String questionText, String questionType, int marks,
                                   String modelAnswer, String expectedKeywords) {
        ensureTeacherSchema();
        String sql = "INSERT INTO question_bank (subject_id, teacher_id, question_text, question_type, marks, model_answer, expected_keywords) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, subjectId);
            ps.setLong(2, teacherId);
            ps.setString(3, questionText);
            ps.setString(4, questionType);
            ps.setInt(5, marks);
            ps.setString(6, modelAnswer);
            ps.setString(7, expectedKeywords);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to add bank question", e);
        }
        throw new RuntimeException("Unable to read bank question id");
    }

    public void insertBankOption(long bankQuestionId, String optionText, boolean correct) {
        String sql = "INSERT INTO question_bank_options (bank_question_id, option_text, is_correct) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, bankQuestionId);
            ps.setString(2, optionText);
            ps.setBoolean(3, correct);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to add bank option", e);
        }
    }

    public void copyBankQuestionToExam(long bankQuestionId, long examId, long teacherId) {
        ensureTeacherSchema();
        String verify = "SELECT b.question_text, b.question_type, b.marks, b.model_answer, b.expected_keywords " +
                "FROM question_bank b JOIN exams e ON e.subject_id = b.subject_id " +
                "WHERE b.bank_question_id = ? AND e.exam_id = ? AND e.teacher_id = ? AND b.teacher_id = ?";
        String qSql = "INSERT INTO questions (exam_id, question_text, question_type, marks, model_answer, expected_keywords) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String optSql = "INSERT INTO options (question_id, option_text, is_correct) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String qText;
                String qType;
                int marks;
                String model;
                String kw;
                try (PreparedStatement v = con.prepareStatement(verify)) {
                    v.setLong(1, bankQuestionId);
                    v.setLong(2, examId);
                    v.setLong(3, teacherId);
                    v.setLong(4, teacherId);
                    try (ResultSet rs = v.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Bank question does not match this exam or teacher.");
                        }
                        qText = rs.getString("question_text");
                        qType = rs.getString("question_type");
                        marks = rs.getInt("marks");
                        model = rs.getString("model_answer");
                        kw = rs.getString("expected_keywords");
                    }
                }
                long newQid;
                try (PreparedStatement ins = con.prepareStatement(qSql, Statement.RETURN_GENERATED_KEYS)) {
                    ins.setLong(1, examId);
                    ins.setString(2, qText);
                    ins.setString(3, qType);
                    ins.setInt(4, marks);
                    ins.setString(5, model);
                    ins.setString(6, kw);
                    ins.executeUpdate();
                    try (ResultSet keys = ins.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new RuntimeException("No question id");
                        }
                        newQid = keys.getLong(1);
                    }
                }
                String loadOpts = "SELECT option_text, is_correct FROM question_bank_options WHERE bank_question_id = ? ORDER BY bank_option_id";
                try (PreparedStatement lo = con.prepareStatement(loadOpts);
                     PreparedStatement io = con.prepareStatement(optSql)) {
                    lo.setLong(1, bankQuestionId);
                    try (ResultSet ors = lo.executeQuery()) {
                        while (ors.next()) {
                            io.setLong(1, newQid);
                            io.setString(2, ors.getString("option_text"));
                            io.setBoolean(3, ors.getBoolean("is_correct"));
                            io.executeUpdate();
                        }
                    }
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to copy bank question", e);
        }
    }

    public List<Map<String, Object>> getExamRoster(long examId, long teacherId) {
        ensureTeacherSchema();
        String sql = "SELECT u.user_id, u.full_name, u.email, " +
                "COALESCE(r.status, 'NOT_STARTED') AS attempt_status, r.score_obtained, r.submitted_at, r.result_id, r.teacher_feedback " +
                "FROM exams e " +
                "JOIN subjects sub ON sub.subject_id = e.subject_id " +
                "JOIN enrollments en ON en.course_id = sub.course_id " +
                "JOIN users u ON u.user_id = en.student_id " +
                "JOIN roles rr ON rr.role_id = u.role_id AND UPPER(TRIM(rr.role_name)) = 'STUDENT' " +
                "LEFT JOIN results r ON r.exam_id = e.exam_id AND r.student_id = u.user_id " +
                "WHERE e.exam_id = ? AND e.teacher_id = ? " +
                "ORDER BY u.full_name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("user_id", rs.getLong("user_id"));
                    row.put("full_name", rs.getString("full_name"));
                    row.put("email", rs.getString("email"));
                    row.put("attempt_status", rs.getString("attempt_status"));
                    row.put("score_obtained", rs.getBigDecimal("score_obtained"));
                    row.put("submitted_at", rs.getTimestamp("submitted_at"));
                    row.put("result_id", rs.getObject("result_id"));
                    row.put("teacher_feedback", rs.getString("teacher_feedback"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load roster", e);
        }
        return list;
    }

    public void updateResultFeedback(long resultId, long teacherId, String feedback) {
        ensureTeacherSchema();
        String sql = "UPDATE results r JOIN exams e ON e.exam_id = r.exam_id " +
                "SET r.teacher_feedback = ? WHERE r.result_id = ? AND e.teacher_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, feedback);
            ps.setLong(2, resultId);
            ps.setLong(3, teacherId);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new IllegalArgumentException("Unable to update feedback for this result.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to save feedback", e);
        }
    }
}
