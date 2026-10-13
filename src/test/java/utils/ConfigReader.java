package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads settings from src/test/resources/config.properties.
 */
public class ConfigReader {

    private static final Properties properties = loadProperties();

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream file = ConfigReader.class.getResourceAsStream("/config.properties")) {
            if (file == null) {
                throw new RuntimeException("config.properties not found in src/test/resources");
            }
            props.load(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read config.properties", e);
        }
        return props;
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing value in config.properties: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    // Full URL from base.url + a path, e.g. getUrl("login.path")
    public static String getUrl(String pathKey) {
        return get("base.url") + get(pathKey);
    }

    // The password is kept out of the code, in an environment variable
    public static String getPassword() {
        String password = System.getenv("UNTANGLED_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Set the UNTANGLED_PASSWORD environment variable");
        }
        return password;
    }
}
