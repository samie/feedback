package org.vaadin.addons.feedback;

// Data class for feedback
public class FeedbackData {
    private String message;
    private String name;
    private String email;
    private String type;
    private String systemInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(String systemInfo) {
        this.systemInfo = systemInfo;
    }

    @Override
    public String toString() {
        return String.format("FeedbackData{name='%s', email='%s', type='%s'}",
                name, email, type);
    }

}
