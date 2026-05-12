package vn.edu.tdtu.edocument.document.model.enums;

import java.util.List;

public enum NotificationChannelType {
    EMAIL("Email"),
    SMS("SMS"),
    APP_PUSH("App Push");

    private final String displayName;
    public static List<NotificationChannelType> defaultPreference() {
        return List.of(NotificationChannelType.EMAIL);
    }

    NotificationChannelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
