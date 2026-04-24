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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private UserDao userDao = new UserDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/admin/users", this::userManagement);
        dispatcher.registerPostMapping("/admin/users/create", this::createUser);
        dispatcher.registerPostMapping("/admin/users/update", this::updateUser);
        dispatcher.registerPostMapping("/admin/users/delete", this::deleteUser);
        dispatcher.registerPostMapping("/admin/users/approve", this::approveAdminApplication);
    }

    public void userManagement(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadUserManagementPage(request);
        request.getRequestDispatcher("/views/user_management.jsp").forward(request, response);
    }

    public void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String username = trimToEmpty(request.getParameter("username"));
        String email = trimToEmpty(request.getParameter("email"));
        String password = trimToEmpty(request.getParameter("password"));
        String permissionValue = trimToEmpty(request.getParameter("permission"));

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            redirectToUserManagement(response, request, null, "Username, email and password are required.");
            return;
        }
        if (userDao.existsByUsername(username)) {
            redirectToUserManagement(response, request, null, "Username already exists.");
            return;
        }
        if (userDao.existsByEmail(email)) {
            redirectToUserManagement(response, request, null, "Email already exists.");
            return;
        }

        try {
            Permission permission = permissionValue.isEmpty() ? Permission.NORMAL_USER : Permission.valueOf(permissionValue);
            boolean adminApproved = permission == Permission.ADMIN;
            User user = new User(0, username, hashPassword(password), email, permission, adminApproved, new Date());
            int id = userDao.save(user);
            if (id == 0) {
                redirectToUserManagement(response, request, null, "Failed to create user.");
                return;
            }
            String successMessage = permission == Permission.ADMIN
                    ? "Administrator created and approved."
                    : "User created successfully.";
            redirectToUserManagement(response, request, successMessage, null);
        } catch (IllegalArgumentException e) {
            redirectToUserManagement(response, request, null, e.getMessage());
        }
    }

    public void updateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        request.setCharacterEncoding("UTF-8");

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            redirectToUserManagement(response, request, null, "Invalid user id.");
            return;
        }

        User existingUser = userDao.findById(id);
        if (existingUser == null) {
            redirectToUserManagement(response, request, null, "User not found.");
            return;
        }

        String username = trimToEmpty(request.getParameter("username"));
        String email = trimToEmpty(request.getParameter("email"));
        String password = trimToEmpty(request.getParameter("password"));
        String permissionValue = trimToEmpty(request.getParameter("permission"));

        if (username.isEmpty() || email.isEmpty()) {
            redirectToUserManagement(response, request, null, "Username and email are required.");
            return;
        }
        if (userDao.existsByUsernameExceptId(username, id)) {
            redirectToUserManagement(response, request, null, "Username already exists.");
            return;
        }
        if (userDao.existsByEmailExceptId(email, id)) {
            redirectToUserManagement(response, request, null, "Email already exists.");
            return;
        }

        try {
            Permission permission = permissionValue.isEmpty() ? existingUser.getPermission() : Permission.valueOf(permissionValue);
            String passwordHash = password.isEmpty() ? existingUser.getPasswordHash() : hashPassword(password);
            boolean adminApproved = permission == Permission.ADMIN;
            User updatedUser = new User(id, username, passwordHash, email, permission, adminApproved, existingUser.getCreatedAt());
            if (!userDao.update(updatedUser)) {
                redirectToUserManagement(response, request, null, "Failed to update user.");
                return;
            }

            User currentUser = getCurrentUser(request);
            if (currentUser != null && currentUser.getId() == id) {
                request.getSession().setAttribute("currentUser", userDao.findById(id));
            }
            redirectToUserManagement(response, request, "User updated successfully.", null);
        } catch (IllegalArgumentException e) {
            redirectToUserManagement(response, request, null, e.getMessage());
        }
    }

    public void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            redirectToUserManagement(response, request, null, "Invalid user id.");
            return;
        }

        User currentUser = getCurrentUser(request);
        if (currentUser != null && currentUser.getId() == id) {
            redirectToUserManagement(response, request, null, "You cannot delete your own account.");
            return;
        }
        if (!userDao.deleteById(id)) {
            redirectToUserManagement(response, request, null, "Failed to delete user.");
            return;
        }
        redirectToUserManagement(response, request, "User deleted successfully.", null);
    }

    public void approveAdminApplication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            redirectToUserManagement(response, request, null, "Invalid user id.");
            return;
        }
        if (!userDao.approveAdmin(id)) {
            redirectToUserManagement(response, request, null, "Failed to approve administrator application.");
            return;
        }
        redirectToUserManagement(response, request, "Administrator application approved.", null);
    }

    private void loadUserManagementPage(HttpServletRequest request) {
        request.setAttribute("users", userDao.findAll());
        request.setAttribute("pendingAdmins", userDao.findPendingAdmins());
        request.setAttribute("permissionOptions", Permission.values());
        request.setAttribute("successMessage", trimToNull(request.getParameter("successMessage")));
        request.setAttribute("errorMessage", trimToNull(request.getParameter("errorMessage")));
    }

    private void redirectToUserManagement(HttpServletResponse response, HttpServletRequest request, String successMessage, String errorMessage)
            throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/admin/users");
        boolean hasQuery = false;
        if (successMessage != null && !successMessage.isBlank()) {
            url.append("?successMessage=").append(encode(successMessage));
            hasQuery = true;
        }
        if (errorMessage != null && !errorMessage.isBlank()) {
            url.append(hasQuery ? "&" : "?").append("errorMessage=").append(encode(errorMessage));
        }
        response.sendRedirect(url.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Integer parseId(String value) {
        try {
            return value == null ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || currentUser.getPermission() != Permission.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrators only.");
            return false;
        }
        return true;
    }

    private User getCurrentUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object currentUser = request.getSession(false).getAttribute("currentUser");
        return currentUser instanceof User ? (User) currentUser : null;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
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
