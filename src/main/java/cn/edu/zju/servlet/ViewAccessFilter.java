package cn.edu.zju.servlet;

import cn.edu.zju.bean.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class ViewAccessFilter implements Filter {

    private static final Set<String> PUBLIC_VIEWS = Set.of("/views/welcome.jsp", "/views/register.jsp");
    private static final Set<String> PROFESSIONAL_ONLY_VIEWS = Set.of("/views/drug_professional_info.jsp");

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String viewPath = getViewPath(httpRequest);

        if (PUBLIC_VIEWS.contains(viewPath)) {
            chain.doFilter(request, response);
            return;
        }

        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/");
            return;
        }

        if (PROFESSIONAL_ONLY_VIEWS.contains(viewPath) && !hasProfessionalAccess(currentUser)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Professional users or administrators only.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private String getViewPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return requestUri.startsWith(contextPath) ? requestUri.substring(contextPath.length()) : requestUri;
    }

    private User getCurrentUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object currentUser = request.getSession(false).getAttribute("currentUser");
        return currentUser instanceof User ? (User) currentUser : null;
    }

    private boolean hasProfessionalAccess(User user) {
        return user.getPermission() == User.Permission.PROFESSIONAL_USER
                || user.getPermission() == User.Permission.ADMIN;
    }
}
