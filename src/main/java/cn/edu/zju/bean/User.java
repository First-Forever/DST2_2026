package cn.edu.zju.bean;

import java.util.Date;
import java.util.Locale;

public class User {
    public enum Permission {
        NORMAL_USER,
        PROFESSIONAL_USER,
        ADMIN
    }

    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private Permission permission = Permission.NORMAL_USER;
    private boolean adminApproved;
    private Date createdAt;

    public User() {
    }

    public User(int id, String username, String passwordHash, String email, Date createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        setEmail(email);
        this.createdAt = createdAt;
    }

    public User(int id, String username, String passwordHash, String email, Permission permission, boolean adminApproved,
            Date createdAt) {
        this(id, username, passwordHash, email, permission, adminApproved, createdAt, true);
    }

    private User(int id, String username, String passwordHash, String email, Permission permission, boolean adminApproved,
            Date createdAt, boolean enforcePermissionEmailValidation) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.permission = permission == null ? Permission.NORMAL_USER : permission;
        validatePermissionEmail(this.permission, email, enforcePermissionEmailValidation);
        this.email = email;
        this.adminApproved = requiresApproval(this.permission) && adminApproved;
        this.createdAt = createdAt;
    }

    public static User createByAdmin(int id, String username, String passwordHash, String email, Permission permission,
            boolean adminApproved, Date createdAt) {
        return new User(id, username, passwordHash, email, permission, adminApproved, createdAt, false);
    }

    public static User fromPersistence(int id, String username, String passwordHash, String email, Permission permission,
            boolean adminApproved, Date createdAt) {
        return new User(id, username, passwordHash, email, permission, adminApproved, createdAt, false);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validatePermissionEmail(permission, email, true);
        this.email = email;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        Permission targetPermission = permission == null ? Permission.NORMAL_USER : permission;
        validatePermissionEmail(targetPermission, email, true);
        this.permission = targetPermission;
        if (!requiresApproval(targetPermission)) {
            this.adminApproved = false;
        }
    }

    public boolean isAdminApproved() {
        return adminApproved;
    }

    public void setAdminApproved(boolean adminApproved) {
        this.adminApproved = requiresApproval(permission) && adminApproved;
    }

    public boolean isElevatedPermission() {
        return requiresApproval(permission);
    }

    public boolean isPermissionApproved() {
        return !requiresApproval(permission) || adminApproved;
    }

    public boolean isRoleApplicationPending() {
        return requiresApproval(permission) && !adminApproved;
    }

    public String getPermissionLabel() {
        switch (permission) {
            case PROFESSIONAL_USER:
                return "Professional user";
            case ADMIN:
                return "Administrator";
            case NORMAL_USER:
            default:
                return "Normal user";
        }
    }

    public String getApprovalStatusLabel() {
        if (!requiresApproval(permission)) {
            return "Not required";
        }
        return adminApproved ? "Approved" : "Pending approval";
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    private void validatePermissionEmail(Permission permission, String email, boolean enforcePermissionEmailValidation) {
        if (!enforcePermissionEmailValidation) {
            return;
        }
        if (permission == Permission.ADMIN && email != null
                && !isZjuEmail(email)) {
            throw new IllegalArgumentException("Administrators must use a ZJU email");
        }
    }

    private static boolean requiresApproval(Permission permission) {
        return permission == Permission.PROFESSIONAL_USER || permission == Permission.ADMIN;
    }

    public static boolean isZjuEmail(String email) {
        if (email == null) {
            return false;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        int atIndex = normalizedEmail.lastIndexOf('@');
        if (atIndex < 0 || atIndex == normalizedEmail.length() - 1) {
            return false;
        }
        String domain = normalizedEmail.substring(atIndex + 1);
        return "zju.edu.cn".equals(domain) || domain.endsWith(".zju.edu.cn");
    }
}
