package vn.edu.tdtu.edocument.notification.core;

import vn.edu.tdtu.edocument.document.model.Document;

public interface NotificationObserver {
    void update(UserProfile userProfile, Document doc);
}
