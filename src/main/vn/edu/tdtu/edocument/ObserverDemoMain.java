package vn.edu.tdtu.edocument;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.UserPreference;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.notification.impl.AppPushNotification;
import vn.edu.tdtu.edocument.notification.impl.DocumentPublisher;
import vn.edu.tdtu.edocument.notification.impl.BrevoEmailNotification;
import vn.edu.tdtu.edocument.notification.impl.SMSNotification;
import vn.edu.tdtu.edocument.service.DocumentProcessor;

public class ObserverDemoMain {
    public static void main(String[] args) {
        DocumentPublisher publisher = new DocumentPublisher();
        publisher.attach(new BrevoEmailNotification());
        publisher.attach(new SMSNotification());
        publisher.attach(new AppPushNotification());

        DocumentProcessor processor = new DocumentProcessor(publisher);

        UserPreference preference = new UserPreference(true, false, true);
        Document doc = new Document(
                "A1B2C3D4",
                "Nguyen Van A",
                "nguyenvana@example.com",
                "0909000000",
                "Can bo B",
                "canbob@tdtu.edu.vn",
                "0911000000",
                DocumentTypes.DON_XIN_PHEP,
                "sample.txt",
                DocumentExtension.TXT,
                120,
                "SIG-001",
                "Noi dung mau",
                null,
                DocumentStatus.KHONG_XAC_DINH,
                preference);

        processor.process(doc);

        if (DocumentStatus.DANG_XET_DUYET.equals(doc.status)) {
            publisher.notifyObservers(doc);
        }
    }
}
