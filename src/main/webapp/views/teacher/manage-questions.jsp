<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Questions</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
    <script>
        function toggleMcqFields() {
            const type = document.getElementById("questionType").value;
            document.getElementById("mcqBlock").style.display = (type === "MCQ") ? "block" : "none";
            document.getElementById("subjectiveBlock").style.display = (type === "SUBJECTIVE") ? "block" : "none";
        }
    </script>
</head>
<body class="p-3 p-md-4" onload="toggleMcqFields()">
<div class="container-fluid">
<div class="topbar mb-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
    <div>
        <h5 class="mb-0">Exam #${examId} — ${examMeta.subject_name}</h5>
        <small class="text-secondary">${examMeta.title}</small>
    </div>
    <div class="d-flex gap-2 flex-wrap">
        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/teacher/exam/roster?examId=${examId}">Roster &amp; completion</a>
        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/dashboard">Back</a>
    </div>
</div>

<c:if test="${not empty param.imported}">
    <p class="text-success">Imported ${param.imported} question(s).</p>
</c:if>
<p class="text-danger">${param.err}</p>

<div class="row g-3 mb-3">
    <div class="col-lg-6">
        <div class="view-card p-3 h-100">
            <h6 class="fw-semibold mb-2">Add from question bank</h6>
            <p class="small text-secondary mb-2">Select items for this exam (same subject).</p>
            <form method="post" action="${pageContext.request.contextPath}/teacher/exam/questions">
                <input type="hidden" name="formAction" value="addFromBank"/>
                <input type="hidden" name="examId" value="${examId}"/>
                <input type="hidden" name="formToken" value="${formToken}"/>
                <div class="table-responsive" style="max-height: 220px; overflow-y: auto;">
                    <table class="table table-sm mb-2">
                        <c:forEach var="bq" items="${bankQuestions}">
                            <tr>
                                <td><input type="checkbox" name="bankQuestionId" value="${bq.bank_question_id}"/></td>
                                <td class="small">${bq.question_text}</td>
                                <td class="small text-nowrap">${bq.question_type} / ${bq.marks}m</td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
                <button class="btn btn-primary btn-sm" type="submit">Add selected to exam</button>
                <a class="btn btn-link btn-sm" href="${pageContext.request.contextPath}/teacher/bank?subjectId=${examMeta.subject_id}">Manage bank</a>
            </form>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="view-card p-3 h-100">
            <h6 class="fw-semibold mb-2">Bulk import into this exam</h6>
            <p class="small text-secondary">Same line format as the bank import (pipe or CSV).</p>
            <form method="post" action="${pageContext.request.contextPath}/teacher/exam/questions/import" enctype="multipart/form-data">
                <input type="hidden" name="target" value="exam"/>
                <input type="hidden" name="examId" value="${examId}"/>
                <div class="mb-2">
                    <select class="form-select form-select-sm" name="fileType">
                        <option value="csv">CSV / text</option>
                        <option value="pdf">PDF (text)</option>
                    </select>
                </div>
                <input class="form-control form-control-sm mb-2" type="file" name="dataFile" required/>
                <button class="btn btn-outline-primary btn-sm" type="submit">Import</button>
            </form>
        </div>
    </div>
</div>

<div class="view-card p-3 mb-3">
<form class="row g-3" method="post" action="${pageContext.request.contextPath}/teacher/exam/questions" onsubmit="return lockQuestionSubmit(this)">
    <input type="hidden" name="formAction" value="addQuestion"/>
    <input type="hidden" name="examId" value="${examId}" />
    <input type="hidden" name="formToken" value="${formToken}" />
    <div class="col-12"><label class="form-label">Question Text</label>
    <textarea class="form-control" name="questionText" rows="3" required></textarea></div>

    <div class="col-md-4"><label class="form-label">Question Type</label>
    <select class="form-select" id="questionType" name="questionType" onchange="toggleMcqFields()">
        <option value="MCQ">MCQ</option>
        <option value="SUBJECTIVE">SUBJECTIVE</option>
    </select></div>

    <div class="col-md-4"><label class="form-label">Marks</label>
    <input class="form-control" type="number" name="marks" required min="1" /></div>

    <div id="mcqBlock" class="col-12">
        <h6>Options</h6>
        <div class="row g-2">
        <div class="col-md-6"><input class="form-control" type="text" name="option1" placeholder="Option 1" /></div>
        <div class="col-md-6"><input class="form-control" type="text" name="option2" placeholder="Option 2" /></div>
        <div class="col-md-6"><input class="form-control" type="text" name="option3" placeholder="Option 3" /></div>
        <div class="col-md-6"><input class="form-control" type="text" name="option4" placeholder="Option 4" /></div>
        </div>
        <label class="form-label mt-2">Correct Option</label>
        <select class="form-select" name="correctOption">
            <option value="1">Option 1</option>
            <option value="2">Option 2</option>
            <option value="3">Option 3</option>
            <option value="4">Option 4</option>
        </select>
    </div>

    <div id="subjectiveBlock" class="col-12">
        <h6>Subjective Reference (NLP)</h6>
        <textarea class="form-control mb-2" name="modelAnswer" rows="2" placeholder="Model answer"></textarea>
        <input class="form-control" type="text" name="expectedKeywords" placeholder="Expected keywords, comma separated" />
    </div>
    <div class="col-12">
        <button id="addQuestionBtn" class="btn btn-primary" type="submit">Add Question</button>
        <span id="questionLoader" class="spinner-border spinner-border-sm ms-2 d-none text-primary"></span>
    </div>
</form>
</div>

<div class="view-card p-3">
<h6>Existing Questions</h6>
<div class="table-responsive"><table class="table table-modern mb-0">
    <tr><th>Question ID</th><th>Text</th><th>Type</th><th>Marks</th><th>Options</th><th>Correct Option</th></tr>
    <c:forEach var="q" items="${questions}">
        <tr>
            <td>${q.question_id}</td>
            <td>${q.question_text}</td>
            <td>${q.question_type}</td>
            <td>${q.marks}</td>
            <td>${q.options_text}</td>
            <td>${q.correct_options}</td>
        </tr>
    </c:forEach>
</table></div></div></div>
<script>
    function lockQuestionSubmit(form) {
        const btn = document.getElementById("addQuestionBtn");
        const loader = document.getElementById("questionLoader");
        if (btn) btn.disabled = true;
        if (loader) loader.classList.remove("d-none");
        return true;
    }
</script>
</body>
</html>
