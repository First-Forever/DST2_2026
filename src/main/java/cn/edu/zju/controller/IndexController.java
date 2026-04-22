package cn.edu.zju.controller;

import cn.edu.zju.bean.User;
import cn.edu.zju.bean.User.Permission;
import cn.edu.zju.dao.UserDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private UserDao userDao = new UserDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/", this::index);
        dispatcher.registerGetMapping("/dashboard", this::dashboard);
        dispatcher.registerGetMapping("/register", this::register);
        dispatcher.registerPostMapping("/login", this::login);
        dispatcher.registerPostMapping("/register", this::doRegister);
    }

    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/welcome.jsp").forward(request, response);
    }

    public void dashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/index.jsp").forward(request, response);
    }

    public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/register.jsp").forward(request, response);
    }

    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String account = trimToEmpty(request.getParameter("account"));
        String password = trimToEmpty(request.getParameter("password"));
        if (account.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Username/email and password are required.");
            request.getRequestDispatcher("/views/welcome.jsp").forward(request, response);
            return;
        }

        User user = account.contains("@") ? userDao.findByEmail(account) : userDao.findByUsername(account);
        if (user == null || !user.getPasswordHash().equals(hashPassword(password))) {
            request.setAttribute("errorMessage", "Invalid username/email or password.");
            request.getRequestDispatcher("/views/welcome.jsp").forward(request, response);
            return;
        }
        if (user.getPermission() == Permission.ADMIN && !user.isAdminApproved()) {
            request.setAttribute("errorMessage", "Administrator permission is waiting for approval.");
            request.getRequestDispatcher("/views/welcome.jsp").forward(request, response);
            return;
        }

        request.getSession().setAttribute("currentUser", user);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    public void doRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = trimToEmpty(request.getParameter("username"));
        String email = trimToEmpty(request.getParameter("email"));
        String password = trimToEmpty(request.getParameter("password"));
        String permissionValue = trimToEmpty(request.getParameter("permission"));

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Username, email and password are required.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }
        if (userDao.existsByUsername(username)) {
            request.setAttribute("errorMessage", "Username already exists.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }
        if (userDao.existsByEmail(email)) {
            request.setAttribute("errorMessage", "Email already exists.");
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }

        try {
            Permission permission = permissionValue.isEmpty() ? Permission.NORMAL_USER : Permission.valueOf(permissionValue);
            boolean bootstrapAdmin = permission == Permission.ADMIN && !userDao.hasApprovedAdmin();
            User user = new User(0, username, hashPassword(password), email, permission, false, new Date());
            int id = userDao.save(user);
            if (id == 0) {
                request.setAttribute("errorMessage", "Registration failed. Please try again.");
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            }
            if (bootstrapAdmin) {
                userDao.approveAdmin(id);
                request.setAttribute("successMessage", "Initial administrator registered and approved. Please sign in.");
            } else if (permission == Permission.ADMIN) {
                request.setAttribute("successMessage", "Registration successful. Administrator permission is waiting for approval.");
            } else {
                request.setAttribute("successMessage", "Registration successful. Please sign in.");
            }
            request.getRequestDispatcher("/views/welcome.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported", e);
        }
    }
}
