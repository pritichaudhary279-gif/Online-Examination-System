package com.oes.model;

/**
 * Recipients and labels for exam-related emails (loaded from DB).
 */
public class ExamNotificationContext {

    private String examTitle;
    private String studentName;
    private String studentEmail;
    private String teacherName;
    private String teacherEmail;

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public boolean hasStudentEmail() {
        return studentEmail != null && !studentEmail.isBlank();
    }

    public boolean hasTeacherEmail() {
        return teacherEmail != null && !teacherEmail.isBlank();
    }
}
