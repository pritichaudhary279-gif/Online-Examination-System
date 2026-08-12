<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Exam integrity</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <aside class="app-sidebar">
        <div class="mb-4">
            <div class="brand-title"><i class="bi bi-shield-exclamation me-2"></i>Integrity</div>
            <span class="role-badge">${sessionScope.currentUser.roleName}</span>
        </div>
        <c:choose>
            <c:when test="${isAdmin}">
                <a href="${pageContext.request.contextPath}/admin/panel?section=students"><i class="bi bi-arrow-left"></i>Admin panel</a>
                <a href="${pageContext.request.contextPath}/admin/manage"><i class="bi bi-journal-plus"></i>Courses &amp; subjects</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-arrow-left"></i>Teacher dashboard</a>
            </c:otherwise>
        </c:choose>
        <a class="active" href="${pageContext.request.contextPath}/integrity"><i class="bi bi-shield-exclamation"></i>Integrity log</a>
        <a href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right"></i>Logout</a>
    </aside>
    <main class="app-content">
        <div class="topbar mb-4">
            <h5 class="mb-1">Exam integrity dashboard</h5>
            <small class="text-secondary d-block">
                See which students switched away from the exam tab, blurred the exam window, or left the page—and how many times—while an attempt is in progress.
                <c:choose>
                    <c:when test="${isAdmin}">You are viewing all teachers' exams.</c:when>
                    <c:otherwise>You are viewing only exams you created.</c:otherwise>
                </c:choose>
            </small>
            <c:if test="${not empty integrityError}">
                <div class="alert alert-danger mt-3 mb-0 small">${integrityError}</div>
            </c:if>
        </div>

        <c:if test="${empty integrityError and rawLogCount == 0}">
            <div class="alert alert-info small mb-4">
                <strong>No activity in the log yet.</strong> Rows appear after a student opens a live exam and the browser reports a tab change, window blur, or leaving the page.
                Use another tab or click outside the browser while taking a practice attempt to verify logging.
            </div>
        </c:if>
        <c:if test="${empty integrityError and rawLogCount > 0 and empty summaryRows}">
            <div class="alert alert-warning small mb-4">
                There are <strong>${rawLogCount}</strong> row(s) in <code>proctoring_logs</code>, but none could be matched to a student and exam.
                Check that <code>exam_id</code> / <code>student_id</code> are populated and that matching users and exams exist.
            </div>
        </c:if>

        <div class="view-card p-3 mb-4">
            <h6 class="fw-semibold mb-1">By student and exam</h6>
            <p class="small text-secondary mb-3">Counts of distraction events per student for each exam attempt context.</p>
            <c:choose>
                <c:when test="${empty summaryRows}">
                    <p class="text-secondary mb-0">No matched distraction events to show.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Student</th>
                                <th>Email</th>
                                <th>Exam</th>
                                <th>Course / subject</th>
                                <th class="text-end" title="Switched to another tab or hid this tab">Tab away</th>
                                <th class="text-end" title="Window lost focus (e.g. clicked outside)">Left window</th>
                                <th class="text-end" title="Navigated away or closed the tab">Left page</th>
                                <th class="text-end">Total</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${summaryRows}">
                                <tr>
                                    <td>${row.student_name}</td>
                                    <td class="small text-secondary">${row.email}</td>
                                    <td>${row.exam_title}</td>
                                    <td class="small">${row.course_name} · ${row.subject_name}</td>
                                    <td class="text-end">${row.tab_switches}</td>
                                    <td class="text-end">${row.window_blurs}</td>
                                    <td class="text-end">${row.page_hides}</td>
                                    <td class="text-end fw-semibold">${row.total_violations}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="view-card p-3">
            <h6 class="fw-semibold mb-1">Recent events (newest first)</h6>
            <p class="small text-secondary mb-3">Each row is one recorded distraction with time, student, and plain-language description.</p>
            <c:choose>
                <c:when test="${empty recentRows}">
                    <p class="text-secondary mb-0">No recent events.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Time</th>
                                <th>Student</th>
                                <th>Exam</th>
                                <th>What happened</th>
                                <th>Severity</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="ev" items="${recentRows}">
                                <tr>
                                    <td class="text-nowrap small">
                                        <fmt:formatDate value="${ev.logged_at}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                    </td>
                                    <td>${ev.student_name}</td>
                                    <td class="small">${ev.exam_title}</td>
                                    <td>${ev.event_label}</td>
                                    <td><span class="badge text-bg-secondary">${ev.severity}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
