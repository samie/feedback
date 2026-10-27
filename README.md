# Feedback Add-on for Vaadin

A small Vaadin Flow add-on that shows a ready-made feedback dialog so users can send
comments, bug reports, or feature requests from within your application. It is built from
standard Vaadin components (no third-party JavaScript) and ships with English, German,
Finnish, and French translations.

## Usage

### Adding the dependency

```xml
        <dependency>
            <groupId>org.vaadin.addons.feedback</groupId>
            <artifactId>feedback</artifactId>
            <version>1.0.0</version>
        </dependency>
```

### Examples

```java
// Simple dialog — asks only for a message
add(new Button("Send feedback", e ->
        Feedback.simple(data -> Notification.show("Thanks! " + data.getMessage()))));

// Full dialog — type, message, name, and email
add(new Button("Send detailed feedback", e ->
        Feedback.normal(data -> Notification.show("Thanks for the details!"))));

// Let users report unhandled errors through a feedback dialog
Feedback.registerErrorHandler(UI.getCurrent(),
        data -> log.warn("User-reported error: {}", data.getMessage()));
```

The handler receives a `FeedbackData` object with the message and, for the full dialog,
the feedback type, name, and email.

### Internationalization

Dialog texts are read from `messages.properties` (with `messages_de`, `messages_fi`, and
`messages_fr` variants) and follow the current `UI` locale. Add a `messages_<lang>.properties`
file on the classpath to provide further translations.

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
