package cn.edu.zju.cmd;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
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
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PharmGKBImporter {

    private static final Logger log = LoggerFactory.getLogger(PharmGKBImporter.class);

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
            throw new IllegalArgumentException("Unknown import target: " + args[0]);
        }
        pharmGKBImporter.importDosingGuideline();
        pharmGKBImporter.importDrug();
        pharmGKBImporter.importDrugLabel();
    }

    private void importDosingGuideline() {
        Gson gson = new Gson();
        List<String> drugLabelsContent = readDataLines("dosingGuideline.data");
        DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();

        drugLabelsContent.forEach(content -> {
            Map guideline = gson.fromJson(content, Map.class);
            Object data = guideline.get("data");
            if (data instanceof List) {
                ((List<Map>) data).forEach(x -> saveDosingGuideline(gson, dosingGuidelineDao, x));
            } else if (data instanceof Map) {
                saveDosingGuideline(gson, dosingGuidelineDao, (Map) data);
            }
        });
    }

    private void saveDosingGuideline(Gson gson, DosingGuidelineDao dosingGuidelineDao, Map data) {
        String id = (String) data.get("id");
        String objCls = (String) data.get("objCls");
        String name = (String) data.get("name");
        boolean recommendation = (Boolean) data.get("recommendation");
        String drugId = ((String) ((List<Map>) data.get("relatedChemicals")).get(0).get("id"));
        String source = (String) data.get("source");
        String summaryMarkdown = ((String) ((Map) data.get("summaryMarkdown")).get("html"));
        String textMarkdown = ((String) ((Map) data.get("textMarkdown")).get("html"));
        String raw = gson.toJson(data);
        DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw);
        if (!dosingGuidelineDao.existsById(id)) {
            dosingGuidelineDao.saveDosingGuideline(dosingGuideline);
            log.info("Saving dosing guideline: {}", id);
        } else {
            log.info("Dosing guideline exists, skipping: {}", id);
        }
    }

    private void importDrug() {
        Gson gson = new Gson();
        String drugsContent = readDataAsString("drugs.data");

        Map drugs = gson.fromJson(drugsContent, Map.class);
        List<Map> drugList = (List<Map>) drugs.get("data");

        DrugDao drugDao = new DrugDao();

        drugList.stream().forEach(x -> {
            log.info("{}", x);
            Map drug = ((Map) x.get("drug"));
            String id = (String) drug.get("id");
            String name = (String) drug.get("name");
            String objCls = (String) drug.get("objCls");
            String drugUrl = (String) x.get("drugUrl");
            boolean biomarker = ((Boolean) x.get("biomarker"));
            Drug drugBean = new Drug(id, name, biomarker, drugUrl, objCls);

            drugDao.saveDrug(drugBean);
        });
    }

    private void importDrugLabel() {
        Gson gson = new Gson();
        List<String> drugLabelsContent = readDataLines("drugLabels.data");


        DrugLabelDao drugLabelDao = new DrugLabelDao();
        int importedCount = 0;
        int efficacyCount = 0;
        int responseWarningCount = 0;
        int alternativeDrugCount = 0;

        for (String line : drugLabelsContent) {
            Map x = gson.fromJson(line, Map.class);
            log.info("Going to save label: {}", (String) x.get("id"));
            String labelId = (String) x.get("id");
            String name = (String) x.get("name");
            String objCls = (String) x.get("objCls");
            boolean alternateDrugAvailable = (Boolean) x.get("alternateDrugAvailable");
            boolean dosingInformation = (Boolean) x.get("dosingInformation");
            String prescribingMarkdown = "";
            if (x.containsKey("prescribingMarkdown")) {
                prescribingMarkdown = getMarkdownHtml(x, "prescribingMarkdown");
            }
            String source = (String) x.get("source");
            String textMarkdown = getMarkdownHtml(x, "textMarkdown");
            String summaryMarkdown = getMarkdownHtml(x, "summaryMarkdown");
            String efficacySummary = (String) x.get("efficacy_summary");
            String responseWarning = (String) x.get("response_warning");
            String alternativeDrug = (String) x.get("alternative_drug");
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

    private List<String> readDataLines(String fileName) {
        try {
            Path workspacePath = new File(fileName).toPath();
            if (Files.exists(workspacePath)) {
                return Files.readAllLines(workspacePath, StandardCharsets.UTF_8);
            }

            InputStream is = getClass().getResourceAsStream("/" + fileName);
            if (is == null) {
                throw new IllegalStateException("Cannot find data file: " + fileName);
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return bufferedReader.lines().collect(toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data file: " + fileName, e);
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
        return html instanceof String ? (String) html : "";
    }

    private String getFirstRelatedChemicalId(Map data) {
        Object relatedChemicals = data.get("relatedChemicals");
        if (!(relatedChemicals instanceof List) || ((List) relatedChemicals).isEmpty()) {
            return "";
        }
        Object firstChemical = ((List) relatedChemicals).get(0);
        if (!(firstChemical instanceof Map)) {
            return "";
        }
        Object id = ((Map) firstChemical).get("id");
        return id instanceof String ? (String) id : "";
    }
}
