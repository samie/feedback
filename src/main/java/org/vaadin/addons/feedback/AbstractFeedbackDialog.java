package org.vaadin.addons.feedback;

import com.vaadin.flow.component.dialog.Dialog;

public abstract class AbstractFeedbackDialog extends Dialog {

    private FeedbackHandler handler;

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
