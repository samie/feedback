package org.vaadin.addons.feedback;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextAreaElement;

/**
 * Integration test for the realistic /examples view. Feedback is emailed to the
 * developer through the embedded GreenMail SMTP server, which lets the test
 * check the email that was actually sent.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ExamplesIT.TEST_SMTP_PORT)
public class ExamplesIT extends AbstractBasePlaywrightIT {

    /**
     * Keeps the test SMTP server off the dev server's port 3025. Shared with
     * {@link FeedbackIT} so both tests reuse the same Spring context.
     */
    static final String TEST_SMTP_PORT = "spring.mail.port=3026";

    @LocalServerPort
    private int port;

    @Autowired
    private GreenMail greenMail;

    @Override
    public String getUrl() {
        return "http://localhost:" + port + "/examples";
    }

    @BeforeEach
    void clearMailbox() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    @DisplayName("Navbar feedback is emailed to the developer")
    void shouldEmailNavbarFeedback() throws Exception {
        page.locator("#navbar-feedback").click();

        TextAreaElement message = TextAreaElement.getByLabel(page, "Message");
        message.assertVisible();
        message.setValue("The order list loads slowly in the morning");
        ButtonElement.getByText(page, "Send Feedback").click();

        NotificationElement.getByText(page, "Thanks! Your feedback was sent to the team.")
                .assertOpen();

        assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No feedback email received");
        MimeMessage mail = greenMail.getReceivedMessages()[0];
        assertEquals("developer@example.com", mail.getAllRecipients()[0].toString());
        assertEquals("[Feedback] Quick feedback via Navbar", mail.getSubject());
        assertTrue(GreenMailUtil.getBody(mail).contains("The order list loads slowly in the morning"));
    }

    @Test
    @DisplayName("Empty search offers to suggest the missing item")
    void shouldSuggestFromEmptySearch() {
        page.locator("input[placeholder='Search orders']").fill("invoice export");

        ButtonElement suggest = ButtonElement.getByText(page, "Suggest it");
        suggest.assertVisible();
        suggest.click();

        TextAreaElement.getByLabel(page, "Message").assertVisible();
        page.getByText("System information").click();
        assertThat(page.getByText("Search term: invoice export")).isVisible();
        assertThat(page.getByText("App version: " + ExamplesView.APP_VERSION)).isVisible();
    }
}
