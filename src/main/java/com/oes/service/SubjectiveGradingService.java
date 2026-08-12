package com.oes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SubjectiveGradingService {
    public BigDecimal evaluateByKeywords(String answer, String expectedKeywords, int maxMarks) {
        if (answer == null || answer.isBlank() || expectedKeywords == null || expectedKeywords.isBlank() || maxMarks <= 0) {
            return BigDecimal.ZERO;
        }
        String normalized = answer.toLowerCase();
        String[] keys = expectedKeywords.toLowerCase().split(",");
        int total = 0;
        int matched = 0;
        for (String k : keys) {
            String key = k.trim();
            if (key.isEmpty()) {
                continue;
            }
            total++;
            if (normalized.contains(key)) {
                matched++;
            }
        }
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = BigDecimal.valueOf(matched).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(maxMarks).multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }
}
