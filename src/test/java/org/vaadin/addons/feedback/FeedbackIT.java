package org.vaadin.addons.feedback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextAreaElement;

/**
 * Integration test for the feedback dialog, using the DramaFinder helper
 * elements (Playwright-based) instead of raw Playwright locators.
 * <p>
 * {@link AbstractBasePlaywrightIT} owns the Playwright/Browser/Page lifecycle,
 * navigates to {@link #getUrl()} and waits for Vaadin to finish loading before
 * each test. Headless is the default; pass {@code -Dheadless=false} to watch.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FeedbackIT extends AbstractBasePlaywrightIT {

    @LocalServerPort
    private int port;

    @Override
    public String getUrl() {
        return "http://localhost:" + port + "/";
    }

    @Test
    @DisplayName("Should successfully submit valid feedback")
    void shouldSubmitValidFeedback() {
        ButtonElement.getByText(page, "Feedback.full()").assertVisible();
        ButtonElement.getByText(page, "Feedback.simple()").assertVisible();

        // Open the full feedback dialog
        ButtonElement.getByText(page, "Feedback.full()").click();

        // Fill the message
        TextAreaElement message = TextAreaElement.getByLabel(page, "Message");
        message.assertVisible();
        message.setValue("This is a detailed bug report with sufficient length for validation");

        // Submit the form
        ButtonElement.getByText(page, "Send Feedback").click();

        // Verify the success notification appears with the expected message
        NotificationElement notification =
                NotificationElement.getByText(page, "Thank you for your detailed feedback");
        notification.assertOpen();
        notification.assertContent("Thank you for your detailed feedback");
    }
}
