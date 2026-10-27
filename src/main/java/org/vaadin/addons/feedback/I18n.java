package org.vaadin.addons.feedback;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private final ResourceBundle bundle;

    public I18n(String baseName, Locale locale) {
        this.bundle = ResourceBundle.getBundle(baseName, locale);
    }

    public String get(String key, Object... args) {
        return MessageFormat.format(this.bundle.getString(key), args);
    }
}
