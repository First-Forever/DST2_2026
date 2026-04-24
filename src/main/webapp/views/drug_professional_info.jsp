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
    <title>Dashboard Template Bootstrap</title>

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
            <jsp:param name="active" value="drug_professional_info" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drug Professional Info</h2>
            </div>
            <form action="<%=request.getContextPath()%>/drugProfessionalInfo" method="get" class="kb-filter-form">
                <div class="kb-filter-group">
                    <label for="keyword">Search by Keyword:</label>
                    <input type="text" class="form-control" id="keyword" name="keyword" value="${selectedKeyword}" placeholder="Enter drug name, gene, source type or PMID">
                </div>
                <div class="kb-filter-group">
                    <label for="sourceTypeFilter">Filter by Source Type:</label>
                    <select id="sourceTypeFilter" name="sourceType" class="form-control">
                        <option value="">All</option>
                        <c:forEach items="${sourceTypeOptions}" var="option">
                            <c:choose>
                                <c:when test="${selectedSourceType == option}">
                                    <option value="${option}" selected="selected">${option}</option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${option}">${option}</option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                </div>
                <div class="kb-filter-group">
                    <label for="evidenceLevelFilter">Filter by Evidence Level:</label>
                    <select id="evidenceLevelFilter" name="evidenceLevel" class="form-control">
                        <option value="">All</option>
                        <c:forEach items="${evidenceLevelOptions}" var="option">
                            <c:choose>
                                <c:when test="${selectedEvidenceLevel == option}">
                                    <option value="${option}" selected="selected">${option}</option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${option}">${option}</option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                </div>
                <div class="kb-filter-actions" style="padding-top: 1.75rem;">
                    <button type="submit" class="btn btn-primary btn-sm">Search</button>
                    <button type="submit" class="btn btn-outline-primary btn-sm">Apply Filters</button>
                    <a href="<%=request.getContextPath()%>/drugProfessionalInfo" class="btn btn-outline-secondary btn-sm">Reset</a>
                </div>
            </form>
            <div class="table-responsive" style="margin-top: 2rem;">
                <table class="table table-striped table-sm" id="drugProfessionalInfoTable">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Drug Name</th>
                        <th>Related Genes</th>
                        <th>Source Type</th>
                        <th>Evidence Level</th>
                        <th>Guideline or Label Tags</th>
                        <th>Literature Summary</th>
                        <th>PMID List</th>
                        <th>Source URL</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${drugProfessionalInfos}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.drugName}</td>
                            <td>${item.relatedGenes}</td>
                            <td>${item.sourceType}</td>
                            <td>${item.evidenceLevel}</td>
                            <td>${item.guidelineOrLabelTags}</td>
                            <td>${item.literatureSummary}</td>
                            <td>${item.pmidList}</td>
                            <td>
                                <c:if test="${not empty item.sourceUrl}">
                                    <a href="${item.sourceUrl}" target="_blank" rel="noopener noreferrer">${item.sourceUrl}</a>
                                </c:if>
                            </td>
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
        $('#drugProfessionalInfoTable').DataTable({
            searching: false,
            paging: false,
            info: false
        });
    });
</script>
</body>
</html>
