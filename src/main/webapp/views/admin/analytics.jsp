<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Admin Analytics</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <!-- Fonts & Bootstrap -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Your CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">

    <!-- UI Enhancement -->
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: #f5f7fb;
        }

        .topbar {
            background: #fff;
            padding: 12px 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }

        .view-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.05);
            transition: 0.3s;
        }

        .view-card:hover {
            transform: translateY(-4px);
        }

        h6 {
            font-weight: 600;
            margin-bottom: 15px;
        }

        table th {
            font-weight: 600;
            color: #555;
        }

        table td {
            vertical-align: middle;
        }

        .table-modern tr:hover {
            background-color: #f1f5ff;
        }

        .badge-score {
            padding: 6px 10px;
            border-radius: 8px;
            font-size: 12px;
        }

        .high { background: #28a745; color: white; }
        .medium { background: #ffc107; color: black; }
        .low { background: #dc3545; color: white; }

    </style>
</head>

<body class="p-3 p-md-4">

<div class="container-fluid">

<!-- 🔷 TOPBAR -->
<div class="topbar mb-4 d-flex justify-content-between align-items-center">
    <h5 class="mb-0">📊 Analytics and Reports</h5>
    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/admin/panel?section=reports">Back</a>
</div>

<!-- 🔷 SUBJECT PERFORMANCE -->
<div class="view-card p-3 mb-4">
<h6>📚 Subject-wise Performance</h6>

<div class="table-responsive">
<table class="table table-modern table-hover mb-0">
    <tr>
        <th>Subject</th>
        <th>Attempts</th>
        <th>Average Score</th>
    </tr>

    <c:forEach var="s" items="${subjectWise}">
        <tr>
            <td>${s.subject_name}</td>
            <td>${s.attempts}</td>
            <td>
                <span class="badge-score 
                    ${s.avg_score >= 7 ? 'high' : 
                      s.avg_score >= 4 ? 'medium' : 'low'}">
                    ${s.avg_score}
                </span>
            </td>
        </tr>
    </c:forEach>
</table>
</div>
</div>

<!-- 🔷 TOP STUDENTS -->
<div class="view-card p-3 mb-4">
<h6>🏆 Top Students</h6>

<div class="table-responsive">
<table class="table table-modern table-hover mb-0">
    <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Total Score</th>
    </tr>

    <c:forEach var="t" items="${topStudents}">
        <tr>
            <td>${t.full_name}</td>
            <td>${t.email}</td>
            <td><span class="badge bg-primary">${t.total_score}</span></td>
        </tr>
    </c:forEach>
</table>
</div>
</div>

<!-- 🔷 PASS RATE -->
<div class="view-card p-3">
<h6>📈 Exam Pass Rate</h6>

<div class="table-responsive">
<table class="table table-modern table-hover mb-0">
    <tr>
        <th>Exam ID</th>
        <th>Title</th>
        <th>Total Attempts</th>
        <th>Pass Count</th>
    </tr>

    <c:forEach var="p" items="${passRate}">
        <tr>
            <td>${p.exam_id}</td>
            <td>${p.title}</td>
            <td>${p.total_attempts}</td>
            <td><span class="badge bg-success">${p.pass_count}</span></td>
        </tr>
    </c:forEach>
</table>
</div>
</div>

</div>
</body>
</html>