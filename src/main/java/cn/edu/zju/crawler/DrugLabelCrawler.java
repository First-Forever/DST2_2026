package cn.edu.zju.crawler;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dbutils.DBUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DrugLabelCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelCrawler.class);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static final String URL_DRUG_LABEL = "https://api.pharmgkb.org/v1/data/label?source=fda";
    public static final String URL_DRUG_LABEL_DETAIL = "https://api.pharmgkb.org/v1/data/label/%s?view=base";
    private Path drugsPath = new File("drugs.data").toPath();
    private Path drugLabelsPath = new File("drugLabels.data").toPath();

    public void doCrawlerDrug() {
        String content = this.getURLContent(URL_DRUG_LABEL);

        try {
            Files.writeString(drugsPath, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doCrawlerDrugLabel() {
        try {
            if (Files.exists(drugLabelsPath)) {
                try {
                    Files.delete(drugLabelsPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Files.createFile(drugLabelsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String drugContent = Files.readString(drugsPath);
            Gson gson = new Gson();
            Map drugs = gson.fromJson(drugContent, Map.class);
            List<Map> data = (List<Map>) drugs.get("data");
            data.stream().forEach(x -> {
                log.info("{}", x);
                String id = (String) (x.get("id"));

                String content = this.getURLContent(String.format(URL_DRUG_LABEL_DETAIL, id));
                Map result = gson.fromJson(content, Map.class);
                Map drugLabel = (Map) result.get("data");
                Map<String, Object> enrichedDrugLabel = this.enrichDrugLabel(drugLabel);
                log.info("Fetch label of drug {}", id);
                try {
                    Files.writeString(drugLabelsPath, gson.toJson(enrichedDrugLabel), StandardOpenOption.APPEND);
                    Files.writeString(drugLabelsPath, "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> enrichDrugLabel(Map drugLabel) {
        Map<String, Object> enrichedDrugLabel = new LinkedHashMap<>(drugLabel);
        String summaryText = getMarkdownText(drugLabel, "summaryMarkdown");
        String prescribingText = getMarkdownText(drugLabel, "prescribingMarkdown");
        String labelText = getMarkdownText(drugLabel, "textMarkdown");

        enrichedDrugLabel.put("efficacy_summary", firstNonBlank(summaryText, labelText));
        enrichedDrugLabel.put("response_warning", buildResponseWarning(prescribingText, labelText, summaryText));
        enrichedDrugLabel.put("alternative_drug", buildAlternativeDrug(drugLabel, summaryText, prescribingText, labelText));
        return enrichedDrugLabel;
    }

    private String buildResponseWarning(String prescribingText, String labelText, String summaryText) {
        String warningText = firstMatchingSentence(
                prescribingText,
                "warning",
                "boxed warning",
                "poor metabolizers",
                "poor metabolisers",
                "reduced effect",
                "diminished",
                "risk",
                "avoid",
                "consider"
        );
        if (warningText != null) {
            return warningText;
        }

        warningText = firstMatchingSentence(
                labelText,
                "warning",
                "boxed warning",
                "poor metabolizers",
                "poor metabolisers",
                "reduced effect",
                "diminished",
                "risk",
                "avoid"
        );
        if (warningText != null) {
            return warningText;
        }

        return firstMatchingSentence(
                summaryText,
                "warning",
                "poor metabolizers",
                "poor metabolisers",
                "reduced effect",
                "diminished",
                "risk",
                "avoid"
        );
    }

    private String buildAlternativeDrug(Map drugLabel, String summaryText, String prescribingText, String labelText) {
        Object alternateDrugAvailable = drugLabel.get("alternateDrugAvailable");
        boolean hasAlternativeDrug = alternateDrugAvailable instanceof Boolean && (Boolean) alternateDrugAvailable;
        if (!hasAlternativeDrug) {
            return null;
        }

        String alternativeDrugText = firstMatchingSentence(
                prescribingText,
                "another",
                "alternative",
                "consider",
                "avoid"
        );
        if (alternativeDrugText != null) {
            return alternativeDrugText;
        }

        alternativeDrugText = firstMatchingSentence(
                summaryText,
                "another",
                "alternative",
                "consider",
                "avoid"
        );
        if (alternativeDrugText != null) {
            return alternativeDrugText;
        }

        return firstMatchingSentence(
                labelText,
                "another",
                "alternative",
                "consider",
                "avoid"
        );
    }

    private String getMarkdownText(Map drugLabel, String key) {
        Object markdown = drugLabel.get(key);
        if (!(markdown instanceof Map)) {
            return null;
        }

        Object html = ((Map) markdown).get("html");
        if (!(html instanceof String)) {
            return null;
        }
        return normalizeText((String) html);
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

    private String firstMatchingSentence(String text, String... keywords) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");
        for (String sentence : sentences) {
            String lowerCaseSentence = sentence.toLowerCase();
            for (String keyword : keywords) {
                if (lowerCaseSentence.contains(keyword.toLowerCase())) {
                    return sentence.trim();
                }
            }
        }
        return null;
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

}
