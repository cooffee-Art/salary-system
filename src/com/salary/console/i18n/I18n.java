package com.salary.console.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 全局双语入口。
 *
 * 语言包位于 resources/messages_zh.properties 和 messages_en.properties。
 * 所有面向用户的文本都必须通过 I18n.get(...) 获取，禁止在 Java 代码里硬编码中英文。
 */
public final class I18n {

    private static final String BUNDLE_NAME = "messages";

    private static Locale locale = Locale.SIMPLIFIED_CHINESE;
    private static ResourceBundle bundle = load(locale);

    private I18n() {
    }

    public static void setLocale(Locale newLocale) {
        locale = newLocale;
        bundle = load(newLocale);
    }

    public static Locale getLocale() {
        return locale;
    }

    public static boolean isEnglish() {
        return "en".equalsIgnoreCase(locale.getLanguage());
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException exception) {
            return "!" + key + "!";
        }
    }

    public static String get(String key, Object... args) {
        if (args == null || args.length == 0) {
            return get(key);
        }
        return MessageFormat.format(get(key), args);
    }

    private static ResourceBundle load(Locale target) {
        try {
            return ResourceBundle.getBundle(BUNDLE_NAME, target);
        } catch (MissingResourceException exception) {
            return ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        }
    }
}
