package org.vaadin.addons.feedback;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Selects the demo's theme explicitly. The theme API differs between Vaadin
 * versions, so this class lives in a version-specific test source folder that
 * the pom picks: this one for Vaadin 25, {@code src/test/vaadin-24} for the
 * Vaadin 24 baseline. The add-on itself never chooses a theme.
 */
@StyleSheet(Aura.STYLESHEET)
public class AppShell implements AppShellConfigurator {
}
