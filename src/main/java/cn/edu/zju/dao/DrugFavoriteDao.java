package cn.edu.zju.dao;

import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DrugFavoriteDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DrugFavoriteDao.class);

    public boolean addFavorite(int userId, String drugId) {
        AtomicBoolean saved = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert ignore into drug_favorite (user_id, drug_id) values (?, ?)");
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, drugId);
                preparedStatement.executeUpdate();
                saved.set(true);
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return saved.get();
    }

    public boolean removeFavorite(int userId, String drugId) {
        AtomicBoolean deleted = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "delete from drug_favorite where user_id = ? and drug_id = ?");
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, drugId);
                deleted.set(preparedStatement.executeUpdate() > 0);
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return deleted.get();
    }

    public boolean isFavorite(int userId, String drugId) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select 1 from drug_favorite where user_id = ? and drug_id = ? limit 1");
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, drugId);
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

    public Set<String> findFavoriteDrugIdsByUserId(int userId) {
        Set<String> drugIds = new LinkedHashSet<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select drug_id from drug_favorite where user_id = ? order by created_at desc");
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    drugIds.add(resultSet.getString("drug_id"));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugIds;
    }

    public Set<String> findFavoriteDrugNamesByUserId(int userId) {
        Set<String> drugNames = new LinkedHashSet<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select d.name from drug d inner join drug_favorite f on f.drug_id = d.id " +
                                "where f.user_id = ? and d.name is not null order by f.created_at desc");
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String drugName = resultSet.getString("name");
                    if (drugName != null && !drugName.trim().isEmpty()) {
                        drugNames.add(drugName.trim().toLowerCase(Locale.ROOT));
                    }
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugNames;
    }
}
