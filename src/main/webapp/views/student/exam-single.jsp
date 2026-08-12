<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Exam</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-theme.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4" style="max-width: 720px;">
    <div class="topbar mb-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
            <h5 class="mb-0">Question ${questionIndex} / ${totalQuestions}</h5>
            <small class="text-secondary">Answer and use Next or Submit</small>
        </div>
        <div class="text-end">
            <small class="text-secondary d-block">Time left</small>
            <span class="badge text-bg-danger fs-6" id="timerDisplay">--:--</span>
        </div>
    </div>

    <div id="proctorWarning" class="alert alert-warning d-none align-items-center" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        <span>Warning: Tab switch or window blur detected. Stay on this exam window.</span>
    </div>

    <div class="view-card p-4 mb-3">
        <div class="mb-2 small text-secondary">Marks: ${question.marks}</div>
        <p class="fw-semibold mb-3">${question.questionText}</p>

        <form id="examForm" method="post" action="${pageContext.request.contextPath}/student/exam">
            <input type="hidden" name="examId" value="${examId}"/>
            <input type="hidden" name="duration" value="${durationMinutes}"/>
            <input type="hidden" name="currentQuestionId" value="${question.questionId}"/>

            <c:choose>
                <c:when test="${question.questionType == 'MCQ'}">
                    <c:forEach var="opt" items="${question.options}">
                        <div class="form-check mb-2">
                            <input class="form-check-input" type="radio" name="answer" id="opt${opt.optionId}"
                                   value="${opt.optionId}"
                                   <c:if test="${not empty savedValue and savedValue == opt.optionId}">checked="checked"</c:if>/>
                            <label class="form-check-label" for="opt${opt.optionId}">${opt.optionText}</label>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <textarea class="form-control" name="answer" rows="5" placeholder="Your answer">${savedValue}</textarea>
                </c:otherwise>
            </c:choose>

            <div class="d-flex flex-wrap gap-2 mt-4">
                <button type="submit" name="action" value="prev" class="btn btn-outline-secondary" ${questionIndex <= 1 ? 'disabled' : ''}>
                    <i class="bi bi-arrow-left"></i> Previous
                </button>
                <button type="submit" name="action" value="next" class="btn btn-primary" ${questionIndex >= totalQuestions ? 'disabled' : ''}>
                    Next <i class="bi bi-arrow-right"></i>
                </button>
                <button type="submit" name="action" value="submit" id="submitBtn" class="btn btn-success ms-auto">
                    <span class="submit-label">Submit Exam</span>
                    <span id="submitSpin" class="spinner-border spinner-border-sm d-none ms-1"></span>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    const endMs = ${serverEndMs};
    const examId = ${examId};
    let examNavigating = false;

    function tick() {
        const left = Math.max(0, endMs - Date.now());
        const m = Math.floor(left / 60000);
        const s = Math.floor((left % 60000) / 1000);
        document.getElementById("timerDisplay").textContent =
            m.toString().padStart(2, "0") + ":" + s.toString().padStart(2, "0");
        if (left <= 0) {
            examNavigating = true;
            const f = document.getElementById("examForm");
            const i = document.createElement("input");
            i.type = "hidden";
            i.name = "action";
            i.value = "submit";
            f.appendChild(i);
            document.getElementById("submitBtn").disabled = true;
            document.getElementById("submitSpin").classList.remove("d-none");
            f.submit();
        }
    }
    tick();
    setInterval(tick, 500);

    function autosave() {
        const form = document.getElementById("examForm");
        const p = new URLSearchParams();
        p.set("examId", form.elements.examId.value);
        p.set("currentQuestionId", form.elements.currentQuestionId.value);
        const ans = form.elements.namedItem("answer");
        if (ans) {
            p.set("answer", ans.value || "");
        }
        fetch("${pageContext.request.contextPath}/student/exam/autosave", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: p.toString(),
            credentials: "same-origin"
        }).catch(function () {});
    }
    setInterval(autosave, 25000);

    function logProctor(type) {
        const p = new URLSearchParams();
        p.set("examId", String(examId));
        p.set("eventType", type);
        fetch("${pageContext.request.contextPath}/student/proctor/event", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: p.toString(),
            credentials: "same-origin"
        }).catch(function () {});
    }
    window.addEventListener("blur", function () {
        logProctor("WINDOW_BLUR");
        document.getElementById("proctorWarning").classList.remove("d-none");
        document.getElementById("proctorWarning").classList.add("d-flex");
    });
    document.addEventListener("visibilitychange", function () {
        if (document.hidden) {
            logProctor("TAB_SWITCH");
            document.getElementById("proctorWarning").classList.remove("d-none");
            document.getElementById("proctorWarning").classList.add("d-flex");
        }
    });
    window.addEventListener("pagehide", function () {
        if (examNavigating) {
            return;
        }
        logProctor("PAGE_HIDE");
        document.getElementById("proctorWarning").classList.remove("d-none");
        document.getElementById("proctorWarning").classList.add("d-flex");
    });

    document.getElementById("examForm").addEventListener("submit", function (e) {
        examNavigating = true;
        const sub = e.submitter;
        if (sub && sub.getAttribute("name") === "action" && sub.value === "submit") {
            document.getElementById("submitSpin").classList.remove("d-none");
            sub.disabled = true;
        }
    });
</script>
</body>
</html>
