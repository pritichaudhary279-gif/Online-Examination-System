<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Create Exam</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
</head>
<body class="p-3 p-md-4">
<div class="container">
    <div class="topbar mb-3 d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Create Exam</h5>
        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/dashboard">Back</a>
    </div>
    <div class="view-card p-4">
<form class="row g-3" method="post" action="${pageContext.request.contextPath}/teacher/exam/create">
    <div class="col-12">
    <label class="form-label">Subject</label>
    <select class="form-select" name="subjectId" required>
        <c:forEach var="s" items="${subjects}">
            <option value="${s.subject_id}">${s.course_name} - ${s.subject_name}</option>
        </c:forEach>
    </select></div>
    <div class="col-md-6"><label class="form-label">Exam Title</label><input class="form-control" type="text" name="title" required /></div>
    <div class="col-md-3"><label class="form-label">Duration (min)</label><input class="form-control" type="number" name="duration" required /></div>
    <div class="col-md-3"><label class="form-label">Total Marks</label><input class="form-control" type="number" name="totalMarks" required /></div>
    <div class="col-md-4"><label class="form-label">Passing marks (optional)</label>
        <input class="form-control" type="number" name="passingMarks" min="0" placeholder="Leave empty for 40% rule"/>
    </div>
    <div class="col-md-4"><label class="form-label">Available from (optional)</label>
        <input class="form-control" type="datetime-local" name="availableFrom"/></div>
    <div class="col-md-4"><label class="form-label">Available until (optional)</label>
        <input class="form-control" type="datetime-local" name="availableUntil"/></div>
    <div class="col-12 small text-secondary">Students only see the exam between these times (when set). Timer still uses duration above.</div>
    <div class="col-12"><button class="btn btn-primary" type="submit">Create Exam</button></div>
</form>
</div></div>
</body>
</html>
