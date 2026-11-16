package org.vaadin.addons.feedback;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextAreaElement;

/**
 * Integration test for the phone-first /mobile view, run at an iPhone-sized
 * viewport. Uses the same profile as {@link ExamplesIT} so the feedback email
 * can be checked in the embedded GreenMail server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("smtp-test")
public class MobileExampleIT extends AbstractBasePlaywrightIT {

    private static final int PHONE_WIDTH = 390;
    private static final int PHONE_HEIGHT = 844;

    @LocalServerPort
    private int port;

    @Autowired
    private GreenMail greenMail;

    @Override
    public String getUrl() {
        return "http://localhost:" + port + "/mobile";
    }

    @BeforeEach
    void usePhoneViewport() throws Exception {
        page.setViewportSize(PHONE_WIDTH, PHONE_HEIGHT);
        greenMail.purgeEmailFromAllMailboxes();
    }

    /** The icon-only button is found by its accessible name (aria-label). */
    private Locator feedbackButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send feedback"));
    }

    @Test
    @DisplayName("Icon-only button opens a full-screen dialog and emails the feedback")
    void shouldSendFeedbackFromFullScreenDialog() throws Exception {
        feedbackButton().click();

        TextAreaElement message = TextAreaElement.getByLabel(page, "Message");
        message.assertVisible();

        // Measure after the opening animation, which scales the overlay. The
        // selectors cover Vaadin 25 and the separate overlay element of Vaadin 24.
        page.waitForFunction("() => !document.querySelector('vaadin-dialog[opening], vaadin-dialog-overlay[opening]')");
        BoundingBox box = page.locator(".feedback-dialog [part='overlay']").boundingBox();
        assertEquals(0, box.x, 1);
        assertEquals(0, box.y, 1);
        assertEquals(PHONE_WIDTH, box.width, 1);
        assertEquals(PHONE_HEIGHT, box.height, 1);

        message.setValue("Order cards should show the delivery date");
        ButtonElement.getByText(page, "Send Feedback").click();

        NotificationElement.getByText(page, "Thanks! Your feedback was sent to the team.")
                .assertOpen();

        assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No feedback email received");
        MimeMessage mail = greenMail.getReceivedMessages()[0];
        assertEquals("[Feedback] Quick feedback via Mobile app bar", mail.getSubject());
        assertTrue(GreenMailUtil.getBody(mail).contains("Order cards should show the delivery date"));
    }

    @Test
    @DisplayName("Header close button dismisses the full-screen dialog")
    void shouldCloseFromHeader() {
        feedbackButton().click();
        TextAreaElement.getByLabel(page, "Message").assertVisible();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();

        assertThat(page.locator(".feedback-close")).hasCount(0);
    }
}
