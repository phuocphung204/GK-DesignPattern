package vn.edu.tdtu.edocument.document.model.enums;

public enum NotificationChannelType {
    EMAIL("Email"),
    SMS("SMS"),
    APP_PUSH("App Push");

    private final String displayName;

    NotificationChannelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
