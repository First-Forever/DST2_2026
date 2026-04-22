<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Register - Precision Medicine Matching System</title>
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <style>
        body {
            min-height: 100vh;
            background:
                    linear-gradient(120deg, rgba(22, 92, 99, 0.12), rgba(235, 244, 240, 0.95)),
                    repeating-linear-gradient(45deg, rgba(31, 111, 120, 0.06) 0, rgba(31, 111, 120, 0.06) 1px, transparent 1px, transparent 18px);
            color: #17324d;
        }

        .register-shell {
            min-height: 100vh;
            display: flex;
            align-items: center;
            padding: 48px 0;
        }

        .register-card {
            border: 0;
            border-radius: 24px;
            box-shadow: 0 22px 60px rgba(23, 50, 77, 0.14);
            overflow: hidden;
        }

        .register-heading {
            background: #17324d;
            color: #fff;
            padding: 34px 40px;
        }

        .register-body {
            background: rgba(255, 255, 255, 0.96);
            padding: 40px;
        }

        .form-control {
            border-radius: 12px;
            min-height: 46px;
        }

        .btn-register {
            border-radius: 12px;
            background: #1f6f78;
            border-color: #1f6f78;
            min-height: 46px;
            font-weight: 600;
        }

        .btn-register:hover {
            background: #185b63;
            border-color: #185b63;
        }

        .hint-box {
            border-radius: 14px;
            background: #eef6f2;
            color: #31536a;
            padding: 14px 16px;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
<div class="container register-shell">
    <div class="row justify-content-center w-100">
        <div class="col-lg-7 col-xl-6">
            <div class="card register-card">
                <div class="register-heading">
                    <h1 class="h3 mb-2">Create an account</h1>
                    <p class="mb-0">Register to use the Precision Medicine Matching System.</p>
                </div>
                <div class="register-body">
                    <% if (request.getAttribute("errorMessage") != null) { %>
                    <div class="alert alert-danger" role="alert">
                        <%=request.getAttribute("errorMessage")%>
                    </div>
                    <% } %>

                    <form method="post" action="<%=request.getContextPath()%>/register">
                        <div class="form-group">
                            <label for="username">Username</label>
                            <input type="text" class="form-control" id="username" name="username"
                                   placeholder="Choose a username" required>
                        </div>
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="name@example.com" required>
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="Create a password" required>
                        </div>
                        <div class="form-group">
                            <label for="permission">Permission</label>
                            <select class="form-control" id="permission" name="permission">
                                <option value="NORMAL_USER">Normal user</option>
                                <option value="PROFESSIONAL_USER">Professional user</option>
                                <option value="ADMIN">Administrator</option>
                            </select>
                        </div>
                        <div class="hint-box mb-4">
                            Professional users and administrators must use a ZJU email. The first administrator is
                            approved automatically; later administrators need approval after registration.
                        </div>
                        <button type="submit" class="btn btn-primary btn-register btn-block">Register</button>
                    </form>

                    <div class="text-center mt-4">
                        <a href="<%=request.getContextPath()%>/">Back to sign in</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
