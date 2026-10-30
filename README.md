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
When an error occurs on the given `UI`, the add-on opens a simple dialog pre-filled with a
short notice, appends the exception details to whatever the user writes, invokes your handler,
and then chains to any previously installed error handler.

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

> `FeedbackData` also has a `systemInfo` field reserved for future use — the built-in dialogs
> do not populate it (the running Vaadin version is only shown in the full dialog's form).

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

```
mvn jetty:run
```

This deploys the demo at http://localhost:8080

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
mvn clean package -Pdirectory
```

The package is created as `target/feedback-1.0.0.zip`. To upload it, visit
https://vaadin.com/directory/my-components?uploadNewComponent
