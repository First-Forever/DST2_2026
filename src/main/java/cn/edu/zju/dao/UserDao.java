package cn.edu.zju.dao;

import cn.edu.zju.bean.User;
import cn.edu.zju.bean.User.Permission;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UserDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public int save(User user) {
        AtomicInteger key = new AtomicInteger();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into app_user(username, password_hash, email, permission, admin_approved, created_at) values (?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPasswordHash());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setString(4, user.getPermission().name());
                preparedStatement.setBoolean(5, user.isAdminApproved());
                preparedStatement.setTimestamp(6, toTimestamp(user.getCreatedAt()));
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                while (generatedKeys.next()) {
                    key.set(generatedKeys.getInt(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return key.get();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, username, password_hash, email, permission, admin_approved, created_at " +
                                "from app_user order by created_at desc, id desc");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return users;
    }

    public User findById(int id) {
        AtomicReference<User> user = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, username, password_hash, email, permission, admin_approved, created_at from app_user where id = ?");
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    user.set(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return user.get();
    }

    public User findByUsername(String username) {
        AtomicReference<User> user = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, username, password_hash, email, permission, admin_approved, created_at from app_user where username = ?");
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    user.set(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return user.get();
    }

    public User findByEmail(String email) {
        AtomicReference<User> user = new AtomicReference<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, username, password_hash, email, permission, admin_approved, created_at from app_user where email = ?");
                preparedStatement.setString(1, email);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    user.set(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return user.get();
    }

    public boolean existsByUsername(String username) {
        return existsByColumn("username", username);
    }

    public boolean existsByEmail(String email) {
        return existsByColumn("email", email);
    }

    public boolean existsByUsernameExceptId(String username, int id) {
        return existsByColumnExceptId("username", username, id);
    }

    public boolean existsByEmailExceptId(String email, int id) {
        return existsByColumnExceptId("email", email, id);
    }

    public List<User> findPendingAdmins() {
        List<User> users = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, username, password_hash, email, permission, admin_approved, created_at from app_user where permission = ? and admin_approved = ?");
                preparedStatement.setString(1, Permission.ADMIN.name());
                preparedStatement.setBoolean(2, false);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return users;
    }

    public boolean hasApprovedAdmin() {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select 1 from app_user where permission = ? and admin_approved = ? limit 1");
                preparedStatement.setString(1, Permission.ADMIN.name());
                preparedStatement.setBoolean(2, true);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    exists.set(true);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return exists.get();
    }

    public boolean approveAdmin(int id) {
        AtomicBoolean updated = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update app_user set admin_approved = ? where id = ? and permission = ?");
                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, id);
                preparedStatement.setString(3, Permission.ADMIN.name());
                updated.set(preparedStatement.executeUpdate() > 0);
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return updated.get();
    }

    public boolean update(User user) {
        AtomicBoolean updated = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update app_user set username = ?, password_hash = ?, email = ?, permission = ?, admin_approved = ? where id = ?");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPasswordHash());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setString(4, user.getPermission().name());
                preparedStatement.setBoolean(5, user.isAdminApproved());
                preparedStatement.setInt(6, user.getId());
                updated.set(preparedStatement.executeUpdate() > 0);
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return updated.get();
    }

    public boolean deleteById(int id) {
        AtomicBoolean deleted = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "delete from app_user where id = ?");
                preparedStatement.setInt(1, id);
                deleted.set(preparedStatement.executeUpdate() > 0);
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return deleted.get();
    }

    private boolean existsByColumn(String columnName, String value) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("select 1 from app_user where %s = ?", columnName));
                preparedStatement.setString(1, value);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    exists.set(true);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return exists.get();
    }

    private boolean existsByColumnExceptId(String columnName, String value, int id) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("select 1 from app_user where %s = ? and id <> ?", columnName));
                preparedStatement.setString(1, value);
                preparedStatement.setInt(2, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    exists.set(true);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return exists.get();
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        String passwordHash = resultSet.getString("password_hash");
        String email = resultSet.getString("email");
        Permission permission = Permission.valueOf(resultSet.getString("permission"));
        boolean adminApproved = resultSet.getBoolean("admin_approved");
        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        Date createdAt = createdAtTimestamp == null ? null : new Date(createdAtTimestamp.getTime());
        return new User(id, username, passwordHash, email, permission, adminApproved, createdAt);
    }

    private Timestamp toTimestamp(Date date) {
        Date createdAt = date == null ? new Date() : date;
        return new Timestamp(createdAt.getTime());
    }
}
