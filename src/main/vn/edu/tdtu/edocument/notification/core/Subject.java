package vn.edu.tdtu.edocument.notification.core;

import vn.edu.tdtu.edocument.document.model.Document;

public interface Subject {
    void attach(NotificationObserver observer);

    void detach(NotificationObserver observer);

    void notifyObservers(Document doc);
}
