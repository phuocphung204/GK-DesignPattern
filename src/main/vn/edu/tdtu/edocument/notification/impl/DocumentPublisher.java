package vn.edu.tdtu.edocument.notification.impl;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import vn.edu.tdtu.edocument.notification.core.Subject;

import java.util.ArrayList;
import java.util.List;

public class DocumentPublisher implements Subject {
    private final List<NotificationObserver> observers = new ArrayList<>();

    @Override
    public void attach(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Document doc) {
        for (NotificationObserver observer : observers) {
            observer.update(doc);
        }
    }
}
