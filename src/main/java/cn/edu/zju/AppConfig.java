package cn.edu.zju;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final AppConfig instance = new AppConfig();

    public static AppConfig getInstance() {
        return instance;
    }

    public AppConfig() {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.properties");
            Properties properties = new Properties();
            try {
                if (resourceAsStream == null) {
                    log.warn("app.properties not found on classpath.");
                } else {
                    properties.load(resourceAsStream);
                }
                this.jdbcUrl = properties.getProperty("jdbc.url");
                this.jdbcUsername = properties.getProperty("jdbc.username");
                this.jdbcPassword = properties.getProperty("jdbc.password");
                this.deepSeekApiKey = getSetting(properties, "deepseek.apiKey", "DEEPSEEK_API_KEY", "");
                this.deepSeekBaseUrl = getSetting(properties, "deepseek.baseUrl", "DEEPSEEK_BASE_URL", "https://api.deepseek.com");
                this.deepSeekModel = getSetting(properties, "deepseek.model", "DEEPSEEK_MODEL", "deepseek-chat");
                this.deepSeekTemperature = parseDoubleSetting(
                        getSetting(properties, "deepseek.temperature", "DEEPSEEK_TEMPERATURE", "0.2"), 0.2);
                this.deepSeekMaxTokens = parseIntegerSetting(
                        getSetting(properties, "deepseek.maxTokens", "DEEPSEEK_MAX_TOKENS", "800"), 800);
            } catch (IOException e) {
                log.info("", e);
            }
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    log.info("", e);
                }
            }
        }
    }

    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private String deepSeekApiKey;
    private String deepSeekBaseUrl;
    private String deepSeekModel;
    private double deepSeekTemperature;
    private int deepSeekMaxTokens;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public String getDeepSeekApiKey() {
        return deepSeekApiKey;
    }

    public void setDeepSeekApiKey(String deepSeekApiKey) {
        this.deepSeekApiKey = deepSeekApiKey;
    }

    public String getDeepSeekBaseUrl() {
        return deepSeekBaseUrl;
    }

    public void setDeepSeekBaseUrl(String deepSeekBaseUrl) {
        this.deepSeekBaseUrl = deepSeekBaseUrl;
    }

    public String getDeepSeekModel() {
        return deepSeekModel;
    }

    public void setDeepSeekModel(String deepSeekModel) {
        this.deepSeekModel = deepSeekModel;
    }

    public double getDeepSeekTemperature() {
        return deepSeekTemperature;
    }

    public void setDeepSeekTemperature(double deepSeekTemperature) {
        this.deepSeekTemperature = deepSeekTemperature;
    }

    public int getDeepSeekMaxTokens() {
        return deepSeekMaxTokens;
    }

    public void setDeepSeekMaxTokens(int deepSeekMaxTokens) {
        this.deepSeekMaxTokens = deepSeekMaxTokens;
    }

    public boolean isDeepSeekConfigured() {
        return deepSeekApiKey != null && !deepSeekApiKey.trim().isEmpty();
    }

    private String getSetting(Properties properties, String propertyName, String environmentName, String defaultValue) {
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue.trim();
        }
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }
        return defaultValue;
    }

    private double parseDoubleSetting(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid DeepSeek temperature '{}'. Use default {}.", value, defaultValue);
            return defaultValue;
        }
    }

    private int parseIntegerSetting(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid DeepSeek max tokens '{}'. Use default {}.", value, defaultValue);
            return defaultValue;
        }
    }
}
