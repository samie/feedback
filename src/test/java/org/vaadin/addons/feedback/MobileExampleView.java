package org.vaadin.addons.feedback;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * A phone-first example: a compact app bar with an icon-only feedback button
 * above a list of order cards. On a phone the feedback dialog opens
 * full-screen. Every submission is emailed to the developer by
 * {@link FeedbackMailer}.
 */
@Route("mobile")
public class MobileExampleView extends VerticalLayout {

    record Order(String id, String customer, String product, String status) {
    }

    private final FeedbackMailer mailer;

    public MobileExampleView(FeedbackMailer mailer) {
        this.mailer = mailer;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createAppBar(), createOrderList());
    }

    private HorizontalLayout createAppBar() {
        H2 title = new H2("Acme Orders");
        title.getStyle().set("margin", "0").set("font-size", "1.125rem");

        // Icon-only button: the aria-label gives screen readers a name and the
        // tooltip explains the icon on desktop. 48px meets the iOS (44pt) and
        // Android (48dp) minimum tap target size.
        Button feedback = new Button(VaadinIcon.COMMENT_ELLIPSIS_O.create(),
                e -> Feedback.simple(handler("Mobile app bar")));
        feedback.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        feedback.setAriaLabel("Send feedback");
        feedback.setTooltipText("Send feedback");
        feedback.setMinWidth("48px");
        feedback.setMinHeight("48px");
        feedback.setId("mobile-feedback");

        HorizontalLayout appBar = new HorizontalLayout(title, feedback);
        appBar.setWidthFull();
        appBar.setAlignItems(FlexComponent.Alignment.CENTER);
        appBar.expand(title);
        appBar.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "0.25rem 0.25rem 0.25rem 1rem")
                .set("border-bottom", "1px solid var(--vaadin-border-color-secondary, var(--lumo-contrast-10pct))");
        return appBar;
    }

    private VerticalLayout createOrderList() {
        List<Order> orders = List.of(
                new Order("1001", "Alice Johnson", "Standing desk", "Shipped"),
                new Order("1002", "Bob Smith", "Office chair", "Processing"),
                new Order("1003", "Carol White", "Monitor arm", "Delivered"),
                new Order("1004", "David Brown", "Keyboard", "Shipped"),
                new Order("1005", "Eve Davis", "Desk lamp", "Cancelled"),
                new Order("1006", "Frank Miller", "Standing desk", "Processing"));

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.getStyle().set("overflow", "auto");
        orders.forEach(order -> list.add(createCard(order)));
        list.setFlexGrow(1);
        return list;
    }

    private Div createCard(Order order) {
        Span product = new Span(order.product());
        product.getStyle().set("font-weight", "600");
        Span details = new Span("#" + order.id() + " · " + order.customer() + " · " + order.status());
        details.getStyle().set("color", "var(--vaadin-text-color-secondary, var(--lumo-secondary-text-color))").set("font-size", "0.875rem");

        Div card = new Div(product, details);
        card.setWidthFull();
        card.getStyle()
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.25rem")
                .set("padding", "0.75rem 1rem")
                .set("border", "1px solid var(--vaadin-border-color-secondary, var(--lumo-contrast-10pct))")
                .set("border-radius", "var(--vaadin-radius-l, var(--lumo-border-radius-l))");
        return card;
    }

    /**
     * Emails the feedback to the developer and thanks the user.
     */
    private FeedbackHandler handler(String source) {
        return data -> {
            try {
                mailer.send(source, data);
                Notification.show("Thanks! Your feedback was sent to the team.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (RuntimeException e) {
                Notification.show("Sorry, your feedback could not be sent. Please try again later.",
                                5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };
    }
}
