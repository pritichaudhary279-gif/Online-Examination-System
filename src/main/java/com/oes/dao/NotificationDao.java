package com.oes.dao;

import com.oes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDao {
    public List<Map<String, Object>> getUserNotifications(long userId) {
        ensureUserNotificationsTable();
        String sql = "SELECT n.notification_id, n.title, n.message_body, n.created_at, un.is_read " +
                "FROM user_notifications un " +
                "JOIN notifications n ON n.notification_id = un.notification_id " +
                "WHERE un.user_id = ? ORDER BY un.user_notification_id DESC LIMIT 10";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("notification_id", rs.getLong("notification_id"));
                    row.put("title", rs.getString("title"));
                    row.put("message_body", rs.getString("message_body"));
                    row.put("created_at", rs.getTimestamp("created_at"));
                    row.put("is_read", rs.getBoolean("is_read"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load user notifications", e);
        }
        return list;
    }

    public void ensureUserNotificationsTable() {
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
}
