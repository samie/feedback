package org.vaadin.addons.feedback;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * A generic "system error" dialog shown when an unhandled exception occurs.
 * <p>
 * The user sees only a generic explanation and can optionally describe what
 * they were doing. The exception details are never shown in the dialog; they
 * are appended to the message passed to the {@link FeedbackHandler}, which is
 * only invoked when the user chooses to send the report.
 * <p>
 * Usage example:
 * <pre>
 * SystemErrorDialog.show(exception, data -&gt;
 *         log.warn("User-reported error: {}", data.getMessage()));
 * </pre>
 */
public class SystemErrorDialog extends SimpleFeedbackDialog {

    protected Paragraph description;
    private final Throwable error;

    public SystemErrorDialog(Throwable error) {
        this.error = error;
    }

    @Override
    protected void updateTexts() {
        setHeaderTitle(i18n.get("system_error_title"));

        if (message != null) {
            description.setText(i18n.get("system_error_description"));
            message.setLabel(i18n.get("system_error_message"));
            message.setPlaceholder(i18n.get("system_error_message_placeholder"));
            sendButton.setText(i18n.get("system_error_button_send"));
            cancelButton.setText(i18n.get("system_error_button_close"));
        }
    }

    @Override
    protected void createHeader() {
        setHeaderTitle(i18n.get("system_error_title"));
    }

    @Override
    protected void createForm() {
        description = new Paragraph(i18n.get("system_error_description"));
        // The message is optional here, so no minimum length.
        message = new TextArea(i18n.get("system_error_message"));
        message.setPlaceholder(i18n.get("system_error_message_placeholder"));
        message.setMaxLength(1000);
        message.setWidthFull();
        message.setHeight("6rem");
        add(description, message);
    }

    @Override
    protected void createButtons() {
        super.createButtons();
        sendButton.setText(i18n.get("system_error_button_send"));
        cancelButton.setText(i18n.get("system_error_button_close"));
    }

    @Override
    protected void handleSendFeedback() {
        String msg = message.getValue() != null ? message.getValue().trim() : "";
        FeedbackData data = new FeedbackData();
        data.setMessage(msg + "\n\nSystem Error Details:\n" + error);
        processFeedback(data);
        close();
    }

    /**
     * Static method to easily open a new system error dialog
     */
    public static SystemErrorDialog show(Throwable error, FeedbackHandler handler) {
        var d = new SystemErrorDialog(error);
        d.open(handler);
        return d;
    }
}
