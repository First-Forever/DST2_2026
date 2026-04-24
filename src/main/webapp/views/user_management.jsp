<%@ page import="cn.edu.zju.bean.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>User Management - Precision Medicine Matching System</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .admin-card {
            border: 1px solid rgba(23, 50, 77, 0.08);
            border-radius: 16px;
            box-shadow: 0 10px 28px rgba(23, 50, 77, 0.06);
        }

        .admin-card + .admin-card {
            margin-top: 1.5rem;
        }

        .admin-card .card-header {
            background: #f5f8fb;
            border-bottom: 1px solid rgba(23, 50, 77, 0.08);
        }

        .admin-subtitle {
            color: #5f7388;
            margin-bottom: 0;
        }

        .compact-form .form-control,
        .compact-form .custom-select {
            min-width: 140px;
        }

        .compact-form .btn {
            white-space: nowrap;
        }

        .table thead th {
            white-space: nowrap;
            vertical-align: middle;
        }

        .table td {
            vertical-align: middle;
        }

        .admin-note {
            font-size: 0.88rem;
            color: #5f7388;
        }

        @media (max-width: 991.98px) {
            .compact-form .form-row > div {
                margin-bottom: .75rem;
            }
        }
    </style>
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="admin_users" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <div>
                    <h2 class="mb-1">User Management</h2>
                    <p class="admin-subtitle">Approve administrator applications and manage system users.</p>
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success" role="alert">${successMessage}</div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger" role="alert">${errorMessage}</div>
            </c:if>

            <div class="card admin-card">
                <div class="card-header">
                    <h5 class="mb-1">Create User</h5>
                    <p class="admin-subtitle">Administrators can directly create normal, professional, or administrator accounts.</p>
                </div>
                <div class="card-body">
                    <form method="post" action="<%=request.getContextPath()%>/admin/users/create" class="compact-form">
                        <div class="form-row">
                            <div class="form-group col-lg-3 col-md-6">
                                <label for="createUsername">Username</label>
                                <input type="text" class="form-control" id="createUsername" name="username" required>
                            </div>
                            <div class="form-group col-lg-3 col-md-6">
                                <label for="createEmail">Email</label>
                                <input type="email" class="form-control" id="createEmail" name="email" required>
                            </div>
                            <div class="form-group col-lg-2 col-md-6">
                                <label for="createPassword">Password</label>
                                <input type="password" class="form-control" id="createPassword" name="password" required>
                            </div>
                            <div class="form-group col-lg-2 col-md-6">
                                <label for="createPermission">User Type</label>
                                <select class="custom-select" id="createPermission" name="permission">
                                    <c:forEach items="${permissionOptions}" var="option">
                                        <option value="${option}">${option}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group col-lg-2 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary btn-block">Create User</button>
                            </div>
                        </div>
                    </form>
                    <p class="admin-note mb-0">Professional users and administrators must use a ZJU email. Newly created administrators are approved immediately.</p>
                </div>
            </div>

            <div class="card admin-card">
                <div class="card-header">
                    <h5 class="mb-1">Pending Administrator Applications</h5>
                    <p class="admin-subtitle">Review users who requested administrator permission during registration.</p>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty pendingAdmins}">
                            <p class="text-muted mb-0">No pending administrator applications.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-bordered table-hover mb-0">
                                    <thead class="thead-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Username</th>
                                        <th>Email</th>
                                        <th>Created At</th>
                                        <th>Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${pendingAdmins}" var="user">
                                        <tr>
                                            <td>${user.id}</td>
                                            <td>${user.username}</td>
                                            <td>${user.email}</td>
                                            <td>${user.createdAt}</td>
                                            <td>
                                                <form method="post" action="<%=request.getContextPath()%>/admin/users/approve" class="mb-0">
                                                    <input type="hidden" name="id" value="${user.id}">
                                                    <button type="submit" class="btn btn-sm btn-success">Approve</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="card admin-card">
                <div class="card-header">
                    <h5 class="mb-1">All Users</h5>
                    <p class="admin-subtitle">Update usernames, emails, passwords, and permission levels, or delete accounts.</p>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-bordered table-hover mb-0">
                            <thead class="thead-light">
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>User Type</th>
                                <th>Admin Approved</th>
                                <th>Created At</th>
                                <th style="min-width: 460px;">Edit</th>
                                <th>Delete</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${users}" var="user">
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.username}</td>
                                    <td>${user.email}</td>
                                    <td>${user.permission}</td>
                                    <td>${user.adminApproved}</td>
                                    <td>${user.createdAt}</td>
                                    <td>
                                        <form method="post" action="<%=request.getContextPath()%>/admin/users/update" class="compact-form">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <div class="form-row">
                                                <div class="col-md-3">
                                                    <input type="text" class="form-control form-control-sm" name="username" value="${user.username}" required>
                                                </div>
                                                <div class="col-md-3">
                                                    <input type="email" class="form-control form-control-sm" name="email" value="${user.email}" required>
                                                </div>
                                                <div class="col-md-2">
                                                    <input type="password" class="form-control form-control-sm" name="password" placeholder="New password">
                                                </div>
                                                <div class="col-md-2">
                                                    <select class="custom-select custom-select-sm" name="permission">
                                                        <c:forEach items="${permissionOptions}" var="option">
                                                            <c:choose>
                                                                <c:when test="${user.permission == option}">
                                                                    <option value="${option}" selected="selected">${option}</option>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <option value="${option}">${option}</option>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                                <div class="col-md-2">
                                                    <button type="submit" class="btn btn-sm btn-primary btn-block">Save</button>
                                                </div>
                                            </div>
                                        </form>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${currentUser != null && currentUser.id == user.id}">
                                                <span class="text-muted">Current user</span>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post" action="<%=request.getContextPath()%>/admin/users/delete" class="mb-0" onsubmit="return confirm('Delete this user?');">
                                                    <input type="hidden" name="id" value="${user.id}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <p class="admin-note mt-3 mb-0">Leave the password field blank when editing a user if you want to keep the existing password.</p>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
