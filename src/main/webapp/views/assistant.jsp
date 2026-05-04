<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%
    boolean deepSeekConfigured = Boolean.TRUE.equals(request.getAttribute("deepSeekConfigured"));
%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>AI Assistant</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="assistant"/>
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>AI Assistant</h2>
            </div>

            <% if (!deepSeekConfigured) { %>
            <div class="alert alert-warning" role="alert">
                DeepSeek API key is not configured on the server.
            </div>
            <% } %>

            <section class="assistant-panel">
                <div id="assistantMessages" class="assistant-messages" aria-live="polite">
                    <div class="assistant-message assistant-message-assistant">
                        Hi. What would you like to ask?
                    </div>
                </div>

                <form id="assistantForm" class="assistant-composer">
                    <textarea id="assistantInput" class="form-control" rows="3"
                              placeholder="Ask a question"
                              <%= deepSeekConfigured ? "" : "disabled" %>></textarea>
                    <button id="assistantSend" type="submit" class="btn btn-primary"
                            <%= deepSeekConfigured ? "" : "disabled" %>>
                        Send
                    </button>
                </form>
            </section>
        </main>
    </div>
</div>

<script>
    $(function () {
        var endpoint = '<%=request.getContextPath()%>/assistant/ask';
        var conversation = [];
        var pending = false;

        function appendMessage(role, text) {
            var $message = $('<div/>')
                .addClass('assistant-message')
                .addClass('assistant-message-' + role)
                .text(text);
            $('#assistantMessages').append($message);
            $('#assistantMessages').scrollTop($('#assistantMessages')[0].scrollHeight);
            return $message;
        }

        $('#assistantForm').on('submit', function (event) {
            event.preventDefault();
            if (pending) {
                return;
            }

            var message = $('#assistantInput').val().trim();
            if (!message) {
                return;
            }

            pending = true;
            $('#assistantInput').val('').prop('disabled', true);
            $('#assistantSend').prop('disabled', true).text('Sending');
            appendMessage('user', message);
            var $loadingMessage = appendMessage('assistant', '...');

            $.ajax({
                url: endpoint,
                method: 'POST',
                contentType: 'application/json; charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({
                    message: message,
                    history: conversation.slice(-8)
                })
            }).done(function (data) {
                if (data && data.success) {
                    $loadingMessage.text(data.answer);
                    conversation.push({role: 'user', content: message});
                    conversation.push({role: 'assistant', content: data.answer});
                } else {
                    $loadingMessage
                        .removeClass('assistant-message-assistant')
                        .addClass('assistant-message-system')
                        .text((data && data.error) || 'Request failed.');
                }
            }).fail(function (xhr) {
                var error = xhr.responseJSON && xhr.responseJSON.error
                    ? xhr.responseJSON.error
                    : 'Request failed.';
                $loadingMessage
                    .removeClass('assistant-message-assistant')
                    .addClass('assistant-message-system')
                    .text(error);
            }).always(function () {
                pending = false;
                $('#assistantInput').prop('disabled', false).focus();
                $('#assistantSend').prop('disabled', false).text('Send');
            });
        });
    });
</script>
</body>
</html>
