package org.vaadin.addons.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Emails submitted feedback to the developer address configured with
 * {@code feedback.developer-email}.
 * <p>
 * Spring Boot only creates a {@link JavaMailSender} when {@code spring.mail.host}
 * is set. Without one, the email is logged instead so the demo runs anywhere.
 */
@Service
public class FeedbackMailer {

    private static final Logger log = LoggerFactory.getLogger(FeedbackMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String developerEmail;
    private final String fromEmail;

    public FeedbackMailer(ObjectProvider<JavaMailSender> mailSender,
                          @Value("${feedback.developer-email}") String developerEmail,
                          @Value("${feedback.from-email:noreply@example.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.developerEmail = developerEmail;
        this.fromEmail = fromEmail;
    }

    /**
     * Sends the feedback to the developer.
     *
     * @param source where in the UI the feedback was given, e.g. "Navbar"
     * @param data   the submitted feedback
     * @throws MailException if the mail server rejects the message
     */
    public void send(String source, FeedbackData data) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(developerEmail);
        if (hasText(data.getEmail())) {
            // Lets the developer answer the user directly
            mail.setReplyTo(data.getEmail());
        }
        mail.setSubject("[Feedback] " + (hasText(data.getType()) ? data.getType() : "Quick feedback")
                + " via " + source);
        mail.setText(body(source, data));

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("No mail server configured, feedback email not sent:\nTo: {}\nSubject: {}\n\n{}",
                    developerEmail, mail.getSubject(), mail.getText());
            return;
        }
        try {
            sender.send(mail);
            log.info("Sent feedback email '{}' to {}", mail.getSubject(), developerEmail);
        } catch (MailException e) {
            log.error("Failed to send feedback email to {}", developerEmail, e);
            throw e;
        }
    }

    private static String body(String source, FeedbackData data) {
        StringBuilder body = new StringBuilder();
        body.append(data.getMessage()).append("\n\n---\n");
        body.append("Source: ").append(source).append('\n');
        if (hasText(data.getName())) {
            body.append("Name: ").append(data.getName()).append('\n');
        }
        if (hasText(data.getEmail())) {
            body.append("Email: ").append(data.getEmail()).append('\n');
        }
        if (hasText(data.getSystemInfo())) {
            body.append("\nSystem information:\n").append(data.getSystemInfo()).append('\n');
        }
        return body.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
