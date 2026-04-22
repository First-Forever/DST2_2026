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
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.permission = permission == null ? Permission.NORMAL_USER : permission;
        setEmail(email);
        this.adminApproved = this.permission == Permission.ADMIN && adminApproved;
        this.createdAt = createdAt;
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
        validatePermissionEmail(permission, email);
        this.email = email;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        Permission targetPermission = permission == null ? Permission.NORMAL_USER : permission;
        validatePermissionEmail(targetPermission, email);
        this.permission = targetPermission;
        if (targetPermission != Permission.ADMIN) {
            this.adminApproved = false;
        }
    }

    public boolean isAdminApproved() {
        return adminApproved;
    }

    public void setAdminApproved(boolean adminApproved) {
        this.adminApproved = permission == Permission.ADMIN && adminApproved;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    private void validatePermissionEmail(Permission permission, String email) {
        if ((permission == Permission.PROFESSIONAL_USER || permission == Permission.ADMIN) && email != null
                && !isZjuEmail(email)) {
            throw new IllegalArgumentException("Professional users and administrators must use a ZJU email");
        }
    }

    private boolean isZjuEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT).endsWith("zju.edu.cn");
    }
}
