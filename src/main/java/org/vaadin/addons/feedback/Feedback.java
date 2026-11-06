package org.vaadin.addons.feedback;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;


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
     * @return the opened DefaultFeedbackDialog instance, e.g. for calling
     *         {@link DefaultFeedbackDialog#setSystemInfoVisible(boolean)}
     */
    public static DefaultFeedbackDialog normal(FeedbackHandler handler) {
        return DefaultFeedbackDialog.show(handler);
    }

    /**
     * Opens the full feedback dialog with a custom {@link SystemInfoGenerator}
     * controlling the system information that is shown and submitted.
     *
     * @param handler   the callback to handle the feedback data when submitted
     * @param generator produces the system info (augment, replace, or return an
     *                  empty map to omit it)
     * @return the opened DefaultFeedbackDialog instance
     */
    public static DefaultFeedbackDialog normal(FeedbackHandler handler, SystemInfoGenerator generator) {
        return DefaultFeedbackDialog.show(handler, generator);
    }
    /**
     * Registers an error handler for the specified UI that shows a feedback dialog
     * whenever an unhandled exception occurs in that UI.
     * <p>
     * This allows users to report issues directly from the application when errors happen.
     * Calling this again for the same UI replaces its feedback handler, so it is safe to
     * call from a view constructor. The dialog always opens in the UI where the error
     * happened, and the session's previous error handler is still invoked afterwards.
     *
     * @param ui              the UI instance to register the error handler for
     * @param feedbackHandler the callback to handle the feedback data when submitted
     */
    public static void registerErrorHandler(UI ui, FeedbackHandler feedbackHandler) {
        // One handler per UI, stored on the UI itself: re-registering replaces it
        // and it goes away together with the UI.
        ComponentUtil.setData(ui, FeedbackErrorHandler.class.getName(), feedbackHandler);

        // Install the session-wide handler only once instead of wrapping it again
        // on every call, which would open one dialog per registration.
        VaadinSession session = ui.getSession();
        if (!(session.getErrorHandler() instanceof FeedbackErrorHandler)) {
            session.setErrorHandler(new FeedbackErrorHandler(session.getErrorHandler()));
        }
    }

    /**
     * Session error handler that opens a feedback dialog in the failing UI when
     * that UI has a registered {@link FeedbackHandler}, then chains to the
     * previously installed error handler.
     */
    private static final class FeedbackErrorHandler implements ErrorHandler {

        private final ErrorHandler previousHandler;

        private FeedbackErrorHandler(ErrorHandler previousHandler) {
            this.previousHandler = previousHandler;
        }

        @Override
        public void error(ErrorEvent event) {
            // The error handler runs on the request thread of the failing UI.
            UI ui = UI.getCurrent();
            FeedbackHandler feedbackHandler = ui == null ? null
                    : (FeedbackHandler) ComponentUtil.getData(ui, FeedbackErrorHandler.class.getName());

            if (feedbackHandler != null) {
                // accessSynchronously (not access) is required: the error handler runs
                // while the request thread holds the session lock, so opening the
                // dialog synchronously makes it part of the current response. access()
                // would defer the change until the lock is released, and without @Push
                // it would never reach the browser in this round-trip, leaving client
                // and server out of sync (stale dialog, resync / lost connection).
                ui.accessSynchronously(() -> {
                    var dialog = new SimpleFeedbackDialog() {{
                        this.message.setValue("An unexpected system error occurred. Report details:\n");
                        this.setHandler((FeedbackData d) -> {

                            // Prepend error details to the feedback message
                            String originalMessage = d.getMessage();
                            String errorDetails = event.getThrowable().toString();
                            d.setMessage(originalMessage + "\n\nSystem Error Details:\n" + errorDetails);

                            // Pass the feedback data to the provided handler
                            feedbackHandler.handleFeedback(d);
                        });

                    }};
                    dialog.open();
                });
            }

            // Call previous error handler last to ensure feedback dialog is shown first
            if (previousHandler != null) {
                previousHandler.error(event);
            }
        }
    }
}

