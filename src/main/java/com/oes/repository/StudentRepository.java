package com.oes.repository;

import com.oes.model.ExamQuestion;
import com.oes.model.ResultView;
import com.oes.model.StudentExamView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data access for student exam lifecycle (Repository layer).
 */
public interface StudentRepository {

    List<StudentExamView> findAvailableExams(long studentId);

    int findExamTotalMarks(long examId);

    /**
     * Absolute passing marks set by teacher; null means use default percentage rule in service.
     */
    Integer findExamPassingMarks(long examId);

    List<ExamQuestion> findQuestionsByExam(long examId);

    long ensureInProgressResult(long examId, long studentId);

    boolean hasEvaluatedAttempt(long examId, long studentId);

    Map<Long, String> findSavedAnswers(long examId, long studentId);

    void replaceAnswersForResult(long resultId, long examId, Map<Long, String> answerMap);

    BigDecimal finalizeExamGrading(long examId, long studentId, long resultId, Map<Long, String> answerMap);

    void insertProctoringLog(long resultId, long examId, long studentId, String eventType, String severity);

    List<ResultView> findResultsByStudent(long studentId);
}
