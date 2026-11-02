package org.vaadin.addons.feedback;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableFunction;

import java.util.Map;

/**
 * Produces the system information shown in (and submitted with) a
 * {@link DefaultFeedbackDialog}, as an ordered map of label to value. The
 * active {@link UI} is passed in (may be {@code null} before the dialog is
 * attached).
 * <p>
 * Typical uses:
 * <ul>
 *   <li><b>Augment</b> the built-in details: start from
 *       {@link DefaultFeedbackDialog#defaultSystemInfo(UI)} and add entries.</li>
 *   <li><b>Replace</b> them: build and return a fresh map.</li>
 *   <li><b>Disable</b> system info: return an empty map (nothing is shown or
 *       sent). See also {@link DefaultFeedbackDialog#setSystemInfoVisible(boolean)}.</li>
 * </ul>
 * <p>
 * Whatever is returned is both displayed to the end user and transmitted with
 * the feedback, so never include secrets or sensitive server-side details.
 * <p>
 * Extends {@link SerializableFunction} because the dialog (a Vaadin component)
 * must remain serializable for session replication.
 */
@FunctionalInterface
public interface SystemInfoGenerator extends SerializableFunction<UI, Map<String, String>> {
}
