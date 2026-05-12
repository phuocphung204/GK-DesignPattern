package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import java.util.List;

public class SMSNotification implements NotificationObserver {
    @Override
    public void update(Document doc) {

        if (doc == null) {
            return;
        }

        boolean applicantEnabled = contains(doc.applicantPreference, NotificationChannelType.SMS);
        boolean officerEnabled = contains(doc.officerPreference, NotificationChannelType.SMS);
        if (!applicantEnabled && !officerEnabled) {
            return;
        }

        if (applicantEnabled) {
            System.out.println("[GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ chuyển sang trạng thái "
                    + doc.status);
        }
        if (officerEnabled) {
            System.out.println("[GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Hồ sơ chuyển sang trạng thái "
                    + doc.status);
        }
    }

    private static boolean contains(List<NotificationChannelType> preferences, NotificationChannelType type) {
        return preferences != null && type != null && preferences.contains(type);
    }
}
