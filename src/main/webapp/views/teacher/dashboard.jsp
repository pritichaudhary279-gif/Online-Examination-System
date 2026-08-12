<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Teacher Dashboard</title>
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
            <div class="brand-title"><i class="bi bi-mortarboard-fill me-2"></i>Exam System</div>
            <span class="role-badge">${sessionScope.currentUser.roleName}</span>
        </div>
        <a class="active" href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-speedometer2"></i>Dashboard</a>
        <a href="${pageContext.request.contextPath}/teacher/exam/create"><i class="bi bi-journal-plus"></i>Create Exam</a>
        <a href="${pageContext.request.contextPath}/teacher/bank"><i class="bi bi-collection"></i>Question Bank</a>
        <a href="${pageContext.request.contextPath}/integrity"><i class="bi bi-shield-exclamation"></i>Integrity</a>
        <a href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right"></i>Logout</a>
    </aside>

    <main class="app-content">
        <div class="topbar d-flex justify-content-between align-items-center mb-4">
            <div>
                <h5 class="mb-0">Teacher Dashboard</h5>
                <small class="text-secondary">Manage courses, exams, and question workflows</small>
            </div>
            <button class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#createQuestionModal">
                <i class="bi bi-plus-circle me-1"></i>Create Question
            </button>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-lg-5">
                <div class="view-card p-3 h-100">
                    <h6 class="fw-semibold mb-3">Course Management</h6>
                    <div class="list-group">
                        <c:forEach var="s" items="${subjects}">
                            <div class="list-group-item border-0 border-bottom">
                                <div class="fw-semibold">${s.subject_name}</div>
                                <small class="text-secondary">${s.course_name}</small>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
            <div class="col-lg-7">
                <div class="view-card p-3 h-100">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6 class="fw-semibold mb-0">Active Exams Monitor</h6>
                        <span id="activeExamCount" class="badge text-bg-primary">0 Active</span>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-modern align-middle mb-0" id="teacherExamTable">
                            <thead>
                            <tr><th>Exam</th><th>Subject</th><th>Window / Pass</th><th>Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="ex" items="${teacherExams}">
                                <tr data-status="${ex.status}">
                                    <td>${ex.title}</td>
                                    <td>${ex.subject_name}</td>
                                    <td class="small">
                                        <c:choose>
                                            <c:when test="${ex.available_from == null && ex.available_until == null}">Any time</c:when>
                                            <c:otherwise>${ex.available_from} → ${ex.available_until}</c:otherwise>
                                        </c:choose>
                                        <br/>
                                        Pass:
                                        <c:choose>
                                            <c:when test="${ex.passing_marks != null}">${ex.passing_marks} marks</c:when>
                                            <c:otherwise>40% of total</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><span class="badge text-bg-light border">${ex.status}</span></td>
                                    <td>
                                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/teacher/exam/questions?examId=${ex.exam_id}">Questions</a>
                                        <form method="post" action="${pageContext.request.contextPath}/teacher/exam/status" class="d-inline">
                                            <input type="hidden" name="examId" value="${ex.exam_id}" />
                                            <input type="hidden" name="status" value="PUBLISHED" />
                                            <button class="btn btn-sm btn-outline-success" type="submit">Publish</button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/teacher/exam/status" class="d-inline">
                                            <input type="hidden" name="examId" value="${ex.exam_id}" />
                                            <input type="hidden" name="status" value="CLOSED" />
                                            <button class="btn btn-sm btn-outline-danger" type="submit">Close</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="view-card p-3">
            <h6 class="fw-semibold mb-3">Notifications</h6>
            <div class="table-responsive">
                <table class="table table-modern mb-0">
                    <thead><tr><th>Title</th><th>Message</th><th>Created</th></tr></thead>
                    <tbody>
                    <c:forEach var="n" items="${userNotifications}">
                        <tr>
                            <td>${n.title}</td>
                            <td>${n.message_body}</td>
                            <td>${n.created_at}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<div class="modal fade" id="createQuestionModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title">Launch Create Question</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <label class="form-label">Enter Exam ID</label>
                <input type="number" id="questionExamId" class="form-control" placeholder="e.g. 1">
                <small class="text-secondary">This opens the existing question management screen.</small>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" type="button" onclick="openQuestionManager()">Open</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const rows = document.querySelectorAll("#teacherExamTable tbody tr");
        let count = 0;
        rows.forEach(r => {
            if ((r.dataset.status || "").toUpperCase() === "PUBLISHED") count++;
        });
        document.getElementById("activeExamCount").textContent = count + " Active";
    })();

    function openQuestionManager() {
        const examId = document.getElementById("questionExamId").value;
        if (!examId) return;
        window.location.href = "${pageContext.request.contextPath}/teacher/exam/questions?examId=" + examId;
    }
</script>
</body>
</html>
