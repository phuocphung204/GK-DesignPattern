package vn.edu.tdtu.edocument.notification.core;

import vn.edu.tdtu.edocument.model.Document;

public interface NotificationObserver {
    void update(Document doc);
}
