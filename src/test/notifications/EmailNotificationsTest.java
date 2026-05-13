package notifications;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.UserPreference;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.notification.impl.BrevoEmailNotification;

public class EmailNotificationsTest {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationsTest.class);

    @Test
    @DisplayName("Test dịch vụ gửi email thông báo")
    public void testEmailNotification() {
        Document doc = new Document("DOC-001", "Võ Văn Sáng", "sangvo2004ag@gmail.com", "0900000000", "Tran Van B", "",
                "0911111111", DocumentTypes.DON_XIN_PHEP, "./docs/sample.txt", DocumentExtension.TXT, 12, "sig",
                "noi dung", "hash", DocumentStatus.DANG_XET_DUYET, new UserPreference(true, false, false));

        String output = captureOutput(() -> new BrevoEmailNotification().update(doc));
        logger.info("Captured output: " + output);
        assertTrue(output.contains("[BREVO] Gui email thanh cong."));
    }

    @Test
    @DisplayName("Không gửi email khi người dùng tắt nhận email")
    public void testEmailNotificationDisabled() {
        Document doc = new Document("DOC-002");
        doc.userPreference = new UserPreference(false, true, false);

        String output = captureOutput(() -> new BrevoEmailNotification().update(doc));

        assertTrue(output.isEmpty());
    }

    private static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        System.setOut(capture);
        try {
            action.run();
            capture.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
        }
    }
}
