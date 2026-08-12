package com.oes.dao;

import com.oes.model.User;
import com.oes.util.DBConnection;
import com.oes.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuthDao {
    public User login(String identifier, String passwordHash, String md5Hash, String rawPassword) {
        ensureDefaultAccounts();
        String sql = "SELECT u.user_id, u.role_id, u.full_name, u.email, r.role_name " +
                "FROM users u JOIN roles r ON r.role_id = u.role_id " +
                "WHERE (LOWER(TRIM(u.email)) = LOWER(TRIM(?)) OR CAST(u.user_id AS CHAR) = TRIM(?)) " +
                "AND (u.password_hash = ? OR u.password_hash = ? OR u.password_hash = ?) " +
                "AND u.is_active = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, passwordHash);
            ps.setString(4, md5Hash);
            ps.setString(5, rawPassword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setRoleId(rs.getLong("role_id"));
                    user.setRoleName(rs.getString("role_name"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Login error", e);
        }
        return null;
    }

    public User registerStudent(String fullName, String email, String plainPassword) {
        ensureDefaultAccounts();

        String name = fullName == null ? "" : fullName.trim();
        String mail = email == null ? "" : email.trim();
        String pass = plainPassword == null ? "" : plainPassword.trim();
        if (name.isBlank() || mail.isBlank() || pass.isBlank()) {
            throw new IllegalArgumentException("Full name, email and password are required.");
        }

        if (emailExists(mail)) {
            throw new IllegalArgumentException("Email is already registered. Please login.");
        }

        String insertSql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) " +
                "SELECT role_id, ?, ?, ?, 1 FROM roles WHERE role_name = 'STUDENT'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, mail);
            ps.setString(3, PasswordUtil.sha256(pass));
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                throw new RuntimeException("Registration failed.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to register student", e);
        }

        User user = login(mail, PasswordUtil.sha256(pass), PasswordUtil.md5(pass), pass);
        if (user == null) {
            throw new RuntimeException("Registration succeeded, but login failed.");
        }
        return user;
    }

    private boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to validate email", e);
        }
    }

    private void ensureDefaultAccounts() {
        String defaultHash = PasswordUtil.sha256("123456");
        String rolesSql = "INSERT INTO roles (role_name, description) VALUES " +
                "('ADMIN', 'System administrator'), ('TEACHER', 'Faculty user'), ('STUDENT', 'Learner user') " +
                "ON DUPLICATE KEY UPDATE description = VALUES(description)";
        String adminSql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) " +
                "SELECT role_id, 'System Admin', 'admin@oes.com', ?, 1 FROM roles WHERE role_name = 'ADMIN' " +
                "ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), full_name = VALUES(full_name), password_hash = VALUES(password_hash), is_active = VALUES(is_active)";
        String teacherSql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) " +
                "SELECT role_id, 'Teacher One', 'teacher@oes.com', ?, 1 FROM roles WHERE role_name = 'TEACHER' " +
                "ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), full_name = VALUES(full_name), password_hash = VALUES(password_hash), is_active = VALUES(is_active)";
        String studentSql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) " +
                "SELECT role_id, 'Student One', 'student@oes.com', ?, 1 FROM roles WHERE role_name = 'STUDENT' " +
                "ON DUPLICATE KEY UPDATE role_id = VALUES(role_id), full_name = VALUES(full_name), password_hash = VALUES(password_hash), is_active = VALUES(is_active)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement rolePs = con.prepareStatement(rolesSql);
             PreparedStatement adminPs = con.prepareStatement(adminSql);
             PreparedStatement teacherPs = con.prepareStatement(teacherSql);
             PreparedStatement studentPs = con.prepareStatement(studentSql)) {
            rolePs.executeUpdate();
            adminPs.setString(1, defaultHash);
            adminPs.executeUpdate();
            teacherPs.setString(1, defaultHash);
            teacherPs.executeUpdate();
            studentPs.setString(1, defaultHash);
            studentPs.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to prepare default login accounts", e);
        }
    }
}
