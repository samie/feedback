package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
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
import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.Version;
import com.vaadin.flow.server.WebBrowser;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultFeedbackDialog extends AbstractFeedbackDialog implements LocaleChangeObserver {

    private Select<String> type;
    private TextArea message;
    private TextField name;
    private EmailField email;
    private Details systemInfo;
    private Hr systemInfoDivider;
    private Button sendButton;
    private Button cancelButton;
    private final Binder<FeedbackData> binder = new Binder<>(FeedbackData.class);
    protected I18n i18n;
    protected int errorDuration = 3000;
    private SystemInfoGenerator systemInfoGenerator;
    private boolean systemInfoVisible = true;

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

            systemInfo.setSummaryText(i18n.get("system_info_title"));
            systemInfo.removeAll();
            Map<String, String> info = collectSystemInfo();
            info.forEach((label, value) -> systemInfo.add(new Div(label + ": " + value)));
            // Hide the section (and its divider) entirely when disabled or empty
            // (e.g. before attach, or when a generator returns nothing).
            systemInfo.setVisible(!info.isEmpty());
            systemInfoDivider.setVisible(!info.isEmpty());

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
        // Collapsible so the details don't crowd the dialog; content is filled
        // in updateTexts() once a locale (and, on attach, a UI) is available.
        systemInfo = new Details();
        systemInfo.setOpened(false);
        // A divider visually separates the system info from the form. Its
        // visibility is kept in sync with the section in updateTexts().
        systemInfoDivider = new Hr();
        add(systemInfoDivider, systemInfo);
    }

    /**
     * Controls whether the system information section is shown in the dialog and
     * included in the submitted {@link FeedbackData}. Enabled by default.
     * <p>
     * Equivalent to supplying a {@link SystemInfoGenerator} that returns an
     * empty map.
     *
     * @param visible {@code false} to omit system information entirely
     */
    public void setSystemInfoVisible(boolean visible) {
        this.systemInfoVisible = visible;
        if (systemInfo != null) {
            updateTexts();
        }
    }

    public boolean isSystemInfoVisible() {
        return systemInfoVisible;
    }

    /**
     * Sets a custom generator for the system information, replacing the built-in
     * collection. Pass {@code null} to restore the default.
     * <p>
     * Start from {@link #defaultSystemInfo(UI)} to augment the built-in details,
     * return a fresh map to replace them, or an empty map to show/send nothing.
     * Whatever is returned is shown to the end user <b>and</b> transmitted, so
     * never include secrets.
     */
    public void setSystemInfoGenerator(SystemInfoGenerator generator) {
        this.systemInfoGenerator = generator;
        if (systemInfo != null) {
            updateTexts();
        }
    }

    /**
     * Resolves the system information currently in effect: an empty map when
     * disabled or before the dialog is attached, the custom generator's result
     * when one is set, otherwise {@link #defaultSystemInfo(UI)}.
     */
    protected Map<String, String> collectSystemInfo() {
        UI ui = UI.getCurrent();
        // UI is null while the dialog is constructed before attach; the real
        // values are filled in when updateTexts() runs again on attach.
        if (!systemInfoVisible || ui == null) {
            return new LinkedHashMap<>();
        }
        if (systemInfoGenerator != null) {
            Map<String, String> custom = systemInfoGenerator.apply(ui);
            return custom != null ? new LinkedHashMap<>(custom) : new LinkedHashMap<>();
        }
        return defaultSystemInfo(ui);
    }

    /**
     * Collects the built-in, non-sensitive information about the running
     * application and the user's client, as an ordered map of localized label
     * to value. Use this as the starting point when augmenting system info from
     * a {@link SystemInfoGenerator}.
     * <p>
     * Only client/app values are gathered: the same data is both shown to the
     * end user and transmitted with the feedback, so server internals (Java
     * version, server OS, memory, file paths, host names, IP address) are
     * deliberately excluded to avoid information disclosure.
     *
     * @param ui the active UI; may be {@code null}, in which case only the
     *           Vaadin version is returned
     */
    public static Map<String, String> defaultSystemInfo(UI ui) {
        Locale locale = ui != null ? ui.getLocale() : Locale.getDefault();
        I18n i18n = new I18n(DefaultFeedbackDialog.class.getPackageName() + ".messages", locale);

        Map<String, String> info = new LinkedHashMap<>();
        info.put(i18n.get("system_info_vaadin"), Version.getFullVersion());

        if (ui != null) {
            Location location = ui.getActiveViewLocation();
            if (location != null) {
                // The root route reports an empty path; show "/" instead.
                String path = location.getPathWithQueryParameters();
                info.put(i18n.get("system_info_view"), path.isEmpty() ? "/" : path);
            }
            info.put(i18n.get("system_info_language"), ui.getLocale().toLanguageTag());

            WebBrowser browser = ui.getSession().getBrowser();
            if (browser != null) {
                info.put(i18n.get("system_info_browser"), browserName(browser)
                        + " " + browser.getBrowserMajorVersion() + "." + browser.getBrowserMinorVersion());
                info.put(i18n.get("system_info_os"), osName(browser));
            }
        }
        return info;
    }

    /**
     * Renders the collected system information as a plain multi-line string
     * suitable for the submitted {@link FeedbackData}.
     */
    protected String formatSystemInfo(Map<String, String> info) {
        return info.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    protected static String browserName(WebBrowser browser) {
        if (browser.isChrome()) return "Chrome";
        if (browser.isFirefox()) return "Firefox";
        if (browser.isEdge()) return "Edge";
        if (browser.isSafari()) return "Safari";
        if (browser.isOpera()) return "Opera";
        if (browser.isIE()) return "Internet Explorer";
        return "Unknown";
    }

    protected static String osName(WebBrowser browser) {
        if (browser.isWindows()) return "Windows";
        if (browser.isMacOSX()) return "macOS";
        if (browser.isAndroid()) return "Android";
        if (browser.isIPhone()) return "iOS";
        if (browser.isChromeOS()) return "ChromeOS";
        if (browser.isLinux()) return "Linux";
        return "Unknown";
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
        Map<String, String> info = collectSystemInfo();
        feedbackData.setSystemInfo(info.isEmpty() ? null : formatSystemInfo(info));

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

    /**
     * Opens a new feedback dialog with a custom {@link SystemInfoGenerator}. The
     * generator is applied before the dialog opens, so the custom system info is
     * shown from the start.
     */
    public static DefaultFeedbackDialog show(FeedbackHandler handler, SystemInfoGenerator generator) {
        DefaultFeedbackDialog d = new DefaultFeedbackDialog();
        d.setSystemInfoGenerator(generator);
        d.open(handler);
        return d;
    }

}
