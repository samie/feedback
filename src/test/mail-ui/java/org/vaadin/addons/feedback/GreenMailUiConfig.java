package org.vaadin.addons.feedback;

import com.icegreen.greenmail.util.GreenMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.vaadin.greenmail.ui.GreenMailUi;

/**
 * Web inbox for the emails the demo sends to the embedded GreenMail server.
 * Only compiled with the {@code mail-ui} Maven profile, which also activates
 * the {@code mail-ui} Spring profile ({@code application-mail-ui.properties}).
 */
@Configuration
@Profile("mail-ui")
class GreenMailUiConfig {

    private static final Logger log = LoggerFactory.getLogger(GreenMailUiConfig.class);

    @Bean(destroyMethod = "stop")
    GreenMailUi greenMailUi(GreenMail greenMail) {
        GreenMailUi ui = GreenMailUi.start(greenMail);
        log.info("GreenMail inbox: {}", ui.getUrl());
        return ui;
    }
}
