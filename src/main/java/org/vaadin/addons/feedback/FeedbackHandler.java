package org.vaadin.addons.feedback;

// Functional interface for feedback handling
@FunctionalInterface
public interface FeedbackHandler {
    void handleFeedback(FeedbackData feedbackData);
}
