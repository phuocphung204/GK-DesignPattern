package document.requirement4;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import vn.edu.tdtu.edocument.notification.core.Subject;
import vn.edu.tdtu.edocument.notification.core.UserProfile;
import vn.edu.tdtu.edocument.notification.impl.AppPushNotification;
import vn.edu.tdtu.edocument.notification.impl.BrevoEmailNotification;
import vn.edu.tdtu.edocument.notification.impl.SMSNotification;

public class NotificationObserverTest {

    @Test
    @DisplayName("attach adds observer to list")
    void attachAddsObserverToList() {
        TestSubject subject = new TestSubject();
        NotificationObserver observer = new AppPushNotification();

        subject.addObserver(observer);

        assertEquals(1, subject.getObservers().size());
        assertSame(observer, subject.getObservers().get(0));
    }

    @Test
    @DisplayName("detach removes observer from list")
    void detachRemovesObserverFromList() {
        TestSubject subject = new TestSubject();
        NotificationObserver observer = new SMSNotification();

        subject.addObserver(observer);
        subject.removeObserver(observer);

        assertEquals(0, subject.getObservers().size());
    }

    @Test
    @DisplayName("notifyObservers calls all observers with document and profile")
    void notifyObserversCallsAllObservers() {
        TestSubject subject = new TestSubject();
        subject.addObserver(new AppPushNotification());
        subject.addObserver(new SMSNotification());
        subject.addObserver(new BrevoEmailNotification());

        Document doc = new Document(UUID.randomUUID());
        doc.applicantName = "Test User";
        doc.applicantEmail = "sangvo2004ag@gmail.com";
        doc.applicantPhone = "0123456789";

        assertDoesNotThrow(() -> subject.notifyObservers(doc));
    }

    private static final class TestSubject implements Subject {
        private final List<NotificationObserver> observers = new ArrayList<>();

        @Override
        public void attach(List<NotificationObserver> observers, NotificationObserver observer) {
            observers.add(observer);
        }

        @Override
        public void detach(List<NotificationObserver> observers, NotificationObserver observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers(Document doc) {
            UserProfile profile = new UserProfile(doc.applicantName,
                    doc.applicantEmail,
                    doc.applicantPhone,
                    "applicant");
            for (NotificationObserver observer : observers) {
                observer.update(profile, doc);
            }
        }

        private void addObserver(NotificationObserver observer) {
            attach(observers, observer);
        }

        private void removeObserver(NotificationObserver observer) {
            detach(observers, observer);
        }

        private List<NotificationObserver> getObservers() {
            return observers;
        }
    }

}
