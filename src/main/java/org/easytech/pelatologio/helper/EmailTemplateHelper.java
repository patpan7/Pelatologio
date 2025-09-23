package org.easytech.pelatologio.helper;

import java.util.Map;

public class EmailTemplateHelper {

    public record EmailContent(String subject, String body) {
    }

    public static EmailContent prepareEmail(String templateName, Object... dataObjects) {
        String subjectTemplate = AppSettings.loadSetting("email.template.subject." + templateName);
        String bodyTemplate = AppSettings.loadSetting("email.template.body." + templateName);

        if (subjectTemplate == null || bodyTemplate == null) {
            return new EmailContent("Error: Template not found", "Template '" + templateName + "' not found in app.properties.");
        }

        Map<String, String> placeholders = PlaceholderUtil.createPlaceholders(dataObjects);

        String finalSubject = replacePlaceholders(subjectTemplate, placeholders);
        String finalBody = replacePlaceholders(bodyTemplate, placeholders);

        return new EmailContent(finalSubject, finalBody);
    }

    private static String replacePlaceholders(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String htmlToPlainText(String html) {
        if (html == null) {
            return "";
        }
        // Replace <br> tags with newlines
        String textWithNewlines = html.replaceAll("<br\\s*/?>", "\n");
        // Remove all other HTML tags
        return textWithNewlines.replaceAll("<[^>]*>", "");
    }
}
