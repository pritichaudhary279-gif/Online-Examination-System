package com.oes.model;

import java.util.ArrayList;
import java.util.List;

public class ExamQuestion {
    private long questionId;
    private String questionText;
    private String questionType;
    private int marks;
    private List<QuestionOption> options = new ArrayList<>();

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }
}
