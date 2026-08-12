package com.oes.service;

public class ProctoringService {
    public String normalizeSeverity(String eventType) {
        if (eventType == null) {
            return "LOW";
        }
        String e = eventType.toUpperCase();
        if (e.contains("TAB_SWITCH") || e.contains("WINDOW_BLUR") || e.contains("PAGE_HIDE")) {
            return "MEDIUM";
        }
        if (e.contains("MULTI_FACE") || e.contains("NO_FACE") || e.contains("SCREEN_SHARE")) {
            return "HIGH";
        }
        return "LOW";
    }
}
