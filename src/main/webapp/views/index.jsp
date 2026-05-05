<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css?v=dashboard-20260505" rel="stylesheet">
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

        .dashboard-intro {
            display: grid;
            grid-template-columns: minmax(0, 1fr) 320px;
            gap: 1.5rem;
            align-items: stretch;
            margin-bottom: 1.5rem;
        }

        .dashboard-intro-main,
        .dashboard-intro-note,
        .dashboard-card,
        .dashboard-workflow {
            border: 1px solid #d7dde5;
            border-radius: .25rem;
            background: #fff;
        }

        .dashboard-intro-main {
            padding: 1.5rem;
        }

        .dashboard-intro-main h3 {
            margin-bottom: .75rem;
            font-size: 1.5rem;
        }

        .dashboard-intro-main p {
            max-width: 820px;
            margin-bottom: 0;
            color: #4b5b68;
            line-height: 1.6;
        }

        .dashboard-kicker {
            margin-bottom: .45rem !important;
            color: #1f6f78 !important;
            font-size: .78rem;
            font-weight: 700;
            letter-spacing: .08em;
            text-transform: uppercase;
        }

        .dashboard-intro-note {
            display: flex;
            align-items: center;
            padding: 1.25rem;
            color: #5b4b16;
            background: #fff8e5;
            border-color: #f0d995;
            line-height: 1.55;
        }

        .dashboard-card-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 1rem;
            margin-bottom: 1.5rem;
        }

        .dashboard-card {
            padding: 1.25rem;
            min-width: 0;
        }

        .dashboard-card-index {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            margin-bottom: .9rem;
            border-radius: .25rem;
            color: #1f6f78;
            background: #e8f4f2;
            font-weight: 700;
        }

        .dashboard-card h4 {
            margin-bottom: .55rem;
            font-size: 1rem;
            font-weight: 700;
        }

        .dashboard-card p {
            margin-bottom: 0;
            color: #586775;
            line-height: 1.55;
            overflow-wrap: anywhere;
        }

        .dashboard-workflow {
            padding: 1.5rem;
            margin-bottom: 2rem;
        }

        .dashboard-workflow h3 {
            margin-bottom: 1rem;
            font-size: 1.25rem;
        }

        .dashboard-workflow ol {
            margin-bottom: 0;
            padding-left: 1.25rem;
            color: #4b5b68;
            line-height: 1.75;
        }

        @media (max-width: 1199.98px) {
            .dashboard-card-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 991.98px) {
            .dashboard-intro {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 575.98px) {
            .dashboard-card-grid {
                grid-template-columns: 1fr;
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
            <jsp:param name="active" value="dashboard" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Dashboard</h2>
            </div>
            <section class="dashboard-intro">
                <div class="dashboard-intro-main">
                    <p class="dashboard-kicker">Biomedical Informatics Workspace</p>
                    <h3>Precision Medicine Matching System</h3>
                    <p>
                        This system helps users connect patient genomic variation with pharmacogenomic knowledge,
                        including drug records, clinical labels, dosing guidelines, and professional evidence summaries.
                    </p>
                </div>
                <div class="dashboard-intro-note">
                    Use this platform for learning, research, and decision-support workflows. Clinical decisions should
                    be made by qualified professionals using validated sources.
                </div>
            </section>

            <section class="dashboard-card-grid">
                <div class="dashboard-card">
                    <div class="dashboard-card-index">01</div>
                    <h4>Explore Knowledge</h4>
                    <p>
                        Search drugs, drug labels, and dosing guidelines to review pharmacogenomic evidence by keyword,
                        biomarker status, source type, and evidence level.
                    </p>
                </div>
                <div class="dashboard-card">
                    <div class="dashboard-card-index">02</div>
                    <h4>Match Samples</h4>
                    <p>
                        Upload ANNOVAR output, extract patient-related genes, and compare them against known drug-label
                        summaries for precision medicine analysis.
                    </p>
                </div>
                <div class="dashboard-card">
                    <div class="dashboard-card-index">03</div>
                    <h4>Ask the Assistant</h4>
                    <p>
                        Open the AI Assistant to ask general questions through the server-side DeepSeek integration
                        without exposing the API key in the browser.
                    </p>
                </div>
                <div class="dashboard-card">
                    <div class="dashboard-card-index">04</div>
                    <h4>Manage Access</h4>
                    <p>
                        Normal users can browse core resources, professional users can view restricted professional
                        information, and administrators can approve elevated permissions.
                    </p>
                </div>
            </section>

            <section class="dashboard-workflow">
                <h3>Typical Workflow</h3>
                <ol>
                    <li>Register or sign in with the appropriate account permission.</li>
                    <li>Review drug, label, guideline, and professional knowledge-base records.</li>
                    <li>Upload ANNOVAR sample output and run sample matching.</li>
                    <li>Use the AI Assistant for general explanation and follow-up questions.</li>
                </ol>
            </section>
        </main>
    </div>
</div>
</body>
</html>