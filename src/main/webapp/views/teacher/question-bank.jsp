<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Question Bank</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
    <script>
        function toggleMcq() {
            const t = document.getElementById("qType").value;
            document.getElementById("mcqBlock").style.display = (t === "MCQ") ? "block" : "none";
            document.getElementById("subBlock").style.display = (t === "SUBJECTIVE") ? "block" : "none";
        }
    </script>
</head>
<body class="p-3 p-md-4" onload="toggleMcq()">
<div class="container-fluid">
    <div class="topbar mb-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h5 class="mb-0">Question bank</h5>
        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/dashboard">Back</a>
    </div>
    <c:if test="${not empty param.imported}">
        <p class="text-success">Imported ${param.imported} question(s).</p>
    </c:if>
    <p class="text-danger">${param.err}</p>

    <div class="view-card p-3 mb-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-6">
                <label class="form-label">Subject</label>
                <select class="form-select" name="subjectId" onchange="this.form.submit()">
                    <c:forEach var="s" items="${subjects}">
                        <option value="${s.subject_id}" ${s.subject_id == selectedSubjectId ? 'selected' : ''}>${s.course_name} — ${s.subject_name}</option>
                    </c:forEach>
                </select>
            </div>
        </form>
    </div>

    <div class="row g-3">
        <div class="col-lg-5">
            <div class="view-card p-3 mb-3">
                <h6 class="fw-semibold">Add to bank</h6>
                <form method="post" action="${pageContext.request.contextPath}/teacher/bank" class="row g-2">
                    <input type="hidden" name="formToken" value="${formToken}"/>
                    <input type="hidden" name="subjectId" value="${selectedSubjectId}"/>
                    <div class="col-12">
                        <label class="form-label small">Question</label>
                        <textarea class="form-control" name="questionText" rows="3" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small">Type</label>
                        <select class="form-select" id="qType" name="questionType" onchange="toggleMcq()">
                            <option value="MCQ">MCQ</option>
                            <option value="SUBJECTIVE">SUBJECTIVE</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small">Marks</label>
                        <input class="form-control" type="number" name="marks" min="1" value="1" required/>
                    </div>
                    <div id="mcqBlock" class="col-12">
                        <div class="row g-2">
                            <div class="col-6"><input class="form-control" name="option1" placeholder="Option 1"/></div>
                            <div class="col-6"><input class="form-control" name="option2" placeholder="Option 2"/></div>
                            <div class="col-6"><input class="form-control" name="option3" placeholder="Option 3"/></div>
                            <div class="col-6"><input class="form-control" name="option4" placeholder="Option 4"/></div>
                        </div>
                        <label class="form-label small mt-2">Correct</label>
                        <select class="form-select" name="correctOption">
                            <option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option>
                        </select>
                    </div>
                    <div id="subBlock" class="col-12">
                        <textarea class="form-control mb-2" name="modelAnswer" rows="2" placeholder="Model answer"></textarea>
                        <input class="form-control" name="expectedKeywords" placeholder="Keywords, comma separated"/>
                    </div>
                    <div class="col-12"><button class="btn btn-primary" type="submit">Save in bank</button></div>
                </form>
            </div>
            <div class="view-card p-3">
                <h6 class="fw-semibold">Bulk import (CSV or PDF text)</h6>
                <p class="small text-secondary">Use pipe format per line, e.g.<br/>
                    <code>MCQ|Question text|2|OptA|OptB|OptC|OptD|1</code><br/>
                    <code>SUBJECTIVE|Question|5|||||keyword1,keyword2</code>
                </p>
                <form method="post" action="${pageContext.request.contextPath}/teacher/exam/questions/import" enctype="multipart/form-data" class="row g-2">
                    <input type="hidden" name="target" value="bank"/>
                    <input type="hidden" name="subjectId" value="${selectedSubjectId}"/>
                    <div class="col-12">
                        <select class="form-select" name="fileType">
                            <option value="csv">CSV / text file</option>
                            <option value="pdf">PDF (extracted text)</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <input class="form-control" type="file" name="dataFile" required/>
                    </div>
                    <div class="col-12"><button class="btn btn-outline-primary" type="submit">Import into bank</button></div>
                </form>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="view-card p-3">
                <h6 class="fw-semibold">Bank items</h6>
                <div class="table-responsive">
                    <table class="table table-modern mb-0">
                        <thead><tr><th>ID</th><th>Type</th><th>Marks</th><th>Text</th><th>Created</th></tr></thead>
                        <tbody>
                        <c:forEach var="b" items="${bankRows}">
                            <tr>
                                <td>${b.bank_question_id}</td>
                                <td>${b.question_type}</td>
                                <td>${b.marks}</td>
                                <td class="small">${b.question_text}</td>
                                <td class="small">${b.created_at}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
