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
            UI.getCurrent().setLocale(event.getValue());
        });
        localeSelect.setItems(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.forLanguageTag("FI"));
        add(localeSelect);

        // Examples using the new Feedback utility class
        add(new Button("Feedback.simple()", event -> {
        Feedback.simple(feedback -> {
            add(new Text("Simple Feedback via Feedback.simple(): " + feedback.getMessage()));
            Notification.show("Thank you for your simple feedback!",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        }));

        add(new Button("Feedback.full()", event -> {
            Feedback.normal(feedback -> {
                add(new Text("Full Feedback via Feedback.full(): " + feedback));
                Notification.show("Thank you for your detailed feedback!",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
        }));

        add(new Button("Feedback.show() [alias for simple]", event -> {
            Feedback.registerErrorHandler(getUI().orElse(UI.getCurrent()), feedback -> {
                add(new Text("Feedback via Feedback.show(): " + feedback.getMessage()));
                Notification.show("Thank you for your feedback via show()!",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
        }));
    }
}
