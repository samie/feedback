package org.vaadin.addons.feedback;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

/**
 * Realistic examples of where a feedback button lives in an application. Every
 * submission is emailed to the developer by {@link FeedbackMailer}.
 * <ol>
 *     <li>A "Feedback" button in the navbar, always one click away.</li>
 *     <li>A "Send feedback" item in the user menu.</li>
 *     <li>A "Suggest it" button in the empty state of a search.</li>
 *     <li>A floating button in the bottom-right corner of the page.</li>
 * </ol>
 */
@Route("examples")
public class ExamplesView extends VerticalLayout {

    static final String APP_VERSION = "4.2.1";
    static final String USER_NAME = "Jane Doe";

    record Order(String id, String customer, String product, String status) {
    }

    private final FeedbackMailer mailer;

    public ExamplesView(FeedbackMailer mailer) {
        this.mailer = mailer;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createHeader(), createContent(), createFloatingButton());
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Acme Orders");
        title.getStyle().set("margin", "0").set("font-size", "1.25rem");

        // 1. Navbar button: a quick, message-only dialog.
        Button feedback = new Button("Feedback", VaadinIcon.COMMENT_ELLIPSIS_O.create(),
                e -> Feedback.simple(handler("Navbar")));
        feedback.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        feedback.setId("navbar-feedback");

        // 2. User menu: the full dialog, with the signed-in user in the system info.
        MenuBar userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        SubMenu items = userMenu.addItem(new Avatar(USER_NAME)).getSubMenu();
        items.addItem("Profile", e -> Notification.show("Profile"));
        items.addItem("Settings", e -> Notification.show("Settings"));
        items.addItem("Send feedback", e -> Feedback.normal(handler("User menu"),
                withInfo(Map.of("Signed-in user", USER_NAME))));
        items.addItem("Sign out", e -> Notification.show("Signed out"));

        HorizontalLayout header = new HorizontalLayout(title, feedback, userMenu);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.getStyle()
                .set("padding", "0.5rem 1rem")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct, #ddd)");
        return header;
    }

    private VerticalLayout createContent() {
        ListDataProvider<Order> orders = new ListDataProvider<>(List.of(
                new Order("1001", "Alice Johnson", "Standing desk", "Shipped"),
                new Order("1002", "Bob Smith", "Office chair", "Processing"),
                new Order("1003", "Carol White", "Monitor arm", "Delivered"),
                new Order("1004", "David Brown", "Keyboard", "Shipped"),
                new Order("1005", "Eve Davis", "Desk lamp", "Cancelled"),
                new Order("1006", "Frank Miller", "Standing desk", "Processing"),
                new Order("1007", "Grace Wilson", "Webcam", "Delivered"),
                new Order("1008", "Henry Moore", "Headset", "Shipped"),
                new Order("1009", "Ivy Taylor", "Office chair", "Delivered"),
                new Order("1010", "Jack Anderson", "Cable tray", "Processing")));

        Grid<Order> grid = new Grid<>();
        grid.addColumn(Order::id).setHeader("Order").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Order::customer).setHeader("Customer");
        grid.addColumn(Order::product).setHeader("Product");
        grid.addColumn(Order::status).setHeader("Status");
        grid.setItems(orders);

        TextField search = new TextField();
        search.setPlaceholder("Search orders");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setWidth("20rem");

        // 3. Empty state: turn a failed search into a feature request.
        Span noResults = new Span();
        Button suggest = new Button("Suggest it", e -> Feedback.normal(handler("Empty search"),
                withInfo(Map.of("Search term", search.getValue()))));
        suggest.addThemeVariants(ButtonVariant.LUMO_SMALL);
        VerticalLayout emptyState = new VerticalLayout(noResults, suggest);
        emptyState.setAlignItems(FlexComponent.Alignment.CENTER);
        grid.setEmptyStateComponent(emptyState);

        search.addValueChangeListener(e -> {
            String term = e.getValue().trim().toLowerCase();
            orders.setFilter(order -> term.isEmpty()
                    || order.id().contains(term)
                    || order.customer().toLowerCase().contains(term)
                    || order.product().toLowerCase().contains(term)
                    || order.status().toLowerCase().contains(term));
            noResults.setText("No orders match \"" + e.getValue() + "\". Can't find what you need?");
        });

        VerticalLayout content = new VerticalLayout(search, grid);
        content.setSizeFull();
        content.setFlexGrow(1, grid);
        return content;
    }

    private Button createFloatingButton() {
        // 4. Floating button: always visible, independent of the page layout.
        Button floating = new Button("Feedback", VaadinIcon.COMMENT_O.create(),
                e -> Feedback.normal(handler("Floating button"), withInfo(Map.of())));
        floating.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        floating.setId("floating-feedback");
        floating.getStyle()
                .set("position", "fixed")
                .set("bottom", "var(--lumo-space-l, 1.5rem)")
                .set("right", "var(--lumo-space-l, 1.5rem)")
                .set("z-index", "100")
                .set("box-shadow", "var(--lumo-box-shadow-m, 0 2px 8px rgba(0, 0, 0, 0.25))");
        return floating;
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

    /**
     * Starts from the built-in system info and adds the app version plus the
     * given context, so the developer knows where the feedback came from.
     */
    private static SystemInfoGenerator withInfo(Map<String, String> extra) {
        return ui -> {
            var info = DefaultFeedbackDialog.defaultSystemInfo(ui);
            info.put("App version", APP_VERSION);
            info.putAll(extra);
            return info;
        };
    }
}
