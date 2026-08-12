package com.oes.repository.impl;

import com.oes.model.ExamQuestion;
import com.oes.model.QuestionOption;
import com.oes.model.ResultView;
import com.oes.model.StudentExamView;
import com.oes.dao.TeacherDao;
import com.oes.repository.StudentRepository;
import com.oes.service.SubjectiveGradingService;
import com.oes.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentRepositoryImpl implements StudentRepository {

    private final SubjectiveGradingService subjectiveGradingService = new SubjectiveGradingService();

    private void ensureExamSchedulingColumns() {
        new TeacherDao().ensureTeacherSchema();
    }

    @Override
    public List<StudentExamView> findAvailableExams(long studentId) {
        ensureExamSchedulingColumns();
        String enrolledSql = "SELECT DISTINCT e.exam_id, c.course_name, s.subject_name, e.title, e.duration_minutes " +
                "FROM exams e " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "JOIN courses c ON c.course_id = s.course_id " +
                "JOIN enrollments en ON en.course_id = c.course_id " +
                "WHERE en.student_id = ? AND UPPER(TRIM(e.status)) = 'PUBLISHED' " +
                "AND (e.available_from IS NULL OR e.available_from <= NOW()) " +
                "AND (e.available_until IS NULL OR e.available_until >= NOW()) " +
                "ORDER BY e.exam_id DESC";

        List<StudentExamView> list = loadExamRows(enrolledSql, studentId);
        if (!list.isEmpty()) {
            return list;
        }

        // No published exams in enrolled courses: show all published exams (demo / visibility).
        String fallbackSql = "SELECT DISTINCT e.exam_id, c.course_name, s.subject_name, e.title, e.duration_minutes " +
                "FROM exams e " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "JOIN courses c ON c.course_id = s.course_id " +
                "WHERE UPPER(TRIM(e.status)) = 'PUBLISHED' " +
                "AND (e.available_from IS NULL OR e.available_from <= NOW()) " +
                "AND (e.available_until IS NULL OR e.available_until >= NOW()) " +
                "ORDER BY e.exam_id DESC";
        return loadExamRowsWithoutStudent(fallbackSql);
    }

    private List<StudentExamView> loadExamRows(String sql, long studentId) {
        List<StudentExamView> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapExamRow(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load student exams", e);
        }
        return list;
    }

    private List<StudentExamView> loadExamRowsWithoutStudent(String sql) {
        List<StudentExamView> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapExamRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load published exams", e);
        }
        return list;
    }

    private StudentExamView mapExamRow(ResultSet rs) throws Exception {
        StudentExamView row = new StudentExamView();
        row.setExamId(rs.getLong("exam_id"));
        row.setCourseName(rs.getString("course_name"));
        row.setSubjectName(rs.getString("subject_name"));
        row.setExamTitle(rs.getString("title"));
        row.setDurationMinutes(rs.getInt("duration_minutes"));
        return row;
    }

    @Override
    public int findExamTotalMarks(long examId) {
        String sql = "SELECT total_marks FROM exams WHERE exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_marks");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load exam total marks", e);
        }
        return 0;
    }

    @Override
    public Integer findExamPassingMarks(long examId) {
        ensureExamSchedulingColumns();
        String sql = "SELECT passing_marks FROM exams WHERE exam_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt("passing_marks");
                    if (rs.wasNull()) {
                        return null;
                    }
                    return v;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load passing marks", e);
        }
        return null;
    }

    @Override
    public List<ExamQuestion> findQuestionsByExam(long examId) {
        String sql = "SELECT q.question_id, q.question_text, q.question_type, q.marks, " +
                "o.option_id, o.option_text " +
                "FROM questions q LEFT JOIN options o ON o.question_id = q.question_id " +
                "WHERE q.exam_id = ? ORDER BY q.question_id, o.option_id";
        Map<Long, ExamQuestion> map = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long qid = rs.getLong("question_id");
                    ExamQuestion q = map.get(qid);
                    if (q == null) {
                        q = new ExamQuestion();
                        q.setQuestionId(qid);
                        q.setQuestionText(rs.getString("question_text"));
                        q.setQuestionType(rs.getString("question_type"));
                        q.setMarks(rs.getInt("marks"));
                        map.put(qid, q);
                    }
                    long optionId = rs.getLong("option_id");
                    if (optionId > 0) {
                        QuestionOption opt = new QuestionOption();
                        opt.setOptionId(optionId);
                        opt.setOptionText(rs.getString("option_text"));
                        q.getOptions().add(opt);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load exam questions", e);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public long ensureInProgressResult(long examId, long studentId) {
        String checkSql = "SELECT result_id FROM results WHERE exam_id = ? AND student_id = ? AND status = 'IN_PROGRESS'";
        String insertSql = "INSERT INTO results (exam_id, student_id, status) VALUES (?, ?, 'IN_PROGRESS')";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setLong(1, examId);
                check.setLong(2, studentId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("result_id");
                    }
                }
            }
            try (PreparedStatement insert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setLong(1, examId);
                insert.setLong(2, studentId);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to start result", e);
        }
        throw new RuntimeException("Unable to create result");
    }

    @Override
    public boolean hasEvaluatedAttempt(long examId, long studentId) {
        String sql = "SELECT COUNT(*) FROM results WHERE exam_id = ? AND student_id = ? AND status = 'EVALUATED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to validate exam attempt", e);
        }
        return false;
    }

    @Override
    public Map<Long, String> findSavedAnswers(long examId, long studentId) {
        String sql = "SELECT q.question_id, q.question_type, sa.selected_option_id, sa.subjective_answer " +
                "FROM questions q " +
                "LEFT JOIN results r ON r.exam_id = q.exam_id AND r.student_id = ? AND r.status = 'IN_PROGRESS' " +
                "LEFT JOIN student_answers sa ON sa.result_id = r.result_id AND sa.question_id = q.question_id " +
                "WHERE q.exam_id = ?";
        Map<Long, String> answers = new HashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long qid = rs.getLong("question_id");
                    String type = rs.getString("question_type");
                    if ("MCQ".equalsIgnoreCase(type)) {
                        long selected = rs.getLong("selected_option_id");
                        if (selected > 0) {
                            answers.put(qid, String.valueOf(selected));
                        }
                    } else {
                        String text = rs.getString("subjective_answer");
                        if (text != null) {
                            answers.put(qid, text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load saved answers", e);
        }
        return answers;
    }

    @Override
    public void replaceAnswersForResult(long resultId, long examId, Map<Long, String> answerMap) {
        String clearSql = "DELETE FROM student_answers WHERE result_id = ?";
        String qTypeSql = "SELECT question_id, question_type FROM questions WHERE exam_id = ?";
        String saveSql = "INSERT INTO student_answers (result_id, question_id, selected_option_id, subjective_answer, awarded_marks) VALUES (?, ?, ?, ?, 0)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement clear = con.prepareStatement(clearSql)) {
                clear.setLong(1, resultId);
                clear.executeUpdate();
            }
            Map<Long, String> types = new HashMap<>();
            try (PreparedStatement qType = con.prepareStatement(qTypeSql)) {
                qType.setLong(1, examId);
                try (ResultSet rs = qType.executeQuery()) {
                    while (rs.next()) {
                        types.put(rs.getLong("question_id"), rs.getString("question_type"));
                    }
                }
            }
            try (PreparedStatement save = con.prepareStatement(saveSql)) {
                for (Map.Entry<Long, String> entry : answerMap.entrySet()) {
                    Long questionId = entry.getKey();
                    String value = entry.getValue();
                    String type = types.get(questionId);
                    if (type == null) {
                        continue;
                    }
                    save.setLong(1, resultId);
                    save.setLong(2, questionId);
                    if ("MCQ".equalsIgnoreCase(type)) {
                        long selected = value == null || value.isBlank() ? 0L : Long.parseLong(value);
                        if (selected == 0L) {
                            save.setNull(3, java.sql.Types.BIGINT);
                        } else {
                            save.setLong(3, selected);
                        }
                        save.setNull(4, java.sql.Types.LONGVARCHAR);
                    } else {
                        save.setNull(3, java.sql.Types.BIGINT);
                        save.setString(4, value);
                    }
                    save.addBatch();
                }
                save.executeBatch();
            }
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Unable to autosave answers", e);
        }
    }

    @Override
    public BigDecimal finalizeExamGrading(long examId, long studentId, long resultId, Map<Long, String> answerMap) {
        String clearSql = "DELETE FROM student_answers WHERE result_id = ?";
        String saveSql = "INSERT INTO student_answers (result_id, question_id, selected_option_id, subjective_answer, awarded_marks) VALUES (?, ?, ?, ?, ?)";
        String mcqSql = "SELECT q.question_id, q.marks, o.option_id FROM questions q " +
                "JOIN options o ON o.question_id = q.question_id " +
                "WHERE q.exam_id = ? AND q.question_type = 'MCQ' AND o.is_correct = 1";
        String subjectiveSql = "SELECT question_id, marks, expected_keywords FROM questions WHERE exam_id = ? AND question_type = 'SUBJECTIVE'";
        String updateResultSql = "UPDATE results SET score_obtained = ?, status = 'EVALUATED', submitted_at = ? WHERE result_id = ?";

        BigDecimal total = BigDecimal.ZERO;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement clear = con.prepareStatement(clearSql)) {
                clear.setLong(1, resultId);
                clear.executeUpdate();
            }
            try (PreparedStatement mcq = con.prepareStatement(mcqSql);
                 PreparedStatement save = con.prepareStatement(saveSql)) {
                mcq.setLong(1, examId);
                try (ResultSet rs = mcq.executeQuery()) {
                    while (rs.next()) {
                        long questionId = rs.getLong("question_id");
                        long correctOptionId = rs.getLong("option_id");
                        int marks = rs.getInt("marks");
                        String ans = answerMap.get(questionId);
                        long selectedOption = ans == null || ans.isBlank() ? 0L : Long.parseLong(ans);
                        BigDecimal awarded = selectedOption == correctOptionId ? BigDecimal.valueOf(marks) : BigDecimal.ZERO;
                        total = total.add(awarded);

                        save.setLong(1, resultId);
                        save.setLong(2, questionId);
                        if (selectedOption == 0L) {
                            save.setNull(3, java.sql.Types.BIGINT);
                        } else {
                            save.setLong(3, selectedOption);
                        }
                        save.setNull(4, java.sql.Types.LONGVARCHAR);
                        save.setBigDecimal(5, awarded);
                        save.addBatch();
                    }
                }
                save.executeBatch();
            }

            try (PreparedStatement subjective = con.prepareStatement(subjectiveSql);
                 PreparedStatement save = con.prepareStatement(saveSql)) {
                subjective.setLong(1, examId);
                try (ResultSet rs = subjective.executeQuery()) {
                    while (rs.next()) {
                        long questionId = rs.getLong("question_id");
                        int marks = rs.getInt("marks");
                        String expectedKeywords = rs.getString("expected_keywords");
                        String text = answerMap.get(questionId);
                        BigDecimal awarded = subjectiveGradingService.evaluateByKeywords(text, expectedKeywords, marks);
                        total = total.add(awarded);
                        save.setLong(1, resultId);
                        save.setLong(2, questionId);
                        save.setNull(3, java.sql.Types.BIGINT);
                        save.setString(4, text);
                        save.setBigDecimal(5, awarded);
                        save.addBatch();
                    }
                }
                save.executeBatch();
            }

            try (PreparedStatement update = con.prepareStatement(updateResultSql)) {
                update.setBigDecimal(1, total);
                update.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                update.setLong(3, resultId);
                update.executeUpdate();
            }
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Unable to submit exam", e);
        }
        return total;
    }

    @Override
    public void insertProctoringLog(long resultId, long examId, long studentId, String eventType, String severity) {
        String sql = "INSERT INTO proctoring_logs (result_id, exam_id, student_id, event_type, severity) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, resultId);
            ps.setLong(2, examId);
            ps.setLong(3, studentId);
            ps.setString(4, eventType);
            ps.setString(5, severity);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save proctoring event", e);
        }
    }

    @Override
    public List<ResultView> findResultsByStudent(long studentId) {
        ensureExamSchedulingColumns();
        String sql = "SELECT s.subject_name, e.title, r.score_obtained, e.total_marks, r.status, r.teacher_feedback " +
                "FROM results r JOIN exams e ON e.exam_id = r.exam_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "WHERE r.student_id = ? ORDER BY r.result_id DESC";
        List<ResultView> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultView r = new ResultView();
                    r.setSubjectName(rs.getString("subject_name"));
                    r.setExamTitle(rs.getString("title"));
                    r.setScore(rs.getBigDecimal("score_obtained"));
                    int total = rs.getInt("total_marks");
                    r.setTotalMarks(total);
                    String st = rs.getString("status");
                    r.setStatus(st);
                    r.setTeacherFeedback(rs.getString("teacher_feedback"));
                    if ("EVALUATED".equalsIgnoreCase(st) && total > 0 && r.getScore() != null) {
                        r.setPercentage(r.getScore()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
                    }
                    list.add(r);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load result panel", e);
        }
        return list;
    }
}
