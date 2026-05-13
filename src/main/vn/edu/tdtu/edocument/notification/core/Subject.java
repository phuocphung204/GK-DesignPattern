package vn.edu.tdtu.edocument.notification.core;

import java.util.List;

import vn.edu.tdtu.edocument.document.model.Document;

public interface Subject {
    void attach(List<NotificationObserver> observers, NotificationObserver observer);

    void detach(List<NotificationObserver> observers, NotificationObserver observer);

    void notifyObservers(Document doc);
}
