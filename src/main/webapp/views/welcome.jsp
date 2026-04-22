<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Welcome - Precision Medicine Matching System</title>
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <style>
        body {
            min-height: 100vh;
            background:
                    radial-gradient(circle at 12% 18%, rgba(55, 132, 214, 0.18), transparent 28%),
                    linear-gradient(135deg, #eef6f2 0%, #d8e9f2 48%, #f7f9fb 100%);
            color: #17324d;
        }

        .welcome-shell {
            min-height: 100vh;
            display: flex;
            align-items: center;
            padding: 48px 0;
        }

        .hero-card {
            border: 0;
            border-radius: 28px;
            box-shadow: 0 24px 70px rgba(23, 50, 77, 0.16);
            overflow: hidden;
        }

        .hero-panel {
            background: linear-gradient(155deg, #123a5a 0%, #1f6f78 100%);
            color: #fff;
            min-width: 0;
        }

        .hero-card .hero-panel {
            padding: 56px 48px;
        }

        .hero-title {
            max-width: 100%;
            font-size: 3rem;
            line-height: 1.08;
            font-weight: 600;
            overflow-wrap: anywhere;
            word-break: normal;
        }

        .hero-description {
            max-width: 560px;
        }

        .login-panel {
            background: rgba(255, 255, 255, 0.94);
            min-width: 0;
        }

        .hero-card .login-panel {
            padding: 48px;
        }

        .system-mark {
            display: inline-block;
            padding: 8px 14px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.14);
            font-size: 0.78rem;
            letter-spacing: 0.08em;
            text-transform: uppercase;
        }

        .form-control {
            border-radius: 12px;
            min-height: 46px;
            max-width: 100%;
        }

        .btn-login {
            border-radius: 12px;
            background: #1f6f78;
            border-color: #1f6f78;
            min-height: 46px;
            font-weight: 600;
        }

        .btn-login:hover {
            background: #185b63;
            border-color: #185b63;
        }

        .register-link {
            color: #1f6f78;
            font-weight: 600;
        }

        @media (max-width: 1199.98px) {
            .hero-title {
                font-size: 2.5rem;
            }
        }

        @media (max-width: 991.98px) {
            .hero-card .hero-panel,
            .hero-card .login-panel {
                padding: 40px 32px;
            }

            .hero-title {
                font-size: 2.35rem;
            }
        }

        @media (max-width: 575.98px) {
            .welcome-shell {
                padding: 24px 0;
            }

            .hero-card .hero-panel,
            .hero-card .login-panel {
                padding: 32px 24px;
            }

            .hero-title {
                font-size: 2rem;
            }

            .system-mark {
                white-space: normal;
            }
        }
    </style>
</head>
<body>
<div class="container welcome-shell">
    <div class="card hero-card w-100">
        <div class="row no-gutters">
            <div class="col-lg-7 hero-panel">
                <span class="system-mark">Biomedical Informatics</span>
                <h1 class="hero-title mt-4 mb-3">Precision Medicine Matching System</h1>
                <p class="lead hero-description mb-0">
                    Welcome back. Sign in to continue matching genomic samples with precision medicine knowledge.
                </p>
            </div>
            <div class="col-lg-5 login-panel">
                <h2 class="mb-2">Welcome</h2>
                <p class="text-muted mb-4">Please enter your username/email and password.</p>

                <% if (request.getAttribute("successMessage") != null) { %>
                <div class="alert alert-success" role="alert">
                    <%=request.getAttribute("successMessage")%>
                </div>
                <% } %>
                <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%=request.getAttribute("errorMessage")%>
                </div>
                <% } %>

                <form method="post" action="<%=request.getContextPath()%>/login">
                    <div class="form-group">
                        <label for="loginAccount">Username / Email</label>
                        <input type="text" class="form-control" id="loginAccount" name="account"
                               placeholder="Enter username or email" required>
                    </div>
                    <div class="form-group">
                        <label for="loginPassword">Password</label>
                        <input type="password" class="form-control" id="loginPassword" name="password"
                               placeholder="Enter password" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-login btn-block mt-4">Sign in</button>
                </form>

                <div class="text-center mt-4">
                    <span class="text-muted">No account yet?</span>
                    <a class="register-link ml-1" href="<%=request.getContextPath()%>/register">Register</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
