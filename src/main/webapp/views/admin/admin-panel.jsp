<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Panel</title>
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
            <div class="brand-title"><i class="bi bi-mortarboard-fill me-2"></i>Admin</div>
            <span class="role-badge">${sessionScope.currentUser.roleName}</span>
        </div>
        <a class="${section == 'students' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/panel?section=students">
            <i class="bi bi-people"></i>Students
        </a>
        <a class="${section == 'exams' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/panel?section=exams">
            <i class="bi bi-journal-text"></i>Exams
        </a>
        <a class="${section == 'results' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/panel?section=results">
            <i class="bi bi-clipboard-data"></i>Results
        </a>
        <a class="${section == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/panel?section=reports">
            <i class="bi bi-file-earmark-bar-graph"></i>Reports
        </a>
        <a href="${pageContext.request.contextPath}/admin/manage"><i class="bi bi-journal-plus"></i>Courses &amp; subjects</a>
        <a href="${pageContext.request.contextPath}/integrity"><i class="bi bi-shield-exclamation"></i>Integrity</a>
        <a href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right"></i>Logout</a>
    </aside>
    <main class="app-content">
        <div class="topbar d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
                <h5 class="mb-0">Administration</h5>
                <small class="text-secondary">Manage students, exams, and outcomes</small>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <span class="badge text-bg-secondary">Users: ${stats.users}</span>
                <span class="badge text-bg-secondary">Exams: ${stats.exams}</span>
            </div>
        </div>

        <p class="text-danger">${error}</p>

        <c:choose>
            <c:when test="${section == 'students'}">
                <div class="row g-3 mb-4">
                    <div class="col-lg-5">
                        <div class="view-card p-3 h-100">
                            <h6 class="fw-semibold mb-3">Add student</h6>
                            <form method="post" action="${pageContext.request.contextPath}/admin/panel" class="row g-2">
                                <input type="hidden" name="action" value="addStudent"/>
                                <div class="col-12">
                                    <label class="form-label small mb-0">Full name</label>
                                    <input class="form-control" type="text" name="fullName" required/>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small mb-0">Student ID / roll</label>
                                    <input class="form-control" type="text" name="studentCode" placeholder="Optional"/>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small mb-0">Course</label>
                                    <c:choose>
                                        <c:when test="${empty courses}">
                                            <p class="text-danger small mb-0">No courses in the system. Add a course first (Reports → setup).</p>
                                        </c:when>
                                        <c:otherwise>
                                            <select class="form-select" name="courseId" required>
                                                <c:forEach var="c" items="${courses}">
                                                    <option value="${c.course_id}">${c.course_name}</option>
                                                </c:forEach>
                                            </select>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small mb-0">Username (login)</label>
                                    <input class="form-control" type="text" name="username" placeholder="Unique username or email" required/>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small mb-0">Password</label>
                                    <input class="form-control" type="password" name="password" required/>
                                </div>
                                <div class="col-12 mt-2">
                                    <button class="btn btn-primary w-100" type="submit" ${empty courses ? 'disabled' : ''}>Save student</button>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="col-lg-7">
                        <div class="view-card p-3 h-100">
                            <h6 class="fw-semibold mb-3">All students</h6>
                            <div class="table-responsive">
                                <table class="table table-modern align-middle mb-0">
                                    <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Student ID</th>
                                        <th>User ID</th>
                                        <th>Username</th>
                                        <th>Course(s)</th>
                                        <th>Exams taken</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="s" items="${studentsOverview}">
                                        <tr>
                                            <td>${s.full_name}</td>
                                            <td>${empty s.student_code ? '—' : s.student_code}</td>
                                            <td>${s.user_id}</td>
                                            <td>${s.username}</td>
                                            <td>${empty s.enrolled_courses ? '—' : s.enrolled_courses}</td>
                                            <td class="small">${empty s.exams_taken ? '—' : s.exams_taken}</td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <c:when test="${section == 'exams'}">
                <div class="view-card p-3 mb-4">
                    <h6 class="fw-semibold mb-3">Exams created by teachers</h6>
                    <div class="table-responsive">
                        <table class="table table-modern align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Exam</th>
                                <th>Teacher</th>
                                <th>Course</th>
                                <th>Subject</th>
                                <th>Duration</th>
                                <th>Total marks</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="ex" items="${teacherExams}">
                                <tr>
                                    <td>${ex.title}</td>
                                    <td>${ex.teacher_name}</td>
                                    <td>${ex.course_name}</td>
                                    <td>${ex.subject_name}</td>
                                    <td>${ex.duration_minutes} min</td>
                                    <td>${ex.total_marks}</td>
                                    <td><span class="badge text-bg-light border">${ex.status}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>

            <c:when test="${section == 'results'}">
                <div class="view-card p-3 mb-4">
                    <h6 class="fw-semibold mb-3">Results by subject</h6>
                    <div class="table-responsive">
                        <table class="table table-modern align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Student</th>
                                <th>Subject</th>
                                <th>Exam</th>
                                <th>Score</th>
                                <th>Out of</th>
                                <th>Percentage</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="r" items="${subjectWiseResults}">
                                <tr>
                                    <td>${r.student_name}</td>
                                    <td>${r.subject_name}</td>
                                    <td>${r.exam_title}</td>
                                    <td>${r.score}</td>
                                    <td>${r.out_of}</td>
                                    <td>${r.pct == null ? '—' : r.pct}</td>
                                    <td><span class="badge text-bg-light border">${r.status}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>

            <c:when test="${section == 'reports'}">
                <div class="view-card p-3 mb-4">
                    <h6 class="fw-semibold mb-3">Exports</h6>
                    <p class="text-secondary small mb-3">Download the same subject-wise results table as CSV or PDF.</p>
                    <div class="d-flex flex-wrap gap-2">
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/export?format=csv">
                            <i class="bi bi-filetype-csv me-1"></i>Export CSV
                        </a>
                        <a class="btn btn-outline-danger" href="${pageContext.request.contextPath}/admin/export?format=pdf">
                            <i class="bi bi-filetype-pdf me-1"></i>Export PDF
                        </a>
                    </div>
                </div>
                <div class="view-card p-3 mb-4">
                    <h6 class="fw-semibold mb-3">More tools</h6>
                    <div class="d-flex flex-wrap gap-2">
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/analytics">Analytics</a>
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/manage">Courses, subjects &amp; notifications</a>
                    </div>
                </div>
            </c:when>
        </c:choose>
    </main>
</div>
</body>
</html>
