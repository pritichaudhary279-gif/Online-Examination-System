package com.oes.dao;

import com.oes.model.ExamNotificationContext;
import com.oes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class ExamMailDao {

    public Optional<ExamNotificationContext> loadNotificationContext(long examId, long studentId) {
        String examSql = "SELECT e.title, t.full_name AS teacher_name, t.email AS teacher_email "
                + "FROM exams e JOIN users t ON t.user_id = e.teacher_id WHERE e.exam_id = ?";
        String studentSql = "SELECT full_name, email FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            ExamNotificationContext ctx = new ExamNotificationContext();
            try (PreparedStatement ps = con.prepareStatement(examSql)) {
                ps.setLong(1, examId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    ctx.setExamTitle(rs.getString("title"));
                    ctx.setTeacherName(rs.getString("teacher_name"));
                    ctx.setTeacherEmail(rs.getString("teacher_email"));
                }
            }
            try (PreparedStatement ps = con.prepareStatement(studentSql)) {
                ps.setLong(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    ctx.setStudentName(rs.getString("full_name"));
                    ctx.setStudentEmail(rs.getString("email"));
                }
            }
            return Optional.of(ctx);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load exam mail context", e);
        }
    }
}
