package cn.edu.zju.crawler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrugProfessionalInfoCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DrugProfessionalInfoCrawler.class);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PMID_PATTERN = Pattern.compile("(\\d{6,})");

    private static final List<String> LABEL_SOURCES = Arrays.asList("fda", "ema", "pmda", "hcsc", "swissmedic");
    private static final List<String> GUIDELINE_SOURCES = Arrays.asList("cpic", "cpnds", "dpwg", "fda", "pro");

    public static final String URL_LABELS = "https://api.pharmgkb.org/v1/data/label?source=%s";
    public static final String URL_LABEL_DETAIL = "https://api.pharmgkb.org/v1/data/label/%s?view=base";
    public static final String URL_GUIDELINES_BY_DRUGS = "https://api.pharmgkb.org/v1/site/guidelinesByDrugs";
    public static final String URL_DATA_PATH = "https://api.pharmgkb.org/v1/data%s";
    public static final String URL_CLINICAL_ANNOTATIONS = "https://api.pharmgkb.org/v1/data/clinicalAnnotation?relatedChemicals.name=%s&view=min";

    private final Gson gson = new Gson();
    private final Path professionalInfoPath = new File("drugProfessionalInfo.data").toPath();

    public void doCrawlerDrugProfessionalInfo() {
        doCrawlerDrugProfessionalInfo(true);
    }

    public void doCrawlerDrugProfessionalInfo(boolean includeClinicalAnnotations) {
        resetOutputFile();

        Set<String> drugNames = new LinkedHashSet<>();
        drugNames.addAll(crawlLabels());
        drugNames.addAll(crawlGuidelines());

        if (includeClinicalAnnotations) {
            crawlClinicalAnnotations(drugNames);
        }
    }

    private Set<String> crawlLabels() {
        Set<String> drugNames = new LinkedHashSet<>();
        Set<String> seenLabelIds = new LinkedHashSet<>();

        for (String labelSource : LABEL_SOURCES) {
            Map response = fetchMap(String.format(URL_LABELS, labelSource));

            for (Map item : mapList(response.get("data"))) {
                String labelId = stringValue(item.get("id"));
                if (isBlank(labelId) || !seenLabelIds.add(labelId)) {
                    continue;
                }

                Map detailResponse = fetchMap(String.format(URL_LABEL_DETAIL, labelId));
                Map label = mapValue(detailResponse.get("data"));
                if (label.isEmpty()) {
                    continue;
                }

                Set<String> recordDrugNames = extractDrugNames(label);
                drugNames.addAll(recordDrugNames);
                for (String drugName : recordDrugNames) {
                    writeRecord(buildLabelRecord(label, drugName));
                }
            }
        }

        return drugNames;
    }

    private Set<String> crawlGuidelines() {
        Set<String> drugNames = new LinkedHashSet<>();
        Set<String> seenGuidelineUrls = new LinkedHashSet<>();

        Map response = fetchMap(URL_GUIDELINES_BY_DRUGS);

        for (Map drugGroup : mapList(response.get("data"))) {
            for (String source : GUIDELINE_SOURCES) {
                for (Map guidelineSummary : mapList(drugGroup.get(source))) {
                    String apiPath = stringValue(guidelineSummary.get("url"));
                    if (isBlank(apiPath) || !seenGuidelineUrls.add(apiPath)) {
                        continue;
                    }

                    Map detailResponse = fetchMap(toDataApiUrl(apiPath));
                    Map guideline = mapValue(detailResponse.get("data"));
                    if (guideline.isEmpty()) {
                        continue;
                    }

                    Set<String> recordDrugNames = extractDrugNames(guideline);
                    if (recordDrugNames.isEmpty()) {
                        String fallbackDrugName = stringValue(drugGroup.get("name"));
                        if (!isBlank(fallbackDrugName)) {
                            recordDrugNames.add(fallbackDrugName);
                        }
                    }

                    drugNames.addAll(recordDrugNames);
                    for (String drugName : recordDrugNames) {
                        writeRecord(buildGuidelineRecord(guideline, drugName));
                    }
                }
            }
        }

        return drugNames;
    }

    private void crawlClinicalAnnotations(Set<String> drugNames) {
        Set<String> seenAnnotations = new LinkedHashSet<>();

        for (String drugName : drugNames) {
            Map response = fetchMap(String.format(URL_CLINICAL_ANNOTATIONS, encode(drugName)));
            if (response.isEmpty()) {
                log.info("No clinical annotation response for {}", drugName);
                continue;
            }

            for (Map annotation : mapList(response.get("data"))) {
                String annotationId = firstNonBlank(
                        stringValue(annotation.get("accessionId")),
                        stringValue(annotation.get("id"))
                );
                String key = drugName + "|" + annotationId;
                if (isBlank(annotationId) || !seenAnnotations.add(key)) {
                    continue;
                }

                writeRecord(buildClinicalAnnotationRecord(annotation, drugName));
            }
        }
    }

    private Map<String, Object> buildLabelRecord(Map label, String drugName) {
        Set<String> tags = new LinkedHashSet<>();
        addFieldTag(tags, "source", label.get("source"));
        addFieldTag(tags, "biomarker_status", label.get("biomarkerStatus"));
        addFieldTag(tags, "testing", nestedTerm(label, "testing"));
        addBooleanTag(tags, "alternate_drug_available", label.get("alternateDrugAvailable"));
        addBooleanTag(tags, "dosing_information", label.get("dosingInformation"));
        addBooleanTag(tags, "other_prescribing_guidance", label.get("otherPrescribingGuidance"));
        addBooleanTag(tags, "pediatric", label.get("pediatric"));
        addBooleanTag(tags, "pgx_related", label.get("pgxRelated"));
        addBooleanTag(tags, "label_document_available", label.get("labelDocumentAvailable"));
        addLiteratureTerms(tags, label);

        String source = stringValue(label.get("source"));
        return buildRecord(
                drugName,
                extractRelatedGenes(label),
                "LABEL_" + normalizeSourceType(source),
                firstNonBlank(nestedTerm(label, "testing"), stringValue(label.get("biomarkerStatus"))),
                tags,
                firstNonBlank(markdownText(label, "summaryMarkdown"), markdownText(label, "textMarkdown")),
                extractPmids(label),
                firstNonBlank(extractSourceUrl(label), pharmgkbUrl("labelAnnotation", stringValue(label.get("id"))))
        );
    }

    private Map<String, Object> buildGuidelineRecord(Map guideline, String drugName) {
        Set<String> tags = new LinkedHashSet<>();
        addFieldTag(tags, "source", guideline.get("source"));
        addFieldTag(tags, "obj_cls", guideline.get("objCls"));
        addBooleanTag(tags, "recommendation", guideline.get("recommendation"));
        addBooleanTag(tags, "dosing_information", guideline.get("dosingInformation"));
        addBooleanTag(tags, "other_prescribing_guidance", guideline.get("otherPrescribingGuidance"));
        addBooleanTag(tags, "pediatric", guideline.get("pediatric"));
        addBooleanTag(tags, "has_testing_info", guideline.get("hasTestingInfo"));
        addLiteratureTerms(tags, guideline);

        String recommendation = Boolean.TRUE.equals(guideline.get("recommendation")) ? "recommendation=true" : "recommendation=false";
        return buildRecord(
                drugName,
                extractRelatedGenes(guideline),
                "GUIDELINE_" + normalizeSourceType(stringValue(guideline.get("source"))),
                recommendation,
                tags,
                firstNonBlank(markdownText(guideline, "summaryMarkdown"), markdownText(guideline, "textMarkdown")),
                extractPmids(guideline),
                firstNonBlank(extractSourceUrl(guideline), pharmgkbUrl("guidelineAnnotation", stringValue(guideline.get("id"))))
        );
    }

    private Map<String, Object> buildClinicalAnnotationRecord(Map annotation, String drugName) {
        Set<String> tags = new LinkedHashSet<>();
        addObjectListTags(tags, "type", annotation.get("types"));
        addObjectListTags(tags, "guideline", annotation.get("relatedGuidelines"));
        addObjectListTags(tags, "label", annotation.get("relatedLabels"));

        String annotationId = firstNonBlank(
                stringValue(annotation.get("accessionId")),
                stringValue(annotation.get("id"))
        );
        return buildRecord(
                drugName,
                extractRelatedGenes(annotation),
                "CLINICAL_ANNOTATION",
                nestedTerm(annotation, "levelOfEvidence"),
                tags,
                stringValue(annotation.get("name")),
                extractPmids(annotation),
                pharmgkbUrl("clinicalAnnotation", annotationId)
        );
    }

    private Map<String, Object> buildRecord(String drugName,
                                            Set<String> genes,
                                            String sourceType,
                                            String evidenceLevel,
                                            Set<String> tags,
                                            String literatureSummary,
                                            Set<String> pmids,
                                            String sourceUrl) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("drug_name", drugName);
        record.put("related_genes", joinValues(genes));
        record.put("source_type", sourceType);
        record.put("evidence_level", evidenceLevel);
        record.put("guideline_or_label_tags", joinValues(tags));
        record.put("literature_summary", literatureSummary);
        record.put("pmid_list", joinValues(pmids));
        record.put("source_url", sourceUrl);
        return record;
    }

    private Set<String> extractDrugNames(Map source) {
        Set<String> drugNames = new LinkedHashSet<>();
        for (Map chemical : mapList(source.get("relatedChemicals"))) {
            addValue(drugNames, firstNonBlank(stringValue(chemical.get("name")), stringValue(chemical.get("term"))));
        }
        return drugNames;
    }

    private Set<String> extractRelatedGenes(Map source) {
        Set<String> genes = new LinkedHashSet<>();
        addGeneValues(genes, source.get("relatedGenes"));

        Map location = mapValue(source.get("location"));
        addGeneValues(genes, location.get("genes"));
        return genes;
    }

    private void addGeneValues(Set<String> genes, Object value) {
        for (Map gene : mapList(value)) {
            addValue(genes, firstNonBlank(
                    stringValue(gene.get("symbol")),
                    stringValue(gene.get("name")),
                    stringValue(gene.get("term")),
                    stringValue(gene.get("id"))
            ));
        }
    }

    private Set<String> extractPmids(Map source) {
        Set<String> pmids = new LinkedHashSet<>();
        for (Map literature : mapList(source.get("literature"))) {
            for (Map crossReference : mapList(literature.get("crossReferences"))) {
                String resource = stringValue(crossReference.get("resource"));
                String url = stringValue(crossReference.get("_url"));
                if (containsIgnoreCase(resource, "PubMed") || containsIgnoreCase(url, "pubmed")) {
                    addPmid(pmids, stringValue(crossReference.get("id")));
                    addPmid(pmids, stringValue(crossReference.get("accessionId")));
                    addPmid(pmids, stringValue(crossReference.get("term")));
                    addPmid(pmids, url);
                }
            }
        }
        return pmids;
    }

    private String extractSourceUrl(Map source) {
        String directUrl = stringValue(source.get("_url"));
        if (!isBlank(directUrl)) {
            return directUrl;
        }

        for (Map literature : mapList(source.get("literature"))) {
            String sameAs = stringValue(literature.get("_sameAs"));
            if (!isBlank(sameAs)) {
                return sameAs;
            }

            for (Map crossReference : mapList(literature.get("crossReferences"))) {
                String url = stringValue(crossReference.get("_url"));
                if (!isBlank(url)) {
                    return url;
                }
            }
        }
        return null;
    }

    private void addLiteratureTerms(Set<String> tags, Map source) {
        for (Map literature : mapList(source.get("literature"))) {
            addObjectListTags(tags, "literature_term", literature.get("terms"));
        }
    }

    private void addObjectListTags(Set<String> tags, String prefix, Object value) {
        if (!(value instanceof List)) {
            return;
        }

        for (Object item : (List) value) {
            if (item instanceof Map) {
                Map map = (Map) item;
                addFieldTag(tags, prefix, firstNonBlank(
                        stringValue(map.get("term")),
                        stringValue(map.get("name")),
                        stringValue(map.get("id"))
                ));
            } else {
                addFieldTag(tags, prefix, item);
            }
        }
    }

    private void addFieldTag(Set<String> tags, String prefix, Object value) {
        String stringValue = stringValue(value);
        if (!isBlank(stringValue)) {
            tags.add(prefix + ":" + stringValue);
        }
    }

    private void addBooleanTag(Set<String> tags, String tag, Object value) {
        if (Boolean.TRUE.equals(value)) {
            tags.add(tag);
        }
    }

    private void addPmid(Set<String> pmids, String value) {
        if (isBlank(value)) {
            return;
        }

        Matcher matcher = PMID_PATTERN.matcher(value);
        while (matcher.find()) {
            pmids.add(matcher.group(1));
        }
    }

    private String markdownText(Map source, String key) {
        Map markdown = mapValue(source.get(key));
        String html = stringValue(markdown.get("html"));
        if (isBlank(html)) {
            return null;
        }
        return normalizeText(html);
    }

    private String normalizeText(String html) {
        String text = html
                .replace("<br />", " ")
                .replace("<br/>", " ")
                .replace("<br>", " ")
                .replace("</p>", " ")
                .replace("</li>", " ")
                .replace("</blockquote>", " ")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
        text = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ").replace('\u00A0', ' ').trim();
        return text.isEmpty() ? null : text;
    }

    private void resetOutputFile() {
        try {
            Files.deleteIfExists(professionalInfoPath);
            Files.createFile(professionalInfoPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to reset " + professionalInfoPath, e);
        }
    }

    private void writeRecord(Map<String, Object> record) {
        try {
            Files.writeString(professionalInfoPath, gson.toJson(record), StandardOpenOption.APPEND);
            Files.writeString(professionalInfoPath, System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write " + professionalInfoPath, e);
        }
    }

    private String fetch(String url) {
        log.info("Fetch {}", url);
        return this.getURLContent(url);
    }

    private Map fetchMap(String url) {
        String content = fetch(url);
        if (isBlank(content)) {
            return Collections.emptyMap();
        }

        Map response = gson.fromJson(content, Map.class);
        return response == null ? Collections.emptyMap() : response;
    }

    private String toDataApiUrl(String apiPath) {
        if (apiPath.startsWith("http://") || apiPath.startsWith("https://")) {
            return apiPath;
        }
        if (apiPath.startsWith("/v1/data")) {
            return "https://api.pharmgkb.org" + apiPath;
        }
        return String.format(URL_DATA_PATH, apiPath);
    }

    private String pharmgkbUrl(String type, String id) {
        if (isBlank(id)) {
            return null;
        }
        return "https://www.pharmgkb.org/" + type + "/" + id;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is not supported", e);
        }
    }

    private String normalizeSourceType(String source) {
        if (isBlank(source)) {
            return "UNKNOWN";
        }
        return source.trim().replaceAll("[^A-Za-z0-9]+", "_").toUpperCase();
    }

    private String nestedTerm(Map source, String key) {
        Object value = source.get(key);
        if (value instanceof Map) {
            return stringValue(((Map) value).get("term"));
        }
        return stringValue(value);
    }

    private String joinValues(Set<String> values) {
        List<String> cleanValues = new ArrayList<>();
        for (String value : values) {
            if (!isBlank(value)) {
                cleanValues.add(value.trim());
            }
        }
        return cleanValues.isEmpty() ? null : String.join("; ", cleanValues);
    }

    private void addValue(Set<String> values, String value) {
        if (!isBlank(value)) {
            values.add(value.trim());
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (!isBlank(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Map mapValue(Object value) {
        return value instanceof Map ? (Map) value : Collections.emptyMap();
    }

    private List<Map> mapList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }

        List<Map> maps = new ArrayList<>();
        for (Object item : (List) value) {
            if (item instanceof Map) {
                maps.add((Map) item);
            }
        }
        return maps;
    }
}
