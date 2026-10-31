package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.Version;

import java.util.Locale;

public class DefaultFeedbackDialog extends AbstractFeedbackDialog implements LocaleChangeObserver {

    private Select<String> type;
    private TextArea message;
    private TextField name;
    private EmailField email;
    private Div systemInfo;
    private Button sendButton;
    private Button cancelButton;
    private final Binder<FeedbackData> binder = new Binder<>(FeedbackData.class);
    protected I18n i18n;
    protected int errorDuration = 3000;

    public DefaultFeedbackDialog() {
        // setModal(true) keeps the add-on compatible with both Vaadin 24 and 25;
        // ModalityMode/setModality only exists from 25.0 onwards.
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("500px");

        // Build the UI once. A Dialog is detached on close and re-attached on
        // re-open, so constructing content on attach would duplicate it.
        setLocale(Locale.getDefault());
        createHeader();
        createFeedbackForm();
        createSystemInfo();
        createButtons();
        configureBinder();

        // Only resolve the active UI locale on attach; this refreshes the
        // existing components rather than recreating them.
        addAttachListener(event -> setLocale(event.getUI().getLocale()));
    }

    public void setLocale(Locale locale) {
        this.i18n = new I18n(this.getClass().getPackageName() + ".messages", locale);
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setLocale(event.getLocale());
    }

    /**
     * Applies the current {@link #i18n} texts to the already-created
     * components. Called whenever the locale changes so that labels update
     * without re-adding components.
     */
    protected void updateTexts() {
        setHeaderTitle(i18n.get("title"));

        if (type != null) {
            String selected = type.getValue();
            type.setLabel(i18n.get("type"));
            type.setItems(i18n.get("type_general"), i18n.get("type_bug"), i18n.get("type_feature"));
            type.setPlaceholder(i18n.get("type_placeholder"));
            type.setValue(selected != null ? selected : i18n.get("type_general"));

            message.setLabel(i18n.get("message"));
            message.setPlaceholder(i18n.get("message_placeholder"));
            message.setHelperText(i18n.get("message_helper"));

            name.setLabel(i18n.get("name"));
            name.setPlaceholder(i18n.get("name_placeholder"));

            email.setLabel(i18n.get("email"));
            email.setPlaceholder(i18n.get("email_placeholder"));
            email.setHelperText(i18n.get("email_help"));

            systemInfo.setText(i18n.get("system_info", Version.getFullVersion()));

            sendButton.setText(i18n.get("button_send"));
            cancelButton.setText(i18n.get("button_cancel"));
        }
    }

    protected void createHeader() {
        setHeaderTitle(i18n.get("title"));
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
        message.setMinHeight("120px");

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
        systemInfo = new Div();
        systemInfo.setText(i18n.get("system_info", Version.getFullVersion()));
        add(systemInfo);
    }

    protected void createButtons() {
        sendButton = new Button(i18n.get("button_send"));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        // Scope the shortcut to this dialog so typing Enter in a field does not
        // submit unexpectedly outside the dialog's context.
        sendButton.addClickShortcut(Key.ENTER).listenOn(this);
        cancelButton = new Button(i18n.get("button_cancel"));

        sendButton.addClickListener(e -> handleSendFeedback());
        cancelButton.addClickListener(e -> close());

        getFooter().add(cancelButton, sendButton);
    }

    private void configureBinder() {
        binder.forField(type).bind(FeedbackData::getType, FeedbackData::setType);
        binder.forField(message)
                .asRequired(context -> i18n.get("error_message_empty"))
                .withValidator(value -> value != null && !value.trim().isEmpty(),
                        i18n.get("error_message_empty"))
                .bind(FeedbackData::getMessage, FeedbackData::setMessage);
        binder.forField(name).bind(FeedbackData::getName, FeedbackData::setName);
        binder.forField(email)
                .withValidator(new EmailValidator(i18n.get("error_validation"), true))
                .bind(FeedbackData::getEmail, FeedbackData::setEmail);
    }

    private void handleSendFeedback() {
        FeedbackData feedbackData = new FeedbackData();
        feedbackData.setSystemInfo(i18n.get("system_info", Version.getFullVersion()));

        if (!binder.writeBeanIfValid(feedbackData)) {
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
        DefaultFeedbackDialog d = new DefaultFeedbackDialog();
        d.open(handler);
        return d;
    }

}
