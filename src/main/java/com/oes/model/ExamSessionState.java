package com.oes.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side exam attempt state (one question at a time).
 */
public class ExamSessionState implements Serializable {
    private static final long serialVersionUID = 1L;

    private long examId;
    private int durationMinutes;
    private List<ExamQuestion> questions;
    private int currentIndex;
    private final Map<Long, String> answers = new HashMap<>();
    private long serverEndTimeEpochMs;

    public long getExamId() {
        return examId;
    }

    public void setExamId(long examId) {
        this.examId = examId;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<ExamQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestion> questions) {
        this.questions = questions;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public long getServerEndTimeEpochMs() {
        return serverEndTimeEpochMs;
    }

    public void setServerEndTimeEpochMs(long serverEndTimeEpochMs) {
        this.serverEndTimeEpochMs = serverEndTimeEpochMs;
    }

    public int getTotalQuestions() {
        return questions == null ? 0 : questions.size();
    }
}
