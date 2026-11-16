package org.vaadin.addons.feedback;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Selects the demo's theme on the Vaadin 24 baseline, which has no Aura theme.
 * See {@code src/test/vaadin-25} for the Vaadin 25 version.
 */
@Theme(themeClass = Lumo.class)
public class AppShell implements AppShellConfigurator {
}
