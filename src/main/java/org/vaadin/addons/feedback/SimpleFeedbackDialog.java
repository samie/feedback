package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    protected FeedbackHandler handler;
    protected I18n i18n;
    protected int errorDuration = 3000;

    public SimpleFeedbackDialog() {
        setModal(true);
        setWidth("460px");

        addAttachListener(event -> {
            this.setLocale(event.getUI().getLocale());
            createHeader();
            createForm();
            createButtons();
        });
    }

    public void setLocale(Locale locale) {
        this.i18n = new I18n(this.getClass().getPackageName()+".messages",locale);
    }

    protected void createHeader() {
        H2 title = new H2(i18n.get("title"));
        add(title);
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
        Button sendButton = new Button(i18n.get("button_send"));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button(i18n.get("button_cancel"));

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, sendButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        add(buttonLayout);

        sendButton.addClickListener(e -> handleSendFeedback());
        cancelButton.addClickListener(e -> close());

    }

    protected void handleSendFeedback() {
        String msg = message.getValue() != null ? message.getValue().trim() : "";
        if (msg.isEmpty()) {
            Notification.show(i18n.get("error_message_empty"), this.errorDuration, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (handler != null) {
            FeedbackData data = new FeedbackData();
            data.setMessage(msg);
            handler.handleFeedback(data);
        }
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

