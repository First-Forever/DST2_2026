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
            <jsp:param name="active" value="drug_labels" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drug Labels</h2>
            </div>
            <form action="<%=request.getContextPath()%>/drugLabels" method="get" class="form-inline mb-3">
                <div class="form-group mr-2">
                    <label for="keyword" class="mr-2">Search by Keyword:</label>
                    <input type="text" class="form-control" id="keyword" name="keyword" placeholder="Enter patient-related info or drug name" required>
                </div>
                <button type="submit" class="btn btn-primary">Search</button>
            </form>
            <div class="mb-3">
                <label for="sourceFilter">Filter by Source:</label>
                <select id="sourceFilter" class="form-control mr-3">
                    <option value="">All</option>
                </select>
                <label for="dosingFilter">Filter by Dosing Information:</label>
                <select id="dosingFilter" class="form-control">
                    <option value="">All</option>
                </select>
                <button id="applyFilters" class="btn btn-secondary btn-sm ml-2 mt-1">Apply Filters</button>
            </div>
            <div class="table-responsive">
                <table class="table table-striped table-sm" id="drugLabelsTable">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Source</th>
                        <th>Dosing Information</th>
                        <th>Summary Markdown</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${drugLabels}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.source}</td>
                            <td>${item.dosingInformation}</td>
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
        $('#drugLabelsTable').DataTable({
            searching: false,
            paging: false,
            info: false
        });
        const srcSet = new Set();
        const dosSet = new Set();
        $('#drugLabelsTable tbody tr').each(function() {
            srcSet.add($(this).find('td').eq(1).text().trim());
            dosSet.add($(this).find('td').eq(2).text().trim());
        });
        srcSet.forEach(val => $('#sourceFilter').append('<option value="' + val + '">' + val + '</option>'));
        dosSet.forEach(val => $('#dosingFilter').append('<option value="' + val + '">' + val + '</option>'));
    });
    document.getElementById("applyFilters").addEventListener("click", function () {
        const srcSelected = document.getElementById("sourceFilter").value;
        const dosSelected = document.getElementById("dosingFilter").value;
        const rows = document.querySelectorAll("#drugLabelsTable tbody tr");

        rows.forEach(row => {
            const srcCell = row.cells[1].innerText.trim();
            const dosCell = row.cells[2].innerText.trim();
            const srcMatch = srcSelected === "" || srcCell === srcSelected;
            const dosMatch = dosSelected === "" || dosCell === dosSelected;
            if (srcMatch && dosMatch) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    });
</script>
</body>
</html>
