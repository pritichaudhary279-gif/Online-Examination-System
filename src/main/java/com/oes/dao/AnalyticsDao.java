package com.oes.dao;

import com.oes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDao {
    public List<Map<String, Object>> getSubjectWisePerformance() {
        String sql = "SELECT s.subject_name, COUNT(r.result_id) AS attempts, AVG(r.score_obtained) AS avg_score " +
                "FROM results r " +
                "JOIN exams e ON e.exam_id = r.exam_id " +
                "JOIN subjects s ON s.subject_id = e.subject_id " +
                "WHERE r.status = 'EVALUATED' " +
                "GROUP BY s.subject_name ORDER BY s.subject_name";
        return queryList(sql);
    }

    public List<Map<String, Object>> getTopStudents() {
        String sql = "SELECT u.full_name, u.email, SUM(r.score_obtained) AS total_score " +
                "FROM results r " +
                "JOIN users u ON u.user_id = r.student_id " +
                "WHERE r.status = 'EVALUATED' " +
                "GROUP BY u.user_id, u.full_name, u.email " +
                "ORDER BY total_score DESC LIMIT 10";
        return queryList(sql);
    }

    public List<Map<String, Object>> getExamPassRate() {
        String sql = "SELECT e.exam_id, e.title, " +
                "COUNT(r.result_id) AS total_attempts, " +
                "SUM(CASE WHEN r.score_obtained >= (e.total_marks * 0.4) THEN 1 ELSE 0 END) AS pass_count " +
                "FROM exams e LEFT JOIN results r ON r.exam_id = e.exam_id AND r.status = 'EVALUATED' " +
                "GROUP BY e.exam_id, e.title ORDER BY e.exam_id DESC";
        return queryList(sql);
    }

    private List<Map<String, Object>> queryList(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                int cols = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load analytics", e);
        }
        return list;
    }
}
