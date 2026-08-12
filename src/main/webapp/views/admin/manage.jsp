<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Management</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <!-- Fonts & Bootstrap -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">

    <!-- Your CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: #f5f7fb;
        }

        .topbar {
            background: #fff;
            padding: 15px 20px;
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

        .form-control, .form-select {
            border-radius: 10px;
        }

        .btn {
            border-radius: 10px;
        }

        .table-modern tr:hover {
            background-color: #f1f5ff;
        }

        .badge-active {
            background: #28a745;
            color: #fff;
            padding: 5px 10px;
            border-radius: 8px;
        }

        .badge-inactive {
            background: #dc3545;
            color: #fff;
            padding: 5px 10px;
            border-radius: 8px;
        }
    </style>
</head>

<body class="p-3 p-md-4">

<div class="container-fluid">

<!-- 🔷 TOPBAR -->
<div class="topbar mb-4 d-flex justify-content-between align-items-center">
    <div>
        <h5 class="mb-0">⚙️ Admin Management Panel</h5>
        <small class="text-secondary">Manage users, courses, subjects, and notifications</small>
    </div>
    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/admin/panel?section=reports">
        <i class="bi bi-arrow-left"></i> Back
    </a>
</div>

<p class="text-danger">${error}</p>

<!-- 🔷 FORMS SECTION -->
<div class="row g-4">

<!-- CREATE USER -->
<div class="col-lg-6">
<div class="view-card p-4">
<h6><i class="bi bi-person-plus"></i> Create User</h6>

<form class="row g-3" method="post" action="${pageContext.request.contextPath}/admin/manage">
    <input type="hidden" name="action" value="createUser"/>

    <div class="col-md-4">
        <select class="form-select" name="roleId">
            <c:forEach var="r" items="${roles}">
                <option value="${r.role_id}">${r.role_name}</option>
            </c:forEach>
        </select>
    </div>

    <div class="col-md-4">
        <input class="form-control" type="text" name="fullName" placeholder="Full Name" required/>
    </div>

    <div class="col-md-4">
        <input class="form-control" type="email" name="email" placeholder="Email" required/>
    </div>

    <div class="col-md-8">
        <input class="form-control" type="password" name="password" placeholder="Password" required/>
    </div>

    <div class="col-md-4">
        <button class="btn btn-primary w-100">
            <i class="bi bi-check-circle"></i> Create
        </button>
    </div>
</form>
</div>
</div>

<!-- COURSE / SUBJECT / NOTIFICATION -->
<div class="col-lg-6">
<div class="view-card p-4">

<h6><i class="bi bi-journal-plus"></i> Create Course</h6>
<form class="row g-3" method="post" action="${pageContext.request.contextPath}/admin/manage">
    <input type="hidden" name="action" value="createCourse"/>
    <div class="col-md-5"><input class="form-control" type="text" name="courseName" placeholder="Course Name" required/></div>
    <div class="col-md-4"><input class="form-control" type="text" name="description" placeholder="Description"/></div>
    <div class="col-md-3"><button class="btn btn-primary w-100">Create</button></div>
</form>

<h6 class="mt-4"><i class="bi bi-book"></i> Create Subject</h6>
<form class="row g-3" method="post" action="${pageContext.request.contextPath}/admin/manage">
    <input type="hidden" name="action" value="createSubject"/>
    <div class="col-md-5">
        <select class="form-select" name="courseId">
            <c:forEach var="c" items="${courses}">
                <option value="${c.course_id}">${c.course_name}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-4"><input class="form-control" type="text" name="subjectName" placeholder="Subject Name" required/></div>
    <div class="col-md-3"><button class="btn btn-primary w-100">Create</button></div>
</form>

<h6 class="mt-4"><i class="bi bi-bell"></i> Create Notification</h6>
<form class="row g-3" method="post" action="${pageContext.request.contextPath}/admin/manage">
    <input type="hidden" name="action" value="createNotification"/>
    <div class="col-md-3"><input class="form-control" type="text" name="title" placeholder="Title" required/></div>
    <div class="col-md-6"><input class="form-control" type="text" name="message" placeholder="Message" required/></div>
    <div class="col-md-3"><button class="btn btn-primary w-100">Send</button></div>
</form>

</div>
</div>
</div>

<!-- 🔷 USERS -->
<div class="view-card p-4 mt-4">
<h6>👥 Users</h6>
<div class="table-responsive">
<table class="table table-hover table-striped mb-0">
<tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th></tr>

<c:forEach var="u" items="${users}">
<tr>
    <td>${u.user_id}</td>
    <td>${u.full_name}</td>
    <td>${u.email}</td>
    <td>${u.role_name}</td>
    <td>
        <span class="${u.is_active ? 'badge-active' : 'badge-inactive'}">
            ${u.is_active}
        </span>
    </td>
</tr>
</c:forEach>

</table>
</div>
</div>

<!-- 🔷 COURSES -->
<div class="view-card p-4 mt-4">
<h6>📚 Courses</h6>
<div class="table-responsive">
<table class="table table-hover table-striped mb-0">
<tr><th>ID</th><th>Name</th><th>Description</th></tr>

<c:forEach var="c" items="${courses}">
<tr>
    <td>${c.course_id}</td>
    <td>${c.course_name}</td>
    <td>${c.description}</td>
</tr>
</c:forEach>

</table>
</div>
</div>

<!-- 🔷 SUBJECTS -->
<div class="view-card p-4 mt-4">
<h6>📖 Subjects</h6>
<div class="table-responsive">
<table class="table table-hover table-striped mb-0">
<tr><th>ID</th><th>Course</th><th>Subject</th></tr>

<c:forEach var="s" items="${subjects}">
<tr>
    <td>${s.subject_id}</td>
    <td>${s.course_name}</td>
    <td>${s.subject_name}</td>
</tr>
</c:forEach>

</table>
</div>
</div>

<!-- 🔷 NOTIFICATIONS -->
<div class="view-card p-4 mt-4">
<h6>🔔 Recent Notifications</h6>
<div class="table-responsive">
<table class="table table-hover table-striped mb-0">
<tr><th>ID</th><th>Title</th><th>Message</th><th>Created</th></tr>

<c:forEach var="n" items="${notifications}">
<tr>
    <td>${n.notification_id}</td>
    <td>${n.title}</td>
    <td>${n.message_body}</td>
    <td>${n.created_at}</td>
</tr>
</c:forEach>

</table>
</div>
</div>

</div>
</body>
</html>