package com.rudekfshape.mobile.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class Config {

    private static final String DEFAULT_RESOURCE = "/configs/emulator.properties";
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = Config.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("Config file not found: " + DEFAULT_RESOURCE);
            }
            PROPS.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config: " + DEFAULT_RESOURCE, e);
        }
    }

    private Config() {
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();
        String val = PROPS.getProperty(key);
        return val == null ? null : val.trim();
    }

    public static String require(String key) {
        return Objects.requireNonNull(get(key), "Missing required config key: " + key);
    }

    public static boolean getBool(String key, boolean def) {
        String v = get(key);
        if (v == null || v.isBlank()) return def;
        return Boolean.parseBoolean(v);
    }

    public static int getInt(String key, int def) {
        String v = get(key);
        if (v == null || v.isBlank()) return def;
        return Integer.parseInt(v);
    }
}
