<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login - OES</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body {
            min-height: 100vh;
            background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 45%, #93c5fd 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: Inter, "Segoe UI", Arial, sans-serif;
        }
        .login-card {
            width: 100%;
            max-width: 430px;
            border: 0;
            border-radius: 16px;
            box-shadow: 0 20px 40px rgba(15, 23, 42, 0.15);
        }
        .form-control {
            border-radius: 10px;
            padding-left: 2.5rem;
        }
        .input-icon {
            position: relative;
        }
        .input-icon i {
            position: absolute;
            left: 0.8rem;
            top: 50%;
            transform: translateY(-50%);
            color: #64748b;
        }
    </style>
</head>
<body>
<div class="card login-card p-4">
    <div class="card-body">
        <h4 class="fw-bold mb-1 text-primary">Online Examination and Evaluation System</h4>
        <p class="text-secondary mb-4">Sign in to continue</p>
        <c:if test="${not empty message}">
            <div class="alert alert-success py-2">${message}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <label class="form-label">Email or User ID</label>
            <div class="input-icon mb-3">
                <i class="bi bi-person-circle"></i>
                <input type="text" class="form-control" name="identifier" placeholder="admin@oes.com" required/>
            </div>
            <label class="form-label">Password</label>
            <div class="input-icon mb-3">
                <i class="bi bi-lock-fill"></i>
                <input type="password" class="form-control" name="password" placeholder="Enter password" required/>
            </div>
            <button class="btn btn-primary w-100 py-2" type="submit">Login</button>
        </form>
        <p class="text-danger mt-3 mb-0">${error}</p>
        <div class="mt-3 text-center">
            <a href="${pageContext.request.contextPath}/register" class="link-primary text-decoration-none">
                Create a student account
            </a>
        </div>
    </div>
</div>
</body>
</html>
