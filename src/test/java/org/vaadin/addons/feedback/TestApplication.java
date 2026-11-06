package org.vaadin.addons.feedback;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * The entry point of the Spring Boot test/demo application.
 */
@SpringBootApplication
public class TestApplication  {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    /**
     * Embedded SMTP server that receives the feedback emails sent by the
     * /examples demo, so no external mail server is needed. Disable with
     * {@code feedback.embedded-smtp=false} to send through a real server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "feedback.embedded-smtp", havingValue = "true")
    GreenMail greenMail(@Value("${spring.mail.port:3025}") int port) {
        return new GreenMail(new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP));
    }
}
