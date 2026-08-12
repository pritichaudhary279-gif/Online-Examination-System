package com.oes.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExamResultSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal scoreObtained;
    private int totalMarks;
    private boolean passed;
    private String message;
    private String passThresholdDescription;

    public BigDecimal getScoreObtained() {
        return scoreObtained;
    }

    public void setScoreObtained(BigDecimal scoreObtained) {
        this.scoreObtained = scoreObtained;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPassThresholdDescription() {
        return passThresholdDescription;
    }

    public void setPassThresholdDescription(String passThresholdDescription) {
        this.passThresholdDescription = passThresholdDescription;
    }
}
