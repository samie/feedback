# Feedback Add-on for Vaadin

A small Vaadin Flow add-on that shows a ready-made feedback dialog so users can send
comments, bug reports, or feature requests from within your application. It ships with two
ready-made dialogs, is built entirely from standard Vaadin components (a single server-side
dependency, no npm/JavaScript build), and is localized out of the box in English, German,
Finnish, and French.

This component was made as part of my [Vaadin Create 2026](https://vaadin.com/vaadin-create-2026) talk. 

## Usage

### Adding the dependency

```xml
        <dependency>
            <groupId>org.vaadin.addons.feedback</groupId>
            <artifactId>feedback</artifactId>
            <version>1.0.0</version>
        </dependency>
```

### Quick start

```java
// Add a button anywhere in your view
add(new Button("Send feedback", e ->
        Feedback.simple(data -> Notification.show("Thanks! " + data.getMessage()))));
```

See the [Features](#features) section for the full dialog, crash reporting, and
customization.

## Features

Everything is driven from one static entry point, `Feedback`. You pass a handler and the
add-on takes care of the dialog, validation, and localization.

### Simple dialog — one-line feedback collection

`Feedback.simple(...)` opens a lightweight, message-only dialog. Ideal for a "Send feedback"
button where you just want a quick comment. The handler receives the typed message.

```java
add(new Button("Send feedback", e ->
        Feedback.simple(data -> Notification.show("Thanks! " + data.getMessage()))));
```

### Full dialog — structured feedback

`Feedback.normal(...)` opens a richer dialog with a feedback **type** selector
(General Feedback / Bug Report / Feature Request), the message, and optional **name** and
**email** fields. Read whichever fields you need from the `FeedbackData`:

```java
add(new Button("Send detailed feedback", e -> Feedback.normal(data -> {
    log.info("[{}] {} <{}>: {}",
            data.getType(), data.getName(), data.getEmail(), data.getMessage());
    Notification.show("Thanks for the details!");
})));
```

The message is required (up to 1000 characters); name and email are optional. Both dialogs
are modal and can be submitted by pressing **Enter**.

### System information

The full dialog gathers non-sensitive details about the running application and the user's
client — Vaadin version, current view, browser, OS and language — and shows them in a
collapsible **System information** section (collapsed by default). The same details are
attached to the submitted `FeedbackData.getSystemInfo()`.

Because this is both shown to the user and transmitted with the feedback, it contains only
client/app values; server internals (Java version, server OS, file paths, IP address) are
never included — keep that in mind if you customize it, and never add secrets.

**Disable it** when system information isn't desirable:

```java
Feedback.normal(data -> saveFeedback(data))
        .setSystemInfoVisible(false);
```

**Augment it** with your own fields (for example your application version) by starting from
the built-in details and adding to them:

```java
Feedback.normal(
        data -> saveFeedback(data),
        ui -> {
            Map<String, String> info = DefaultFeedbackDialog.defaultSystemInfo(ui);
            info.put("App version", "4.2.1");
            return info;
        });
```

The generator is a `SystemInfoGenerator` (a serializable `UI -> Map<String, String>`).
Return a fresh map to replace the built-in details entirely, or an empty map to omit them.

### Feedback types

The full dialog's type selector is a `Select` populated from the i18n bundle, not a Java enum.
Three categories ship out of the box, keyed by `type_general`, `type_bug`, and `type_feature`,
with **General Feedback** selected by default:

| Bundle key     | English label    |
|----------------|------------------|
| `type_general` | General Feedback |
| `type_bug`     | Bug Report       |
| `type_feature` | Feature Request  |

The value delivered in `FeedbackData.getType()` is the **translated label** for the current
locale (e.g. `"Bug Report"` in English, `"Fehlerbericht"` in German) — not a stable key — so
switch on your own logic rather than on the display string if you localize. Because the labels
come from the bundle, translating them is just a matter of overriding those three keys per
language:

```properties
# messages_de.properties
type=Typ
type_general=Allgemeines Feedback
type_bug=Fehlerbericht
type_feature=Feature-Anfrage
```

To change the set of categories (add, remove, or rename), subclass `DefaultFeedbackDialog` and
override `createFeedbackForm()` to call `type.setItems(...)` with your own values.

### Crash reporting

`Feedback.registerErrorHandler(...)` turns unhandled exceptions into user-submitted reports.
When an error occurs on the given `UI`, the add-on opens a generic, localized **System error**
dialog (`SystemErrorDialog`) with a short explanation and an optional "What were you doing?"
field. The exception is never shown to the user: if they click **Send report**, the exception
details are appended to their (possibly empty) message and your handler is invoked; **Close**
discards the report. The add-on then chains to any previously installed error handler. Calling it again for the same `UI`
replaces that UI's handler, so it is safe to call from a view constructor.

```java
Feedback.registerErrorHandler(UI.getCurrent(),
        data -> log.warn("User-reported error: {}", data.getMessage()));
```

### The `FeedbackData` object

Your handler always receives a `FeedbackData`. Which fields are populated depends on the
dialog: the simple dialog sets only `message`; the full dialog also sets `type`, `name`, and
`email`.

```java
Feedback.normal(data -> {
    String type    = data.getType();     // e.g. "Bug Report" (full dialog only)
    String message = data.getMessage();  // always present
    String name     = data.getName();    // optional, full dialog only
    String email    = data.getEmail();   // optional, full dialog only
    // store it, email it, open a ticket, ...
});
```

> The full dialog also sets `systemInfo` (see [System information](#system-information)); the
> simple dialog leaves it `null`.

### Built-in internationalization

Dialog texts are read from resource bundles and follow the current `UI` locale. Translations
ship for **English** (default), **German**, **Finnish**, and **French**. To add another
language, drop a `messages_<lang>.properties` file for the add-on's package on the classpath:

```properties
# messages_es.properties
title=Enviar comentarios
message=Mensaje
button_send=Enviar
# ... see messages.properties for the full set of keys
```

### Lightweight & customizable

The add-on's only runtime dependency is `com.vaadin:vaadin-core` — there is no custom web
component, no TypeScript, and no npm build step. When you need to change the layout or
behavior, subclass `SimpleFeedbackDialog` or `DefaultFeedbackDialog` and override the
`protected` `createHeader()`, `createForm()` / `createFeedbackForm()`, or `createButtons()`
hooks (the `errorDuration` field, default 3000 ms, controls how long validation notifications
stay on screen):

```java
public class MyFeedbackDialog extends SimpleFeedbackDialog {
    public MyFeedbackDialog() {
        errorDuration = 5000; // show validation errors a little longer
    }

    @Override
    protected void createHeader() {
        add(new H3("We'd love your thoughts"));
    }
}
```

## Add-on architecture

![server-side-addon](https://user-images.githubusercontent.com/991105/211870086-75544597-847d-4d21-82fa-341411753558.svg)

## Development instructions

### Important files
* `Feedback.java`: the entry-point utility class (`simple`, `normal`, `registerErrorHandler`).
* `DefaultFeedbackDialog` / `SimpleFeedbackDialog`: the dialog implementations.
* `AddonView.java` (test folder): a demo view for trying the component. Classes under the test
  folder are not packaged in the add-on JAR.
* `assembly/`: packaging configuration for the Vaadin Directory ZIP. There is usually no need to
  modify these files unless you need extra JAR manifest entries.

### Running the demo

The demo is the Spring Boot application `TestApplication` (in the test sources). During
development you typically just run that class from your IDE. To start it from the terminal:

```
mvn spring-boot:test-run
```

`test-run` uses the test runtime classpath, so the demo views under `src/test/java`
(`AddonView`, `TestApplication`) are picked up without packaging them into the add-on JAR.
It is also the default goal, so plain `mvn` works too. The demo starts at
http://localhost:9999 (see `src/test/resources/application.properties`).

### Realistic examples

http://localhost:9999/examples (`ExamplesView`) shows the dialog the way an application
would use it. There are four entry points: a **Feedback** button in the navbar, a **Send
feedback** item in the user menu, a **Suggest it** button when an order search finds nothing,
and a floating button in the bottom-right corner. Each one adds its own context (app version,
signed-in user, search term) to the system information.

Every submission is emailed to the developer by `FeedbackMailer` through Spring Mail:

```java
Feedback.normal(data -> {
    mailer.send("Navbar", data);   // emails feedback.developer-email
    Notification.show("Thanks! Your feedback was sent to the team.");
});
```

Mail is configured in `application.properties`:

```properties
feedback.developer-email=developer@example.com
feedback.embedded-smtp=true   # embedded GreenMail SMTP server, no Docker needed
spring.mail.host=localhost
spring.mail.port=3025
```

The embedded [GreenMail](https://greenmail-mail-test.github.io/greenmail/) server receives
the email, and the console logs each send. To use a real SMTP server, set
`feedback.embedded-smtp=false` and point `spring.mail.*` at it. If you remove
`spring.mail.host`, the email is only logged.

### Integration tests

The integration test (`FeedbackIT`) uses [DramaFinder](https://github.com/parttio/dramafinder)
(Playwright-based) and runs through the Spring Boot test server:

```
mvn verify -Pit
```

Tests run headless by default. To watch the browser, add `-Dheadless=false`.
To run the same test against the Vaadin 24.9 baseline, add `-Pvaadin-24`.

## Publishing to Vaadin Directory

Set `organization.name` in `pom.xml` to your own name/organization, then build the ZIP:

```
mvn versions:set -DnewVersion=1.0.0   # snapshot versions cannot be published
mvn clean install -Pdirectory
```

The package is created as `target/feedback-1.0.0.zip`. To upload it manually, visit
https://vaadin.com/directory/my-components?uploadNewComponent

### Uploading with the Directory API

`scripts/publish-directory.sh` uploads the ZIP using the Vaadin Directory upload API. By default the
version is uploaded as a draft; set `PUBLISH=true` to publish it right away.

```
DIRECTORY_API_KEY=<key> RELEASE_NOTES="Bug fixes" scripts/publish-directory.sh
DIRECTORY_API_KEY=<key> PUBLISH=true RELEASE_NOTES_FILE=notes.md scripts/publish-directory.sh --build
```

`--build` runs `mvn clean install -Pdirectory` before uploading.

### Automated release with GitHub Actions

The workflow `.github/workflows/publish-directory.yml` runs the same script. Add the API key as the
repository secret `DIRECTORY_API_KEY`, then:

1. Set the version (`mvn versions:set -DnewVersion=1.2.0 && mvn versions:commit`), commit and push.
2. Create a GitHub Release with tag `v1.2.0`. The release description is used as the release notes.
   - A normal release is published in Directory immediately.
   - A pre-release is uploaded as an unpublished draft.
3. The workflow checks that the tag matches the project version, builds the ZIP, uploads it to
   Directory and attaches it to the GitHub Release.

The workflow can also be started manually from the Actions tab, with a choice to publish or upload a draft.
