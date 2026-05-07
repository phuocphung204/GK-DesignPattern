package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.UserPreference;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;

public class SMSNotification implements NotificationObserver {
    @Override
    public void update(Document doc) {
        if (!isEnabled(doc)) {
            return;
        }
        System.out.println(
                "  [GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println(
                "  [GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
    }

    private boolean isEnabled(Document doc) {
        if (doc == null) {
            return false;
        }
        UserPreference preference = doc.userPreference;
        return preference != null && preference.receiveSms;
    }
}
