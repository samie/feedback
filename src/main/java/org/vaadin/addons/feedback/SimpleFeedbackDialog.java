package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Locale;

/**
 * A very small & minimal feedback dialog variant that only asks for a message.
 * <p>
 * Usage example:
 * <pre>
 * add(new Button("Send feedback", e -> SimpleFeedbackDialog.open(data ->
 *         System.out.println("Feedback: " + data.getMessage())
 * )));
 * </pre>
 */
public class SimpleFeedbackDialog extends AbstractFeedbackDialog {

    protected TextArea message;
    protected Button sendButton;
    protected Button cancelButton;
    protected I18n i18n;
    protected int errorDuration = 3000;

    public SimpleFeedbackDialog() {
        // setModal(true) keeps the add-on compatible with both Vaadin 24 and 25;
        // ModalityMode/setModality only exists from 25.0 onwards.
        setModal(true);
        setWidth("460px");

        // Build the UI once. A Dialog is detached on close and re-attached on
        // re-open, so constructing content on attach would duplicate it.
        setLocale(Locale.getDefault());
        createHeader();
        createForm();
        createButtons();

        // Only resolve the active UI locale on attach; this refreshes the
        // existing components rather than recreating them.
        addAttachListener(event -> setLocale(event.getUI().getLocale()));
    }

    public void setLocale(Locale locale) {
        this.i18n = new I18n(this.getClass().getPackageName() + ".messages", locale);
        updateTexts();
    }

    /**
     * Applies the current {@link #i18n} texts to the already-created
     * components. Called whenever the locale changes so that labels update
     * without re-adding components.
     */
    protected void updateTexts() {
        setHeaderTitle(i18n.get("title"));

        if (message != null) {
            message.setLabel(i18n.get("message"));
            message.setPlaceholder(i18n.get("message_placeholder"));
            sendButton.setText(i18n.get("button_send"));
            cancelButton.setText(i18n.get("button_cancel"));
        }
    }

    protected void createHeader() {
        setHeaderTitle(i18n.get("title"));
    }

    protected void createForm() {
        message = new TextArea(i18n.get("message"));
        message.setPlaceholder(i18n.get("message_placeholder"));
        message.setMinLength(1);
        message.setMaxLength(1000);
        message.setWidthFull();
        message.setHeight("10rem");
        add(message);
    }

    protected void createButtons() {
        sendButton = new Button(i18n.get("button_send"));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Scope the shortcut to this dialog so typing Enter in the message area
        // does not submit from an unexpected context.
        sendButton.addClickShortcut(Key.ENTER).listenOn(this);
        cancelButton = new Button(i18n.get("button_cancel"));

        sendButton.addClickListener(e -> handleSendFeedback());
        cancelButton.addClickListener(e -> close());

        getFooter().add(cancelButton, sendButton);
    }

    protected void handleSendFeedback() {
        String msg = message.getValue() != null ? message.getValue().trim() : "";
        if (msg.isEmpty()) {
            Notification.show(i18n.get("error_message_empty"), this.errorDuration, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        FeedbackData data = new FeedbackData();
        data.setMessage(msg);
        processFeedback(data);
        close();
    }

    /**
     * Static method to easily open a new feedback dialog
     */
    public static SimpleFeedbackDialog show(FeedbackHandler handler) {
        var d = new SimpleFeedbackDialog();
        d.open(handler);
        return d;
    }

}
