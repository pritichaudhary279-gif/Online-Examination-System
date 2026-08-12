<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Exam Result</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="view-card p-4">
        <h3 class="text-success mb-3">Exam Submitted Successfully</h3>
        <p class="mb-1"><strong>Score obtained:</strong> ${result.scoreObtained} / ${result.totalMarks}</p>
        <p class="mb-3">
            <strong>Result:</strong>
            <span class="badge ${result.passed ? 'text-bg-success' : 'text-bg-danger'}">${result.message}</span>
        </p>
        <p class="text-secondary small">MCQ and subjective answers are evaluated. ${result.passThresholdDescription}</p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Go To Dashboard</a>
    </div>
</div>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
    <div id="successToast" class="toast text-bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body">Test submitted successfully. Result saved.</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const toast = new bootstrap.Toast(document.getElementById("successToast"));
    toast.show();
</script>
</body>
</html>
