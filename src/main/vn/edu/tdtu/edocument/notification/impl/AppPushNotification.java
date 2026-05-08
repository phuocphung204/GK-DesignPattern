package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.UserPreference;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;

public class AppPushNotification implements NotificationObserver {
    @Override
    public void update(Document doc) {
        if (!isEnabled(doc)) {
            return;
        }
        System.out.println(
                "  [APP PUSH]  -> Người nộp (" + doc.applicantName + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println(
                "  [APP PUSH]  -> Cán bộ xử lý (" + doc.officerName + "): Hồ sơ chuyển sang trạng thái " + doc.status);
    }

    private boolean isEnabled(Document doc) {
        if (doc == null) {
            return false;
        }
        UserPreference preference = doc.userPreference;
        return preference != null && preference.receiveAppPush;
    }
}
