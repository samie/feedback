package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

import java.util.Locale;

@Route("/")
public class AddonView extends VerticalLayout {

    public AddonView() {

        Select<Locale> localeSelect = new Select<>("Select locale", event -> {
            if (event.getValue() != null) {
                UI.getCurrent().setLocale(event.getValue());
            }
        });
        localeSelect.setItems(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.forLanguageTag("fi"));
        localeSelect.setValue(Locale.ENGLISH);
        add(localeSelect);

        // Simple dialog: asks only for a message.
        add(new Button("Feedback.simple()", event ->
                Feedback.simple(feedback -> {
                    add(new Text("Simple feedback: " + feedback.getMessage()));
                    Notification.show("Thank you for your simple feedback!",
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                })));

        // Full dialog: type, message, name and email.
        add(new Button("Feedback.normal()", event ->
                Feedback.normal(feedback -> {
                    add(new Text("Full feedback: " + feedback));
                    Notification.show("Thank you for your detailed feedback!",
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                })));

        // Register the error handler once so an unhandled exception opens a
        // pre-filled feedback dialog. Clicking the button below triggers one.
        Feedback.registerErrorHandler(UI.getCurrent(), feedback -> {
            add(new Text("Error feedback: " + feedback.getMessage()));
            Notification.show("Thank you for reporting the problem!",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        add(new Button("Trigger an error", event -> {
            throw new IllegalStateException("Demo error for the feedback dialog");
        }));
    }
}
