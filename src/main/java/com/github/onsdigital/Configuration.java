package com.github.onsdigital;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class to get configuration values from {@link System#getProperty(String)} or gracefully fall back to {@link System#getenv()}.
 */
@Slf4j
public abstract class Configuration {

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank.
     *
     * @param key The configuration value key.
     * @return A system property or, if that comes back blank, an environment value.
     */
    public static String get(String key) {
        String value = StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
        log.trace("CONFIGURATION|key: {} value: {}", key, ConfigurationManager.createSafeValue(key, value));
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        int i = defaultValue;
        String value = System.getProperty(key);
        if (value != null) {
            i = Integer.parseInt(value);
        }
        log.info("CONFIGURATION|key: {} value: {}", key, i);
        return i;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        boolean b = defaultValue;
        String value = get(key);
        log.info("***** CONFIGURATION|System.getProperty(key) key: {} value: {}", key, value);

        if (value != null) {
            b = Boolean.parseBoolean(value);
        }
        log.info("CONFIGURATION|key: {} value: {}", key, b);
        return b;
    }

    /**
     * Gets a configuration value from {@link System#getProperty(String)}, falling back to {@link System#getenv()}
     * if the property comes back blank, then falling back to the default value.
     *
     * @param key          The configuration value key.
     * @param defaultValue The default to use if neither a property nor an environment value are present.
     * @return The result of {@link #get(String)}, or <code>defaultValue</code> if that result is blank.
     */
    public static String get(String key, String defaultValue) {
        String value = StringUtils.defaultIfBlank(get(key), defaultValue);
        log.info("CONFIGURATION|key: {} value: {}", key, ConfigurationManager.createSafeValue(key, value));
        return value;
    }

    public static String set(String key, String value) {
        return System.setProperty(key, value);
    }

    public static String set(String key, int value) {
        return System.setProperty(key, "" + value);
    }
}
