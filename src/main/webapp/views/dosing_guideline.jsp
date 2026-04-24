<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="generator" content="">
    <title>Dashboard Template · Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/static/datatables/dataTables.bootstrap4.min.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/datatables/jquery.dataTables.min.js"></script>
    <script src="<%=request.getContextPath()%>/static/datatables/dataTables.bootstrap4.min.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .bd-placeholder-img {
            font-size: 1.125rem;
            text-anchor: middle;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }

        @media (min-width: 768px) {
            .bd-placeholder-img-lg {
                font-size: 3.5rem;
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
        <jsp:include page="nav.jsp" >
            <jsp:param name="active" value="dosing_guideline" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Dosing Guidelines</h2>
            </div>
            <form action="<%=request.getContextPath()%>/dosingGuideline" method="get" class="kb-filter-form">
                <div class="kb-filter-group">
                    <label for="keyword">Search by Keyword:</label>
                    <input type="text" class="form-control" id="keyword" name="keyword" value="${param.keyword}" placeholder="Enter patient-related info or drug name">
                </div>
                <div class="kb-filter-group">
                    <label for="recommendationFilter">Filter by Recommendation:</label>
                    <select id="recommendationFilter" class="form-control">
                        <option value="">All</option>
                    </select>
                </div>
                <div class="kb-filter-group">
                    <label for="sourceFilter">Filter by Source:</label>
                    <select id="sourceFilter" class="form-control">
                        <option value="">All</option>
                    </select>
                </div>
                <div class="kb-filter-actions" style="padding-top: 1.75rem;">
                    <button type="submit" class="btn btn-primary btn-sm">Search</button>
                    <button type="button" id="applyFilters" class="btn btn-outline-primary btn-sm">Apply Filters</button>
                    <a href="<%=request.getContextPath()%>/dosingGuideline" class="btn btn-outline-secondary btn-sm">Reset</a>
                </div>
            </form>
            <div class="table-responsive" style="margin-top: 2rem;">
                <table class="table table-striped table-sm" id="dosingGuidelinesTable">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th>Recommendation</th>
                        <th>Drug Id</th>
                        <th>Source</th>
                        <th>Summary Markdown</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${dosingGuidelines}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.name}</td>
                            <td>${item.recommendation}</td>
                            <td>${item.drugId}</td>
                            <td>${item.source}</td>
                            <td>${item.summaryMarkdown}</td>
                        </tr>
                    </c:forEach>

                    </tbody>
                </table>
            </div>
        </main>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#dosingGuidelinesTable').DataTable({
            searching: false,
            paging: false,
            info: false
        });
        const recSet = new Set();
        const srcSet = new Set();
        $('#dosingGuidelinesTable tbody tr').each(function() {
            recSet.add($(this).find('td').eq(2).text().trim());
            srcSet.add($(this).find('td').eq(4).text().trim());
        });
        recSet.forEach(val => $('#recommendationFilter').append('<option value="' + val + '">' + val + '</option>'));
        srcSet.forEach(val => $('#sourceFilter').append('<option value="' + val + '">' + val + '</option>'));
    });
    document.getElementById("applyFilters").addEventListener("click", function () {
        const recSelected = document.getElementById("recommendationFilter").value;
        const srcSelected = document.getElementById("sourceFilter").value;
        const rows = document.querySelectorAll("#dosingGuidelinesTable tbody tr");

        rows.forEach(row => {
            const recCell = row.cells[2].innerText.trim();
            const srcCell = row.cells[4].innerText.trim();
            const recMatch = recSelected === "" || recCell === recSelected;
            const srcMatch = srcSelected === "" || srcCell === srcSelected;
            if (recMatch && srcMatch) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    });
</script>
</body>
</html>
