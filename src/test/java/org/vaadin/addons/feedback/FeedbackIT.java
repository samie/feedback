package org.vaadin.addons.feedback;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
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
// Same profile as ExamplesIT so both tests reuse one Spring context
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("smtp-test")
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
        ButtonElement.getByText(page, "Feedback.normal()").assertVisible();
        ButtonElement.getByText(page, "Feedback.simple()").assertVisible();

        // Open the full feedback dialog
        ButtonElement.getByText(page, "Feedback.normal()").click();

        // Fill the message
        TextAreaElement message = TextAreaElement.getByLabel(page, "Message");
        message.assertVisible();
        message.setValue("This is a detailed bug report with sufficient length for validation");

        // The system information is shown in a collapsible section, collapsed
        // by default. Expanding it reveals the gathered client/app details.
        assertThat(page.getByText("System information")).isVisible();
        page.getByText("System information").click();
        assertThat(page.getByText("Vaadin version:")).isVisible();
        assertThat(page.getByText("Browser:")).isVisible();

        // Submit the form
        ButtonElement.getByText(page, "Send Feedback").click();

        // Verify the success notification appears with the expected message
        NotificationElement notification =
                NotificationElement.getByText(page, "Thank you for your detailed feedback");
        notification.assertOpen();
        notification.assertContent("Thank you for your detailed feedback");
    }

    @Test
    @DisplayName("Should open a single error dialog after re-entering the view")
    void shouldOpenSingleErrorDialogAfterReentering() {
        // Each visit re-creates AddonView, which registers the error handler again.
        for (int i = 0; i < 2; i++) {
            page.locator("a[href='examples']").click();
            page.locator("#navbar-feedback").waitFor();
            page.goBack();
            ButtonElement.getByText(page, "Trigger an error").assertVisible();
        }

        ButtonElement.getByText(page, "Trigger an error").click();

        TextAreaElement.getByLabel(page, "What were you doing? (optional)").assertVisible();
        assertThat(page.locator("vaadin-dialog[opened]")).hasCount(1);
    }

    @Test
    @DisplayName("Should send an error report with hidden exception details")
    void shouldSendErrorReportWithHiddenDetails() {
        ButtonElement.getByText(page, "Trigger an error").click();

        assertThat(page.getByText("System error", new Page.GetByTextOptions().setExact(true))).isVisible();
        // The exception is never shown to the user in the dialog.
        assertThat(page.getByText("IllegalStateException")).hasCount(0);

        // The message is optional: sending an empty one still reports the error.
        ButtonElement.getByText(page, "Send report").click();

        assertThat(page.getByText("Error feedback:")).containsText("IllegalStateException");
    }

    @Test
    @DisplayName("Should omit system information when disabled")
    void shouldOmitSystemInfoWhenDisabled() {
        ButtonElement.getByText(page, "No system info").click();

        TextAreaElement.getByLabel(page, "Message").assertVisible();
        // The system information section must not be present at all.
        assertThat(page.getByText("System information")).hasCount(0);
    }

    @Test
    @DisplayName("Should show augmented system information")
    void shouldShowAugmentedSystemInfo() {
        ButtonElement.getByText(page, "Augment system info").click();

        TextAreaElement.getByLabel(page, "Message").assertVisible();
        page.getByText("System information").click();
        assertThat(page.getByText("Vaadin version:")).isVisible();
        assertThat(page.getByText("App version: 4.2.1")).isVisible();
    }
}
