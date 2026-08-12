<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Student Dashboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <!-- Fonts & Bootstrap -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: #f5f7fb;
        }

        .app-sidebar {
            background: #1e293b;
            color: #fff;
            min-height: 100vh;
            padding: 20px;
        }

        .app-sidebar a {
            display: block;
            padding: 10px;
            border-radius: 8px;
            color: #cbd5e1;
            text-decoration: none;
            margin-bottom: 8px;
            transition: 0.3s;
        }

        .app-sidebar a:hover,
        .app-sidebar a.active {
            background: #3b82f6;
            color: #fff;
        }

        .app-content {
            padding: 20px;
        }

        .topbar {
            background: #fff;
            padding: 15px 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }

        .view-card, .countdown-box, .stat-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.05);
            transition: 0.3s;
        }

        .view-card:hover, .stat-card:hover, .countdown-box:hover {
            transform: translateY(-4px);
        }

        .stat-card {
            padding: 15px;
        }

        .btn {
            border-radius: 10px;
        }

        .progress-track {
            height: 8px;
            background: #e2e8f0;
            border-radius: 10px;
            overflow: hidden;
        }

        .progress-track span {
            display: block;
            height: 100%;
            border-radius: 10px;
        }

        .table-modern tr:hover {
            background-color: #f1f5ff;
        }

        .badge-status {
            padding: 5px 10px;
            border-radius: 8px;
        }

        .status-evaluated {
            background: #28a745;
            color: #fff;
        }

        .status-pending {
            background: #ffc107;
            color: #000;
        }
    </style>
</head>

<body>

<c:set var="completedCount" value="0"/>
<c:forEach var="r" items="${results}">
    <c:if test="${r.status == 'EVALUATED'}">
        <c:set var="completedCount" value="${completedCount + 1}"/>
    </c:if>
</c:forEach>

<c:set var="totalCount" value="${exams.size()}"/>

<div class="app-shell">

<!-- 🔷 SIDEBAR -->
<aside class="app-sidebar">
    <div class="mb-4">
        <div class="brand-title"><i class="bi bi-mortarboard-fill me-2"></i>Exam System</div>
        <span class="badge bg-primary">${sessionScope.currentUser.roleName}</span>
    </div>

    <a class="active" href="${pageContext.request.contextPath}/dashboard">
        <i class="bi bi-speedometer2"></i> Dashboard
    </a>

    <a href="${pageContext.request.contextPath}/logout">
        <i class="bi bi-box-arrow-right"></i> Logout
    </a>
</aside>

<!-- 🔷 MAIN -->
<main class="app-content">

<div class="topbar d-flex justify-content-between align-items-center mb-4">
    <div>
        <h5 class="mb-0">📊 Student Dashboard</h5>
        <small class="text-secondary">Track upcoming exams and performance</small>
    </div>
    <span class="badge bg-primary px-3 py-2">${completedCount} Completed</span>
</div>

<p class="text-danger">${error}</p>

<!-- 🔷 TOP CARDS -->
<div class="row g-3 mb-4">

<div class="col-lg-4">
    <div class="countdown-box p-3 h-100">
        <h6>⏳ Next Exam</h6>
        <div class="small text-secondary mb-2">
            <c:choose>
                <c:when test="${not empty exams}">${exams[0].examTitle}</c:when>
                <c:otherwise>No scheduled exams</c:otherwise>
            </c:choose>
        </div>
        <div class="h3 text-primary fw-bold" id="nextExamTimer">--:--:--</div>
    </div>
</div>

<div class="col-lg-8">
    <div class="view-card p-3 h-100">
        <h6>📈 Progress Tracker</h6>

        <div class="mb-2 small text-secondary">Learning Progress</div>
        <div class="progress-track mb-2"><span style="width: 100%; background:#3b82f6;"></span></div>

        <div class="mb-2 small text-secondary">Exam Completion</div>
        <div class="progress-track mb-2">
            <span style="width: ${totalCount == 0 ? 0 : (completedCount * 100 / totalCount)}%; background:#0ea5e9;"></span>
        </div>

        <div class="mb-2 small text-secondary">Practice Readiness</div>
        <div class="progress-track">
            <span style="width: 75%; background:#6366f1;"></span>
        </div>
    </div>
</div>

</div>

<!-- 🔷 AVAILABLE TESTS -->
<div class="view-card p-3 mb-4">
<h6>📝 Available Tests</h6>

<div class="row g-3">
<c:forEach var="e" items="${exams}">
    <div class="col-md-6 col-xl-4">
        <div class="stat-card h-100">
            <div class="small text-secondary">${e.courseName} - ${e.subjectName}</div>
            <div class="fw-semibold mt-1">${e.examTitle}</div>
            <div class="small text-secondary mb-3">${e.durationMinutes} mins</div>

            <a class="btn btn-sm btn-primary w-100"
               href="${pageContext.request.contextPath}/student/exam?examId=${e.examId}&duration=${e.durationMinutes}">
               ▶ Start Test
            </a>
        </div>
    </div>
</c:forEach>
</div>
</div>

<!-- 🔷 RESULTS -->
<div class="view-card p-3 mb-4">
<h6>📊 Result Panel</h6>

<div class="table-responsive">
<table class="table table-hover table-striped mb-0">

<thead>
<tr>
<th>Subject</th><th>Exam</th><th>Score</th><th>Out of</th><th>%</th><th>Status</th><th>Feedback</th>
</tr>
</thead>

<tbody>
<c:forEach var="r" items="${results}">
<tr>
    <td>${r.subjectName}</td>
    <td>${r.examTitle}</td>
    <td>${r.score}</td>
    <td>${r.totalMarks == null ? '—' : r.totalMarks}</td>
    <td>${r.percentage == null ? '—' : r.percentage}</td>

    <td>
        <span class="badge-status 
            ${r.status == 'EVALUATED' ? 'status-evaluated' : 'status-pending'}">
            ${r.status}
        </span>
    </td>

    <td class="small">${empty r.teacherFeedback ? '—' : r.teacherFeedback}</td>
</tr>
</c:forEach>
</tbody>

</table>
</div>
</div>

</main>
</div>

<!-- 🔷 TIMER SCRIPT (UNCHANGED) -->
<script>
(function () {
    const timerEl = document.getElementById("nextExamTimer");
    let target = Date.now() + (90 * 60 * 1000);

    <c:if test="${not empty exams}">
    target = Date.now() + (${exams[0].durationMinutes} * 60 * 1000);
    </c:if>

    function render() {
        const diff = Math.max(0, target - Date.now());
        const h = Math.floor(diff / 3600000);
        const m = Math.floor((diff % 3600000) / 60000);
        const s = Math.floor((diff % 60000) / 1000);

        timerEl.textContent =
            h.toString().padStart(2, "0") + ":" +
            m.toString().padStart(2, "0") + ":" +
            s.toString().padStart(2, "0");
    }

    render();
    setInterval(render, 1000);
})();
</script>

</body>
</html>