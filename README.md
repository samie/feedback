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
            <version>1.2.0</version>
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

No mail server is configured for the demo, so `FeedbackMailer` prints each email (To,
Subject and body) to the console instead of sending it. To send real email, point
`spring.mail.*` in `application.properties` at an SMTP server:

```properties
feedback.developer-email=developer@example.com
spring.mail.host=smtp.example.com
spring.mail.port=587
```

The integration tests run with the `smtp-test` profile (`application-smtp-test.properties`),
which starts an embedded [GreenMail](https://greenmail-mail-test.github.io/greenmail/) SMTP
server. `ExamplesIT` checks that the emails are actually delivered.

### Integration tests

The integration test (`FeedbackIT`) uses [DramaFinder](https://github.com/parttio/dramafinder)
(Playwright-based) and runs through the Spring Boot test server:

```
mvn verify -Pit
```

Tests run headless by default. To watch the browser, add `-Dheadless=false`.
To run the same test against the Vaadin 24.9 baseline, add `-Pvaadin-24`.

## Publishing to Vaadin Directory

Releases are made by pushing a tag. The workflow `.github/workflows/publish-directory.yml` takes
the version from the tag (`v1.2.0` → `1.2.0`; only the tag has the `v`), builds the ZIP, uploads it
to Vaadin Directory and creates a GitHub Release with the same notes and a link to Directory.
The workflow never commits, so `pom.xml` keeps its `-SNAPSHOT` development version.

One-time setup: get the Directory API key for the component and add it as a repository secret:

```
gh secret set DIRECTORY_API_KEY
```

### Making a release

1. Sync and test: `git switch main && git pull --ff-only`, then `mvn -B verify -Pit`.
2. Optionally update the dependency snippet in this README to the new version, then commit.
3. Push `main`, so the commit you tag is on GitHub: `git push origin main`.
4. Tag it: `git tag -a v1.2.0`. The tag message is used as the release notes (first line is the
   summary). Without a message, the commit subjects since the previous tag are used.
5. Push the tag: `git push origin v1.2.0`, and follow the run with `gh run watch`.
6. Check that Directory lists 1.2.0 and that `gh release view v1.2.0` shows the notes.

A tag with a suffix, such as `v1.2.0-rc1`, is uploaded as an unpublished Directory draft and marked
as a GitHub pre-release.

When you start work toward the next minor, bump the development version and commit it:
`mvn versions:set -DnewVersion=1.3-SNAPSHOT -DgenerateBackupPoms=false`. Patch releases need no bump.

If the build or the Directory upload fails, nothing is published: fix the problem, push `main`,
move the tag (`git tag -d v1.2.0 && git push origin :refs/tags/v1.2.0`) and tag again. If only the
GitHub Release step fails, don't re-run the workflow (Directory would get a second upload); create
the release with `gh release create v1.2.0 --title 1.2.0 --verify-tag` instead.

### Building the ZIP manually

Set `organization.name` in `pom.xml` to your own name/organization, then:

```
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false
mvn clean install -Pdirectory
```

The package is created as `target/feedback-1.2.0.zip`. To upload it manually, visit
https://vaadin.com/directory/my-components?uploadNewComponent
