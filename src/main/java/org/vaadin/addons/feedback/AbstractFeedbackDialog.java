package org.vaadin.addons.feedback;

import com.vaadin.flow.component.dialog.Dialog;

public abstract class AbstractFeedbackDialog extends Dialog {

    private FeedbackHandler handler;

    protected void processFeedback(FeedbackData feedbackData) {
        if (this.handler != null) {
            handler.handleFeedback(feedbackData);
        }
    }

    public void open(FeedbackHandler handler) {
        this.handler = handler;
        super.open();
    }

}
