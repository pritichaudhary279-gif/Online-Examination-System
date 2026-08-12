package com.oes.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses bulk question lines (pipe-separated preferred; simple CSV fallback).
 * <p>
 * Pipe format: TYPE|question text|marks|opt1|opt2|opt3|opt4|correctIndex|keywords
 * correctIndex: 1-4 for MCQ (required). For SUBJECTIVE leave options empty, keywords last.
 */
public final class TeacherBulkImportUtil {

    private TeacherBulkImportUtil() {
    }

    public static class ParsedQuestion {
        public String type;
        public String questionText;
        public int marks;
        public final List<String> options = new ArrayList<>();
        public int correctIndex1Based;
        public String keywords;
        public String modelAnswer;
    }

    public static ParsedQuestion parseLine(String raw) {
        if (raw == null) {
            return null;
        }
        String line = raw.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }
        if (line.toUpperCase().startsWith("TYPE")) {
            return null;
        }
        ParsedQuestion q = new ParsedQuestion();
        if (line.contains("|")) {
            String[] p = line.split("\\|", -1);
            if (p.length < 3) {
                return null;
            }
            q.type = p[0].trim().toUpperCase();
            q.questionText = p[1].trim();
            q.marks = parseIntSafe(p[2], 1);
            if ("MCQ".equals(q.type)) {
                for (int i = 3; i <= 6 && i < p.length; i++) {
                    String o = p[i].trim();
                    if (!o.isEmpty()) {
                        q.options.add(o);
                    }
                }
                if (p.length > 7) {
                    q.correctIndex1Based = parseIntSafe(p[7], 1);
                }
            } else {
                if (p.length > 7) {
                    q.keywords = p[7].trim();
                }
                if (p.length > 8) {
                    q.modelAnswer = p[8].trim();
                }
            }
        } else {
            List<String> cells = splitCsv(line);
            if (cells.size() < 3) {
                return null;
            }
            q.type = cells.get(0).trim().toUpperCase();
            q.questionText = cells.get(1).trim();
            q.marks = parseIntSafe(cells.get(2), 1);
            if ("MCQ".equals(q.type) && cells.size() >= 8) {
                for (int i = 3; i <= 6; i++) {
                    String o = cells.get(i).trim();
                    if (!o.isEmpty()) {
                        q.options.add(o);
                    }
                }
                q.correctIndex1Based = parseIntSafe(cells.get(7), 1);
            } else if (cells.size() > 7) {
                q.keywords = cells.get(7).trim();
            }
        }
        if (!"MCQ".equals(q.type) && !"SUBJECTIVE".equals(q.type)) {
            return null;
        }
        if (q.questionText.isEmpty()) {
            return null;
        }
        return q;
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static List<String> splitCsv(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQ = !inQ;
                continue;
            }
            if (c == ',' && !inQ) {
                cells.add(cur.toString().trim());
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        cells.add(cur.toString().trim());
        return cells;
    }
}
