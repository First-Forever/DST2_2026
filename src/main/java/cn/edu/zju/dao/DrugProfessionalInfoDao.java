package cn.edu.zju.dao;

import cn.edu.zju.bean.DrugProfessionalInfo;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DrugProfessionalInfoDao {

    private static final Logger log = LoggerFactory.getLogger(DrugProfessionalInfoDao.class);

    public void saveDrugProfessionalInfo(DrugProfessionalInfo drugProfessionalInfo) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into drug_professional_info (drug_name, related_genes, source_type, evidence_level, guideline_or_label_tags, literature_summary, pmid_list, source_url) values (?,?,?,?,?,?,?,?)"
                );
                preparedStatement.setString(1, drugProfessionalInfo.getDrugName());
                preparedStatement.setString(2, drugProfessionalInfo.getRelatedGenes());
                preparedStatement.setString(3, drugProfessionalInfo.getSourceType());
                preparedStatement.setString(4, drugProfessionalInfo.getEvidenceLevel());
                preparedStatement.setString(5, drugProfessionalInfo.getGuidelineOrLabelTags());
                preparedStatement.setString(6, drugProfessionalInfo.getLiteratureSummary());
                preparedStatement.setString(7, drugProfessionalInfo.getPmidList());
                preparedStatement.setString(8, drugProfessionalInfo.getSourceUrl());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public boolean existsByNaturalKey(String drugName, String sourceType, String sourceUrl, String literatureSummary) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select 1 from drug_professional_info where drug_name = ? and source_type = ? " +
                                "and ((source_url = ?) or (source_url is null and ? is null)) " +
                                "and ((literature_summary = ?) or (literature_summary is null and ? is null))"
                );
                preparedStatement.setString(1, drugName);
                preparedStatement.setString(2, sourceType);
                preparedStatement.setString(3, sourceUrl);
                preparedStatement.setString(4, sourceUrl);
                preparedStatement.setString(5, literatureSummary);
                preparedStatement.setString(6, literatureSummary);
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

    public List<DrugProfessionalInfo> findAll() {
        return findByFilters(null, null, null);
    }

    public List<DrugProfessionalInfo> findByKeyword(String keyword) {
        return findByFilters(keyword, null, null);
    }

    public List<DrugProfessionalInfo> findByFilters(String keyword, String sourceType, String evidenceLevel) {
        List<DrugProfessionalInfo> drugProfessionalInfos = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                List<String> parameters = new ArrayList<>();
                StringBuilder sql = new StringBuilder(
                        "select id, drug_name, related_genes, source_type, evidence_level, guideline_or_label_tags, literature_summary, pmid_list, source_url " +
                                "from drug_professional_info where 1 = 1"
                );

                if (notBlank(keyword)) {
                    sql.append(" and (cast(id as char) like ? or drug_name like ? or related_genes like ? or source_type like ? or evidence_level like ? or guideline_or_label_tags like ? or literature_summary like ? or pmid_list like ? or source_url like ?)");
                    String likeKeyword = "%" + keyword.trim() + "%";
                    for (int i = 0; i < 9; i++) {
                        parameters.add(likeKeyword);
                    }
                }
                if (notBlank(sourceType)) {
                    sql.append(" and source_type = ?");
                    parameters.add(sourceType.trim());
                }
                if (notBlank(evidenceLevel)) {
                    sql.append(" and evidence_level = ?");
                    parameters.add(evidenceLevel.trim());
                }

                sql.append(" order by id");

                PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setString(i + 1, parameters.get(i));
                }

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    drugProfessionalInfos.add(buildDrugProfessionalInfo(resultSet));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugProfessionalInfos;
    }

    public List<String> findAllSourceTypes() {
        return findDistinctColumnValues("source_type");
    }

    public List<String> findAllEvidenceLevels() {
        return findDistinctColumnValues("evidence_level");
    }

    private List<String> findDistinctColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select distinct " + columnName + " from drug_professional_info " +
                                "where " + columnName + " is not null and trim(" + columnName + ") <> '' " +
                                "order by " + columnName
                );
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    values.add(resultSet.getString(1));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return values;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private DrugProfessionalInfo buildDrugProfessionalInfo(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String drugName = resultSet.getString("drug_name");
        String relatedGenes = resultSet.getString("related_genes");
        String sourceType = resultSet.getString("source_type");
        String evidenceLevel = resultSet.getString("evidence_level");
        String guidelineOrLabelTags = resultSet.getString("guideline_or_label_tags");
        String literatureSummary = resultSet.getString("literature_summary");
        String pmidList = resultSet.getString("pmid_list");
        String sourceUrl = resultSet.getString("source_url");
        return new DrugProfessionalInfo(id, drugName, relatedGenes, sourceType, evidenceLevel,
                guidelineOrLabelTags, literatureSummary, pmidList, sourceUrl);
    }
}
