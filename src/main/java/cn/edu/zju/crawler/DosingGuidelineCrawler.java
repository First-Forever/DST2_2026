package cn.edu.zju.crawler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DosingGuidelineCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineCrawler.class);

    public static final String URL_BASE = "https://api.pharmgkb.org/v1/data%s";
    public static final String URL_GUIDELINES = "https://api.pharmgkb.org/v1/site/guidelinesByDrugs";

    private final Gson gson = new Gson();
    private final Path dosingGuidelinesPath = new File("dosingGuidelines.data").toPath();

    public void doCrawlerDosingGuidelineList() {
        resetOutputFile();

        Map response = fetchMap(URL_GUIDELINES);
        Set<String> seenGuidelineUrls = new LinkedHashSet<>();

        for (Map drugGroup : this.mapList(response.get("data"))) {
            List.of("cpic", "cpnds", "dpwg", "fda", "pro").forEach(source -> {
                List<Map> guidelineList = this.mapList(drugGroup.get(source));
                if (guidelineList.isEmpty()) {
                    return;
                }

                guidelineList.forEach(guidelineSummary -> {
                    String url = this.stringValue(guidelineSummary.get("url"));
                    if (url == null || url.isBlank() || !seenGuidelineUrls.add(url)) {
                        return;
                    }
                    this.doCrawlerDosingGuideline(url);
                });
            });
        }
    }

    public void doCrawlerDosingGuideline(String url) {
        Map guidelineResponse = fetchMap(String.format(URL_BASE, url));
        Map data = this.mapValue(guidelineResponse.get("data"));
        if (data.isEmpty()) {
            log.warn("Skipping dosing guideline because response data is empty: {}", url);
            return;
        }

        String id = this.stringValue(data.get("id"));
        if (id == null || id.isBlank()) {
            log.warn("Skipping dosing guideline because id is missing: {}", url);
            return;
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", id);
        record.put("obj_cls", this.stringValue(data.get("objCls")));
        record.put("name", this.stringValue(data.get("name")));
        record.put("recommendation", Boolean.TRUE.equals(data.get("recommendation")));
        record.put("drug_id", firstRelatedChemicalId(data));
        record.put("source", this.stringValue(data.get("source")));
        record.put("summary_markdown", this.getMarkdownHtml(data, "summaryMarkdown"));
        record.put("text_markdown", this.getMarkdownHtml(data, "textMarkdown"));
        record.put("raw", gson.toJson(guidelineResponse));

        writeRecord(record);
        log.info("Saved dosing guideline record: {}", id);
    }

    private void resetOutputFile() {
        try {
            Files.deleteIfExists(dosingGuidelinesPath);
            Files.createFile(dosingGuidelinesPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to reset " + dosingGuidelinesPath, e);
        }
    }

    private void writeRecord(Map<String, Object> record) {
        try {
            Files.writeString(dosingGuidelinesPath, gson.toJson(record), StandardOpenOption.APPEND);
            Files.writeString(dosingGuidelinesPath, System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + dosingGuidelinesPath, e);
        }
    }

    private Map fetchMap(String url) {
        String content = this.getURLContent(url);
        if (content == null || content.isBlank()) {
            return Collections.emptyMap();
        }

        Map response = gson.fromJson(content, Map.class);
        return response == null ? Collections.emptyMap() : response;
    }

    private String firstRelatedChemicalId(Map data) {
        List<Map> relatedChemicals = this.mapList(data.get("relatedChemicals"));
        if (relatedChemicals.isEmpty()) {
            return null;
        }
        return this.stringValue(relatedChemicals.get(0).get("id"));
    }

    private String getMarkdownHtml(Map data, String key) {
        Map markdown = this.mapValue(data.get(key));
        return this.stringValue(markdown.get("html"));
    }

    private Map mapValue(Object value) {
        return value instanceof Map ? (Map) value : Collections.emptyMap();
    }

    private List<Map> mapList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }

        List<Map> result = new ArrayList<>();
        for (Object item : (List) value) {
            if (item instanceof Map) {
                result.add((Map) item);
            }
        }
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
