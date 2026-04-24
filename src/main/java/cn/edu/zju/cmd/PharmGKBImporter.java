package cn.edu.zju.cmd;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.DrugProfessionalInfo;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.DrugProfessionalInfoDao;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class PharmGKBImporter {

    private static final Logger log = LoggerFactory.getLogger(PharmGKBImporter.class);
    private static final int PROGRESS_LOG_INTERVAL = 200;

    public static void main(String[] args) {
        PharmGKBImporter pharmGKBImporter = new PharmGKBImporter();
        if (args.length > 0) {
            String importTarget = args[0].trim().toLowerCase();
            if ("labels".equals(importTarget) || "drug-labels".equals(importTarget)) {
                pharmGKBImporter.importDrugLabel();
                return;
            }
            if ("drugs".equals(importTarget)) {
                pharmGKBImporter.importDrug();
                return;
            }
            if ("guidelines".equals(importTarget) || "dosing-guidelines".equals(importTarget)) {
                pharmGKBImporter.importDosingGuideline();
                return;
            }
            if ("professional-info".equals(importTarget) || "drug-professional-info".equals(importTarget)) {
                pharmGKBImporter.importDrugProfessionalInfo();
                return;
            }
            throw new IllegalArgumentException("Unknown import target: " + args[0]);
        }
        pharmGKBImporter.importDrug();
        pharmGKBImporter.importDrugLabel();
        pharmGKBImporter.importDosingGuideline();
        pharmGKBImporter.importDrugProfessionalInfo();
    }

    private void importDosingGuideline() {
        Gson gson = new Gson();
        List<String> drugLabelsContent = readDataLines("dosingGuidelines.data", "dosingGuideline.data");
        DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
        int importedCount = 0;
        int skippedCount = 0;

        log.info("Starting dosing guideline import. total={}", drugLabelsContent.size());

        for (String content : drugLabelsContent) {
            Map guideline = gson.fromJson(content, Map.class);
            if (guideline == null || guideline.isEmpty()) {
                continue;
            }

            if (guideline.containsKey("data")) {
                Object data = guideline.get("data");
                if (data instanceof List) {
                    for (Map item : (List<Map>) data) {
                        if (saveDosingGuideline(gson, dosingGuidelineDao, item, guideline)) {
                            importedCount++;
                        } else {
                            skippedCount++;
                        }
                    }
                } else if (data instanceof Map) {
                    if (saveDosingGuideline(gson, dosingGuidelineDao, (Map) data, guideline)) {
                        importedCount++;
                    } else {
                        skippedCount++;
                    }
                }
                continue;
            }

            if (saveDosingGuideline(gson, dosingGuidelineDao, guideline, guideline)) {
                importedCount++;
            } else {
                skippedCount++;
            }
        }
        log.info("Dosing guideline import finished. total={}, imported={}, skipped={}",
                drugLabelsContent.size(), importedCount, skippedCount);
    }

    private boolean saveDosingGuideline(Gson gson, DosingGuidelineDao dosingGuidelineDao, Map data, Map rawSource) {
        String id = stringValue(data.get("id"));
        if (!hasText(id)) {
            return false;
        }

        if (dosingGuidelineDao.existsById(id)) {
            log.info("Dosing guideline exists, skipping: {}", id);
            return false;
        }

        String objCls = firstNonBlank(stringValue(data.get("obj_cls")), stringValue(data.get("objCls")));
        String name = stringValue(data.get("name"));
        boolean recommendation = boolValue(data.get("recommendation"));
        String drugId = firstNonBlank(stringValue(data.get("drug_id")), getFirstRelatedChemicalId(data));
        String source = stringValue(data.get("source"));
        String summaryMarkdown = firstNonBlank(stringValue(data.get("summary_markdown")), getMarkdownHtml(data, "summaryMarkdown"));
        String textMarkdown = firstNonBlank(stringValue(data.get("text_markdown")), getMarkdownHtml(data, "textMarkdown"));
        String raw = firstNonBlank(stringValue(data.get("raw")), gson.toJson(rawSource));
        DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw);
        dosingGuidelineDao.saveDosingGuideline(dosingGuideline);
        log.info("Saving dosing guideline: {}", id);
        return true;
    }

    private void importDrug() {
        Gson gson = new Gson();
        String drugsContent = readDataAsString("drugs.data");

        Map drugs = gson.fromJson(drugsContent, Map.class);
        List<Map> drugList = (List<Map>) drugs.get("data");

        DrugDao drugDao = new DrugDao();
        int importedCount = 0;
        int skippedCount = 0;
        Set<String> seenDrugIds = new LinkedHashSet<>();

        log.info("Starting drug import. total={}", drugList.size());

        for (Map x : drugList) {
            log.info("{}", x);
            Map drug = ((Map) x.get("drug"));
            if (drug != null) {
                if (saveDrugRecord(
                        drugDao,
                        seenDrugIds,
                        stringValue(drug.get("id")),
                        stringValue(drug.get("name")),
                        stringValue(drug.get("objCls")),
                        stringValue(x.get("drugUrl")),
                        boolValue(x.get("biomarker"))
                )) {
                    importedCount++;
                } else {
                    skippedCount++;
                }
                continue;
            }

            List<Map> relatedChemicals = getRelatedChemicals(x);
            if (relatedChemicals.isEmpty()) {
                skippedCount++;
                continue;
            }

            for (Map chemical : relatedChemicals) {
                if (saveDrugRecord(
                        drugDao,
                        seenDrugIds,
                        stringValue(chemical.get("id")),
                        firstNonBlank(stringValue(chemical.get("name")), stringValue(chemical.get("term"))),
                        firstNonBlank(stringValue(chemical.get("objCls")), "Chemical"),
                        buildDrugUrl(stringValue(chemical.get("id"))),
                        hasText(stringValue(x.get("biomarkerStatus")))
                )) {
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }
        }
        log.info("Drug import finished. total={}, imported={}, skipped={}", drugList.size(), importedCount, skippedCount);
    }

    private void importDrugLabel() {
        Gson gson = new Gson();
        List<String> drugLabelsContent = readDataLines("drugLabels.data");


        DrugLabelDao drugLabelDao = new DrugLabelDao();
        int importedCount = 0;
        int efficacyCount = 0;
        int responseWarningCount = 0;
        int alternativeDrugCount = 0;

        log.info("Starting drug label import. total={}", drugLabelsContent.size());

        for (String line : drugLabelsContent) {
            Map x = gson.fromJson(line, Map.class);
            String labelId = stringValue(x.get("id"));
            if (!hasText(labelId)) {
                continue;
            }
            if (drugLabelDao.existsById(labelId)) {
                log.info("Drug label exists, skipping: {}", labelId);
                continue;
            }

            log.info("Going to save label: {}", labelId);
            String name = stringValue(x.get("name"));
            String objCls = stringValue(x.get("objCls"));
            boolean alternateDrugAvailable = boolValue(x.get("alternateDrugAvailable"));
            boolean dosingInformation = boolValue(x.get("dosingInformation"));
            String prescribingMarkdown = getMarkdownHtml(x, "prescribingMarkdown");
            String source = stringValue(x.get("source"));
            String textMarkdown = getMarkdownHtml(x, "textMarkdown");
            String summaryMarkdown = getMarkdownHtml(x, "summaryMarkdown");
            String efficacySummary = stringValue(x.get("efficacy_summary"));
            String responseWarning = stringValue(x.get("response_warning"));
            String alternativeDrug = stringValue(x.get("alternative_drug"));
            if (hasText(efficacySummary)) {
                efficacyCount++;
            }
            if (hasText(responseWarning)) {
                responseWarningCount++;
            }
            if (hasText(alternativeDrug)) {
                alternativeDrugCount++;
            }
            String raw = gson.toJson(x);
            String drugId = getFirstRelatedChemicalId(x);
            DrugLabel drugLabelBean = new DrugLabel(labelId, name, objCls, alternateDrugAvailable, dosingInformation
                    , prescribingMarkdown, source, textMarkdown, summaryMarkdown, efficacySummary, responseWarning, alternativeDrug, raw, drugId);
            drugLabelDao.saveDrugLabel(drugLabelBean);
            importedCount++;
            log.info("Imported: {}", labelId);
        }
        log.info("Drug label import finished. total={}, imported={}, efficacy_summary={}, response_warning={}, alternative_drug={}",
                drugLabelsContent.size(), importedCount, efficacyCount, responseWarningCount, alternativeDrugCount);
    }

    private void importDrugProfessionalInfo() {
        Gson gson = new Gson();
        List<String> contents = readDataLines("drugProfessionalInfo.data");
        DrugProfessionalInfoDao drugProfessionalInfoDao = new DrugProfessionalInfoDao();
        int importedCount = 0;
        int skippedCount = 0;
        int processedCount = 0;

        log.info("Starting drug professional info import. total={}", contents.size());

        for (String line : contents) {
            processedCount++;
            Map record = gson.fromJson(line, Map.class);
            if (record == null || record.isEmpty()) {
                logProgress(processedCount, contents.size(), importedCount, skippedCount);
                continue;
            }

            DrugProfessionalInfo drugProfessionalInfo = buildDrugProfessionalInfo(record);
            if (drugProfessionalInfo == null) {
                skippedCount++;
                logProgress(processedCount, contents.size(), importedCount, skippedCount);
                continue;
            }

            if (drugProfessionalInfoDao.existsByNaturalKey(
                    drugProfessionalInfo.getDrugName(),
                    drugProfessionalInfo.getSourceType(),
                    drugProfessionalInfo.getSourceUrl(),
                    drugProfessionalInfo.getLiteratureSummary()
            )) {
                skippedCount++;
                logProgress(processedCount, contents.size(), importedCount, skippedCount);
                continue;
            }

            drugProfessionalInfoDao.saveDrugProfessionalInfo(drugProfessionalInfo);
            importedCount++;
            logProgress(processedCount, contents.size(), importedCount, skippedCount);
        }

        log.info("Drug professional info import finished. total={}, imported={}, skipped={}",
                contents.size(), importedCount, skippedCount);
    }

    private DrugProfessionalInfo buildDrugProfessionalInfo(Map record) {
        String drugName = stringValue(record.get("drug_name"));
        String sourceType = stringValue(record.get("source_type"));
        if (!hasText(drugName) || !hasText(sourceType)) {
            return null;
        }

        return new DrugProfessionalInfo(
                drugName,
                stringValue(record.get("related_genes")),
                sourceType,
                stringValue(record.get("evidence_level")),
                stringValue(record.get("guideline_or_label_tags")),
                stringValue(record.get("literature_summary")),
                stringValue(record.get("pmid_list")),
                stringValue(record.get("source_url"))
        );
    }

    private List<String> readDataLines(String... fileNames) {
        try {
            for (String fileName : fileNames) {
                Path workspacePath = new File(fileName).toPath();
                if (Files.exists(workspacePath)) {
                    return Files.readAllLines(workspacePath, StandardCharsets.UTF_8)
                            .stream()
                            .filter(line -> line != null && !line.isBlank())
                            .collect(Collectors.toList());
                }

                InputStream is = getClass().getResourceAsStream("/" + fileName);
                if (is == null) {
                    continue;
                }
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return bufferedReader.lines()
                            .filter(line -> line != null && !line.isBlank())
                            .collect(toList());
                }
            }
            throw new IllegalStateException("Cannot find data file: " + Arrays.toString(fileNames));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data file: " + Arrays.toString(fileNames), e);
        }
    }

    private String readDataAsString(String fileName) {
        return String.join("\n", readDataLines(fileName));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String getMarkdownHtml(Map data, String key) {
        Object markdown = data.get(key);
        if (!(markdown instanceof Map)) {
            return "";
        }
        Object html = ((Map) markdown).get("html");
        return html instanceof String ? (String) html : null;
    }

    private String getFirstRelatedChemicalId(Map data) {
        List<Map> relatedChemicals = getRelatedChemicals(data);
        if (relatedChemicals.isEmpty()) {
            return null;
        }
        return stringValue(relatedChemicals.get(0).get("id"));
    }

    private List<Map> getRelatedChemicals(Map data) {
        Object relatedChemicals = data.get("relatedChemicals");
        if (!(relatedChemicals instanceof List) || ((List) relatedChemicals).isEmpty()) {
            return List.of();
        }

        List<Map> results = new java.util.ArrayList<>();
        for (Object item : (List) relatedChemicals) {
            if (item instanceof Map) {
                results.add((Map) item);
            }
        }
        return results;
    }

    private boolean saveDrugRecord(DrugDao drugDao, Set<String> seenDrugIds, String id, String name, String objCls, String drugUrl, boolean biomarker) {
        if (!hasText(id) || !seenDrugIds.add(id) || drugDao.existsById(id)) {
            return false;
        }

        Drug drugBean = new Drug(id, name, biomarker, drugUrl, objCls);
        drugDao.saveDrug(drugBean);
        return true;
    }

    private String buildDrugUrl(String drugId) {
        if (!hasText(drugId)) {
            return null;
        }
        return "https://www.pharmgkb.org/chemical/" + drugId;
    }

    private boolean boolValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void logProgress(int processedCount, int totalCount, int importedCount, int skippedCount) {
        if (processedCount % PROGRESS_LOG_INTERVAL == 0 || processedCount == totalCount) {
            log.info("Drug professional info import progress: {}/{} processed, imported={}, skipped={}",
                    processedCount, totalCount, importedCount, skippedCount);
        }
    }
}
