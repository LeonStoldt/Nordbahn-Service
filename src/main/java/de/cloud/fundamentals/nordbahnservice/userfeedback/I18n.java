package de.cloud.fundamentals.nordbahnservice.userfeedback;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {

    private static final String PROPERTIES_FILE = "messages";

    private final ResourceBundle resourceBundle;

    public I18n() {
        this.resourceBundle = ResourceBundle.getBundle(PROPERTIES_FILE);
    }

    public String get(final String key) {
        String result;
        try {
            result = resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            result = "";
        }
        return result;
    }

    public String format(final String key, final Object... replacements) {
        return MessageFormat.format(get(key), replacements);
    }

}
