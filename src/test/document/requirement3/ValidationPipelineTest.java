package document.requirement3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.repository.RepositoryFactory;
import vn.edu.tdtu.edocument.service.DocumentProcessor;
import vn.edu.tdtu.edocument.document.builder.IDocumentBuilder;
import vn.edu.tdtu.edocument.document.builder.DocumentBuilder;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.Document;

public class ValidationPipelineTest {
    private IRepository _repository = RepositoryFactory.createRepository(RepositoryType.MONGODB);
    private DocumentProcessor _processor = new DocumentProcessor(_repository);

    private IDocumentBuilder documentBuilder;
    private String applicantName = "Nguyen Van A";
    private String applicantEmail = "nguyenvana@example.com";
    private String applicantPhone = "0123456789";
    private String officerName = "Tran Van A";
    private String officerEmail = "tranvana@example.com";
    private String officerPhone = "0987654321";
    private DocumentTypes documentType = DocumentTypes.BAO_CAO;
    private String filePath = "support_test/passAll.txt";
    private String fileExtension = "TXT";
    private long fileSizeKB = 1;
    private String digitalSignature = "test-signature";

    @Test
    @DisplayName("Kiểm tra toàn bộ pipeline validation cho một tài liệu")
    void shouldPassAllValidationStations() {
        // Arrange: Tạo một tài liệu mẫu với thông tin đầy đủ và hợp lệ
        documentBuilder = new DocumentBuilder();
        // Step 1: Nhập thông tin cá nhân, sau đó thực hiện kiểm tra thông tin cá nhân
        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone);
        Document draft1 = documentBuilder.Build();
        boolean result1 = _processor.proccessInsertPersonalInfo(draft1);

        // Step 2: Nhập tệp đính kèm và thực hiện chuỗi kiểm tra tệp đính kèm
        documentBuilder.SetFileInfo(documentType, filePath, fileExtension, fileSizeKB, digitalSignature);
        Document draft2 = documentBuilder.Build();
        boolean result2 = _processor.proccessInsertDocumentFile(draft2);

        // Step 3: Nhập thông tin người tiếp nhận và thực hiện kiểm tra
        documentBuilder.SetSubmissionInfo(officerName, officerEmail, officerPhone);
        Document draft3 = documentBuilder.Build();
        boolean result3 = _processor.proccessInsertSubmissionInfo(draft3);

        // Assert: Tất cả các bước kiểm tra đều phải trả về true
        assertTrue(result1, "Kiểm tra thông tin cá nhân phải thành công");
        assertTrue(result2, "Kiểm tra tệp đính kèm phải thành công");
        assertTrue(result3, "Kiểm tra thông tin người tiếp nhận phải thành công");
    }
}
