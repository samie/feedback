package org.vaadin.addons.feedback;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;


/**
 * Utility class providing static methods for easily opening different styles of feedback dialogs.
 * <p>
 * This class serves as a convenient entry point for displaying feedback dialogs without having
 * to instantiate the dialog classes directly.
 * <p>
 * Usage examples:
 * <pre>
 * // Open a simple feedback dialog
 * Feedback.simple(data -> System.out.println("Feedback: " + data.getMessage()));
 *
 * // Open a full-featured feedback dialog
 * Feedback.full(data -> System.out.println("Feedback: " + data));
 *
 * // Wrap operations with error handling that shows feedback dialog on errors
 * Feedback.withErrorHandler(
 *     () -> riskyOperation(),
 *     data -> System.out.println("Error feedback: " + data.getMessage())
 * );
 * </pre>
 */
public final class Feedback {

    // Private constructor to prevent instantiation
    private Feedback() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Opens a simple feedback dialog that only asks for a message.
     * This is a lightweight option suitable for quick feedback collection.
     *
     * @param handler the callback to handle the feedback data when submitted
     *                @return the opened SimpleFeedbackDialog instance
     */
    public static SimpleFeedbackDialog simple(FeedbackHandler handler) {
        return SimpleFeedbackDialog.show(handler);
    }

    /**
     * Opens a comprehensive feedback dialog with full form fields including
     * feedback type selection, name, email, and message.
     * This is suitable when you need detailed feedback information.
     *
     * @param handler the callback to handle the feedback data when submitted
     */
    public static void normal(FeedbackHandler handler) {
        DefaultFeedbackDialog.show(handler);
    }
    /**
     * Registers a global error handler for the specified UI that shows a feedback dialog
     * whenever an unhandled exception occurs.
     * <p>
     * This allows users to report issues directly from the application when errors happen.
     *
     * @param ui              the UI instance to register the error handler for
     * @param feedbackHandler the callback to handle the feedback data when submitted
     */
    public static void registerErrorHandler(UI ui, FeedbackHandler feedbackHandler) {
        var previousHandler = ui.getSession().getErrorHandler();
        ui.getSession().setErrorHandler(event -> {

            // Handle errors with previous handler last
            event.getComponent().flatMap(Component::getUI).ifPresent(errorUI -> {

                    // Show feedback dialog only on the specified UI
                    if (ui.equals(errorUI)) {
                        errorUI.access(() -> {
                            var dialog = new SimpleFeedbackDialog() {{
                                this.message.setValue("An unexpected system error occurred. Report details:\n");
                                this.handler = (FeedbackData d) -> {

                                    // Prepend error details to the feedback message
                                    String originalMessage = d.getMessage();
                                    String errorDetails = event.getThrowable().toString();
                                    d.setMessage(originalMessage + "\n\nSystem Error Details:\n" + errorDetails);

                                    // Pass the feedback data to the provided handler
                                    feedbackHandler.handleFeedback(d);
                                };

                            }};
                            dialog.open();
                        });
                    }
            });

            // Call previous error handler last to ensure feedback dialog is shown first
            if (previousHandler != null) {
                previousHandler.error(event);
            };
        });
    }
}

