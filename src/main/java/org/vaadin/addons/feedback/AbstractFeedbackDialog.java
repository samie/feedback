package org.vaadin.addons.feedback;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Base class for the feedback dialogs.
 * <p>
 * On phones (viewport up to 600px wide) the dialogs are shown full-screen with
 * a close button in the header, and on touch screens the buttons get 48px tap
 * targets. Remove the {@code feedback-dialog} class name to opt out.
 */
@StyleSheet("context://feedback/feedback-dialog.css")
public abstract class AbstractFeedbackDialog extends Dialog {

    private FeedbackHandler handler;

    /**
     * Header close button, shown only when the dialog is full-screen on a
     * phone. Subclasses set its accessible name in their localized texts.
     */
    protected final Button closeButton;

    protected AbstractFeedbackDialog() {
        addClassName("feedback-dialog");

        closeButton = new Button(VaadinIcon.CLOSE.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        closeButton.addClassName("feedback-close");
        getHeader().add(closeButton);
    }

    protected void processFeedback(FeedbackData feedbackData) {
        if (this.handler != null) {
            handler.handleFeedback(feedbackData);
        }
    }

    /**
     * Sets the handler invoked when the user submits feedback. Can be used
     * together with the no-argument {@link #open()} instead of
     * {@link #open(FeedbackHandler)}.
     */
    public void setHandler(FeedbackHandler handler) {
        this.handler = handler;
    }

    public void open(FeedbackHandler handler) {
        setHandler(handler);
        super.open();
    }

}
