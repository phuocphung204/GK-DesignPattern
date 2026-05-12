package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import java.util.List;

public class AppPushNotification implements NotificationObserver {
    @Override
    public void update(Document doc) {
        if (doc == null) {
            return;
        }

        boolean applicantEnabled = contains(doc.applicantPreference, NotificationChannelType.APP_PUSH);
        boolean officerEnabled = contains(doc.officerPreference, NotificationChannelType.APP_PUSH);
        if (!applicantEnabled && !officerEnabled) {
            return;
        }
        if (applicantEnabled) {
            System.out.println("[APP PUSH]  -> Người nộp (" + doc.applicantName
                + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        }
        if (officerEnabled) {
            System.out.println("[APP PUSH]  -> Cán bộ xử lý (" + doc.officerName
                + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        }
    }

    private static boolean contains(List<NotificationChannelType> preferences, NotificationChannelType type) {
        return preferences != null && type != null && preferences.contains(type);
    }
}
