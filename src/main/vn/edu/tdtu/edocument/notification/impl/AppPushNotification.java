package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import vn.edu.tdtu.edocument.notification.core.UserProfile;

public class AppPushNotification implements NotificationObserver {
    @Override
    public void update(UserProfile userProfile, Document doc) {
        if (doc == null) {
            return;
        }

        if (userProfile == null || !userProfile.isValidProfile()) {
            return;
        }

        System.out.println("[APP PUSH]  -> Gửi thông báo tới " + userProfile.getRole() + " (" + userProfile.getName()
                + "): Hồ sơ chuyển sang trạng thái " + doc.status);
    }
}
