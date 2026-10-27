package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.Locale;

public class DefaultFeedbackDialog extends AbstractFeedbackDialog  implements LocaleChangeObserver {

    private Select<String> type;
    private TextArea message;
    private TextField name;
    private EmailField email;
    protected I18n i18n;
    protected int errorDuration = 3000;

    public DefaultFeedbackDialog() {
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("500px");

        addAttachListener(event -> {
            this.setLocale(event.getUI().getLocale());
            createHeader();
            createFeedbackForm();
            createSystemInfo();
            createButtons();
        });
    }

    public void setLocale(Locale locale) {
        this.i18n = new I18n(this.getClass().getPackageName()+".messages",locale);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setLocale(event.getLocale());
    }

    protected void createHeader() {
        H2 title = new H2(i18n.get("title"));
        add(title);
    }

    protected void createFeedbackForm() {
        FormLayout formLayout = new FormLayout();

        type = new Select<>();
        type.setLabel(i18n.get("type"));
        type.setItems(i18n.get("type_general"), i18n.get("type_bug"), i18n.get("type_feature"));
        type.setValue(i18n.get("type_general"));
        type.setPlaceholder(i18n.get("type_placeholder"));

        message = new TextArea(i18n.get("message"));
        message.setPlaceholder(i18n.get("message_placeholder"));
        message.setRequired(true);
        message.setMinLength(1);
        message.setMaxLength(1000);
        message.setHelperText(i18n.get("message_helper"));
        message.getStyle().set("min-height", "120px");

        name = new TextField(i18n.get("name"));
        name.setPlaceholder(i18n.get("name_placeholder"));
        email = new EmailField(i18n.get("email"));
        email.setPlaceholder(i18n.get("email_placeholder"));
        email.setHelperText(i18n.get("email_help"));

        formLayout.add(type, message, name, email);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        add(formLayout);
    }

    private void createSystemInfo() {
        Div systemInfo = new Div();
        systemInfo.setText(i18n.get("system_info", com.vaadin.flow.server.Version.getFullVersion()));
        add(systemInfo);
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

    private void handleSendFeedback() {
        FeedbackData feedbackData = new FeedbackData();

        // Manually populate the feedback data since binder isn't properly configured
        feedbackData.setType(type.getValue());
        feedbackData.setMessage(message.getValue());
        feedbackData.setName(name.getValue());
        feedbackData.setEmail(email.getValue());

        // Basic validation
        if (message.getValue() == null || message.getValue().trim().isEmpty()) {
            Notification.show(i18n.get("error_message_empty"),
                            this.errorDuration, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            processFeedback(feedbackData);
            close();
        } catch (Exception ex) {
            Notification.show(i18n.get("error_validation"),
                            this.errorDuration, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Static method to easily open a new feedback dialog
     */
    public static DefaultFeedbackDialog show(FeedbackHandler handler) {
        DefaultFeedbackDialog d =new DefaultFeedbackDialog();
        d.open(handler);
        return d;
    }

}
