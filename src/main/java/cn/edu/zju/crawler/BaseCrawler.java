package cn.edu.zju.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(BaseCrawler.class);

    public String getURLContent(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = ((HttpURLConnection) url.openConnection());
            urlConnection.setConnectTimeout(60000);
            urlConnection.setReadTimeout(60000);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                log.debug("Skip 404 response: {}", urlString);
                return null;
            }

            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                log.warn("Request failed with status {}: {}", responseCode, urlString);
                return null;
            }

            try (InputStream inputStream = urlConnection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count = inputStream.read(buffer);
                while (count >= 0) {
                    byteArrayOutputStream.write(buffer, 0, count);
                    count = inputStream.read(buffer);
                }
                return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to fetch {}", urlString, e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

}
