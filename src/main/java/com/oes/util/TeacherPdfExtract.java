package com.oes.util;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Best-effort PDF text extraction for bulk question import (one question per line, pipe format).
 */
public final class TeacherPdfExtract {

    private TeacherPdfExtract() {
    }

    public static String extractText(InputStream in) throws IOException {
        byte[] data = in.readAllBytes();
        PdfReader reader = new PdfReader(data);
        try {
            StringBuilder sb = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int p = 1; p <= reader.getNumberOfPages(); p++) {
                sb.append(extractor.getTextFromPage(p));
                sb.append('\n');
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
}
