package cn.edu.zju.servlet;

import cn.edu.zju.bean.User;
import cn.edu.zju.controller.AdminController;
import cn.edu.zju.controller.IndexController;
import cn.edu.zju.controller.KnowledgeBaseController;
import cn.edu.zju.controller.MatchingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatchServlet.class);
    private static final Set<String> PUBLIC_GET_PATHS = Set.of("/", "/register");
    private static final Set<String> PUBLIC_POST_PATHS = Set.of("/login", "/register");
    private static final Set<String> PROFESSIONAL_ONLY_PATHS = Set.of("/drugProfessionalInfo");

    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> getRequestMapping;
    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> postRequestMapping;

    private HttpConsumer<HttpServletRequest, HttpServletResponse> notFound = (request, response) -> {
        try {
            response.getWriter().write("Not Found");
        } catch (IOException e) {
            log.info("", e);
        }
    };

    public class Dispatcher {
        public void registerGetMapping(String path, HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            getRequestMapping.put(path, consumer);
        }
        public void registerPostMapping(String path, HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            postRequestMapping.put(path, consumer);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.getRequestMapping = new ConcurrentHashMap<>();
        this.postRequestMapping = new ConcurrentHashMap<>();

        Dispatcher dispatcher = new Dispatcher();
        IndexController indexController = new IndexController();
        indexController.register(dispatcher);

        KnowledgeBaseController knowledgeBaseController = new KnowledgeBaseController();
        knowledgeBaseController.register(dispatcher);

        MatchingController matchingController = new MatchingController();
        matchingController.register(dispatcher);

        AdminController adminController = new AdminController();
        adminController.register(dispatcher);

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        log.info("{}: {}", req.getMethod(), pathInfo);
        if (!isAuthorized(req, resp, pathInfo)) {
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer = getRequestMapping.getOrDefault(pathInfo, notFound);
        consumer.accept(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = getPathInfo(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer = postRequestMapping.getOrDefault(pathInfo, notFound);
        consumer.accept(req, resp);
    }

    private String getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }
        return pathInfo;
    }

    private boolean isAuthorized(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        if (isPublicRequest(req, pathInfo)) {
            return true;
        }

        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return false;
        }

        if (PROFESSIONAL_ONLY_PATHS.contains(pathInfo) && !hasProfessionalAccess(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Professional users or administrators only.");
            return false;
        }

        return true;
    }

    private boolean isPublicRequest(HttpServletRequest req, String pathInfo) {
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            return PUBLIC_GET_PATHS.contains(pathInfo);
        }
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            return PUBLIC_POST_PATHS.contains(pathInfo);
        }
        return false;
    }

    private User getCurrentUser(HttpServletRequest req) {
        if (req.getSession(false) == null) {
            return null;
        }
        Object currentUser = req.getSession(false).getAttribute("currentUser");
        return currentUser instanceof User ? (User) currentUser : null;
    }

    private boolean hasProfessionalAccess(User user) {
        return user.getPermission() == User.Permission.PROFESSIONAL_USER
                || user.getPermission() == User.Permission.ADMIN;
    }
}
