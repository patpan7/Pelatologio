package org.easytech.pelatologio.models.actions;

import java.util.List;

public class EmailActionConfig {
    public String recipientType; // e.g., "customer", "supplier", "custom"
    public String customRecipient;
    public String subject;
    public String bodyTemplate;
    public List<String> attachments;
    public String attachmentDialogTitle;

    // Getters and Setters
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }

    public String getCustomRecipient() { return customRecipient; }
    public void setCustomRecipient(String customRecipient) { this.customRecipient = customRecipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public String getAttachmentDialogTitle() { return attachmentDialogTitle; }
    public void setAttachmentDialogTitle(String attachmentDialogTitle) { this.attachmentDialogTitle = attachmentDialogTitle; }
}
