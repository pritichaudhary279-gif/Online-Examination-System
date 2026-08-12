<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
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
        <a href="${pageContext.request.contextPath}/admin/panel?section=students"><i class="bi bi-people"></i>Admin panel</a>
        <a href="${pageContext.request.contextPath}/admin/analytics"><i class="bi bi-bar-chart-line"></i>Analytics</a>
        <a href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right"></i>Logout</a>
    </aside>
    <main class="app-content">
        <div class="topbar d-flex justify-content-between align-items-center mb-4">
            <div>
                <h5 class="mb-0">Admin Control Center</h5>
                <small class="text-secondary">System-level visibility and action controls</small>
            </div>
            <div class="text-end">
                <small class="text-secondary d-block">System Health</small>
                <span class="badge text-bg-primary">Healthy</span>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Total Users</div>
                    <div class="stat-value">${stats.users}</div>
                    <a class="btn btn-sm btn-outline-primary mt-2" href="${pageContext.request.contextPath}/admin/panel?section=students">Open admin panel</a>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Total Exams</div>
                    <div class="stat-value">${stats.exams}</div>
                    <a class="btn btn-sm btn-outline-primary mt-2" href="${pageContext.request.contextPath}/admin/analytics">Review Performance</a>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Active Users</div>
                    <div class="stat-value">${stats.active_users}</div>
                    <div class="text-success small mt-2"><i class="bi bi-check-circle-fill"></i> Services operational</div>
                </div>
            </div>
        </div>

        <div class="view-card p-3">
            <h6 class="fw-semibold mb-3">Recent Activity</h6>
            <div class="table-responsive">
                <table class="table table-modern align-middle mb-0">
                    <thead>
                    <tr>
                        <th>Student</th>
                        <th>Subject</th>
                        <th>Exam</th>
                        <th>Score</th>
                        <th>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${recentResults}">
                        <tr>
                            <td>${r.full_name}</td>
                            <td>${r.subject_name}</td>
                            <td>${r.title}</td>
                            <td>${r.score_obtained}</td>
                            <td><span class="badge text-bg-light border">${r.status}</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>
</body>
</html>
