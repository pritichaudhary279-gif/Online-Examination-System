<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Exam roster</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
</head>
<body class="p-3 p-md-4">
<div class="container-fluid">
    <div class="topbar mb-3 d-flex justify-content-between align-items-center">
        <div>
            <h5 class="mb-0">Student completion — Exam #${examId}</h5>
            <small class="text-secondary">${examMeta.title}</small>
        </div>
        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/dashboard">Back</a>
    </div>

    <div class="view-card p-3 mb-3">
        <div class="d-flex flex-wrap gap-2">
            <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/teacher/reports/export?examId=${examId}">Export CSV</a>
            <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/teacher/reports/export?examId=${examId}&format=pdf">Export PDF</a>
            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/teacher/exam/questions?examId=${examId}">Questions</a>
        </div>
    </div>

    <div class="view-card p-3">
        <div class="table-responsive">
            <table class="table table-modern align-middle mb-0">
                <thead>
                <tr>
                    <th>Student</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Score</th>
                    <th>Submitted</th>
                    <th>Teacher feedback</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="row" items="${roster}">
                    <tr>
                        <td>${row.full_name}</td>
                        <td>${row.email}</td>
                        <td><span class="badge text-bg-light border">${row.attempt_status}</span></td>
                        <td>${row.score_obtained != null ? row.score_obtained : '—'}</td>
                        <td class="small">${row.submitted_at != null ? row.submitted_at : '—'}</td>
                        <td style="min-width: 220px;">
                            <c:choose>
                                <c:when test="${row.result_id != null}">
                                    <form method="post" action="${pageContext.request.contextPath}/teacher/exam/roster" class="d-flex flex-column gap-1">
                                        <input type="hidden" name="examId" value="${examId}"/>
                                        <input type="hidden" name="resultId" value="${row.result_id}"/>
                                        <textarea class="form-control form-control-sm" name="feedback" rows="2" placeholder="e.g. Good job, focus on Chapter 3">${row.teacher_feedback}</textarea>
                                        <button class="btn btn-sm btn-primary" type="submit">Save feedback</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-secondary small">No attempt yet</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
