package src.test.document.requirement3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.repository.RepositoryFactory;
import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.service.DocumentProcessor;
import vn.edu.tdtu.edocument.document.builder.IDocumentBuilder;
import vn.edu.tdtu.edocument.document.builder.DocumentBuilder;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.Document;

public class DuplicationHandlerTest {
    private IRepository _repository = RepositoryFactory.createRepository(RepositoryType.MONGODB);
    private DocumentProcessor _processor = new DocumentProcessor(_repository);

    private IDocumentBuilder documentBuilder;
    private String applicantName = "Nguyen Van D";
    private String applicantEmail = "nguyenvand@example.com";
    private String applicantPhone = "0123456789";
    private String officerName = "Tran Van D";
    private String officerEmail = "tranvand@example.com";
    private String officerPhone = "0987654321";
    private DocumentTypes documentType = DocumentTypes.BAO_CAO;
    private String filePath = "support_test/note.txt";
    private String fileExtension = "TXT";
    private long fileSizeKB = 5;
    private String digitalSignature = "test-signature";

    @Test
    @DisplayName("Kiểm tra dừng pipeline khi phát hiện trùng lặp trong tệp đính kèm")
    void shouldStopWhenDuplicationDetected() {
        // Arrange: Tạo một tài liệu mẫu với thông tin cá nhân hợp lệ nhưng tệp đính kèm chứa trùng lặp
        documentBuilder = new DocumentBuilder();
        documentBuilder.SetPersonalInfo(
            applicantName,
            applicantEmail,
            applicantPhone);
        Document draft1 = documentBuilder.Build();
        boolean result1 = _processor.proccessInsertPersonalInfo(draft1);

        // Step 2: Nhập tệp đính kèm chứa trùng lặp
        documentBuilder.SetFileInfo(
            documentType,
            filePath,
            fileExtension,
            fileSizeKB,
            digitalSignature);
        Document draft2 = documentBuilder.Build();
        boolean result2 = _processor.proccessInsertDocumentFile(draft2);

        // Assert: Bước kiểm tra tệp đính kèm phải trả về false và pipeline sẽ dừng lại, không thực hiện bước tiếp theo
        assertTrue(result1, "Kiểm tra thông tin cá nhân phải thành công");
        assertFalse(result2, "Kiểm tra tệp đính kèm phải thất bại do phát hiện trùng lặp");
    }
}
