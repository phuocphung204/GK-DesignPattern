package document.requirement1;

import static org.junit.jupiter.api.Assertions.*;

import javax.print.Doc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.DisplayName;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.builder.DocumentBuilder;
import vn.edu.tdtu.edocument.document.builder.IDocumentBuilder;
import vn.edu.tdtu.edocument.service.DocumentProcessor;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;

import java.util.List;
import java.util.UUID;

public class DocumentBuilderTest {

    private IDocumentBuilder documentBuilder;
    private String applicantName = "Nguyen Van A";
    private String applicantEmail = "nguyenvana@example.com";
    private String applicantPhone = "0123456789";
    private String officerName = "Tran Van B";
    private String officerEmail = "tranvanb@example.com";
    private String officerPhone = "0987654321";
    private DocumentTypes documentType = DocumentTypes.BAO_CAO;
    private String filePath = "support_test/bando.jpg";
    private String fileExtension = "JPG";
    private long fileSizeKB = 45;
    private String digitalSignature = "test-signature";

    private Document FetchLatestDraft(Document oldDraft) {
        // Giả lập việc tải lại bản nháp từ bản nháp cũ
        Document newDraft = new Document(oldDraft.id);
        newDraft.applicantName = oldDraft.applicantName;
        newDraft.applicantEmail = oldDraft.applicantEmail;
        newDraft.applicantPhone = oldDraft.applicantPhone;
        newDraft.documentType = oldDraft.documentType;
        newDraft.filePath = oldDraft.filePath;
        newDraft.fileExtension = oldDraft.fileExtension;
        newDraft.fileSizeKB = oldDraft.fileSizeKB;
        newDraft.digitalSignature = oldDraft.digitalSignature;

        // Thông tin người tiếp nhận sẽ bị mất do tải lại từ bản nháp cũ chưa có thông
        // tin người tiếp nhận
        return newDraft;
    }

    @Test
    @DisplayName("Kiểm tra tạo bản nháp tài liệu từng bước")
    void shouldCreateDraftStepByStep() {
        documentBuilder = new DocumentBuilder();
        // Step 1: Ban đầu, bản nháp mới chỉ có thông tin người nộp, các trường khác sẽ
        // được điền dần qua các bước
        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone,
                NotificationChannelType.defaultPreference());
        Document draft_1 = documentBuilder.Build();
        assertEquals(applicantName, draft_1.applicantName);
        assertEquals(applicantEmail, draft_1.applicantEmail);
        assertEquals(applicantPhone, draft_1.applicantPhone);

        // Step 2: Sau khi điền thông tin file, bản nháp sẽ có đầy đủ thông tin cần
        // thiết để nộp, nhưng vẫn chưa hoàn chỉnh vì chưa có thông tin người tiếp nhận
        documentBuilder.SetFileInfo(documentType, filePath, fileExtension, fileSizeKB, digitalSignature);
        Document draft_2 = documentBuilder.Build();
        assertEquals(documentType, draft_2.documentType);
        assertEquals(filePath, draft_2.filePath);
        assertEquals(fileExtension, draft_2.fileExtension);
        assertEquals(fileSizeKB, draft_2.fileSizeKB);
        assertEquals(digitalSignature, draft_2.digitalSignature);

        // Step 3: Cuối cùng, sau khi điền thông tin người tiếp nhận, bản nháp sẽ hoàn
        // chỉnh và có thể được nộp
        documentBuilder.SetSubmissionInfo(officerName, officerEmail, officerPhone,
                NotificationChannelType.defaultPreference());
        Document draft_3 = documentBuilder.Build();
        assertEquals(officerName, draft_3.officerName);
        assertEquals(officerEmail, draft_3.officerEmail);
        assertEquals(officerPhone, draft_3.officerPhone);
    }

    @Test
    @DisplayName("Kiểm tra giữ bản nháp trước khi nộp")
    void shouldKeepDraftBeforeSubmit() {

        // Arrange
        documentBuilder = new DocumentBuilder();

        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone,
                NotificationChannelType.defaultPreference());

        documentBuilder.SetFileInfo(documentType, filePath, fileExtension, fileSizeKB, digitalSignature);

        // Giả lập save draft
        Document savedDraft = documentBuilder.Build();

        // Giả lập load lại draft
        Document loadedDraft = FetchLatestDraft(savedDraft);

        // Tạo builder mới từ draft cũ
        documentBuilder = new DocumentBuilder(loadedDraft);

        Document restoredDraft = documentBuilder.Build();

        // Assert dữ liệu vẫn còn
        assertEquals(applicantName, restoredDraft.applicantName);
        assertEquals(applicantEmail, restoredDraft.applicantEmail);
        assertEquals(applicantPhone, restoredDraft.applicantPhone);

        assertEquals(documentType, restoredDraft.documentType);
        assertEquals(filePath, restoredDraft.filePath);
        assertEquals(fileExtension, restoredDraft.fileExtension);
        assertEquals(fileSizeKB, restoredDraft.fileSizeKB);
        assertEquals(digitalSignature, restoredDraft.digitalSignature);

        // Chưa submit nên chưa có officer info
        assertNull(restoredDraft.officerName);
        assertNull(restoredDraft.officerEmail);
        assertNull(restoredDraft.officerPhone);
    }

    @Test
    @DisplayName("shouldPreservePreviousInformationWhenAddingNewStep")
    void shouldPreservePreviousInformationWhenAddingNewStep() {
        documentBuilder = new DocumentBuilder();

        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone,
                NotificationChannelType.defaultPreference());
        Document step1 = documentBuilder.Build();
        assertEquals(applicantName, step1.applicantName);
        assertEquals(applicantEmail, step1.applicantEmail);
        assertEquals(applicantPhone, step1.applicantPhone);

        documentBuilder.SetFileInfo(documentType, filePath, fileExtension, fileSizeKB, digitalSignature);
        Document step2 = documentBuilder.Build();

        // Personal info should remain intact after setting file info.
        assertEquals(applicantName, step2.applicantName);
        assertEquals(applicantEmail, step2.applicantEmail);
        assertEquals(applicantPhone, step2.applicantPhone);

        documentBuilder.SetSubmissionInfo(officerName, officerEmail, officerPhone,
                NotificationChannelType.defaultPreference());
        Document step3 = documentBuilder.Build();

        // File info + personal info should remain intact after setting submission info.
        assertEquals(applicantName, step3.applicantName);
        assertEquals(applicantEmail, step3.applicantEmail);
        assertEquals(applicantPhone, step3.applicantPhone);
        assertEquals(documentType, step3.documentType);
        assertEquals(filePath, step3.filePath);
        assertEquals(fileExtension, step3.fileExtension);
        assertEquals(fileSizeKB, step3.fileSizeKB);
        assertEquals(digitalSignature, step3.digitalSignature);
    }

    @Test
    @DisplayName("shouldAllowPartialDocumentCreation")
    void shouldAllowPartialDocumentCreation() {
        documentBuilder = new DocumentBuilder();

        // Only personal info is filled.
        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone,
                NotificationChannelType.defaultPreference());
        Document partial = documentBuilder.Build();

        assertNotNull(partial);
        assertNotNull(partial.id);

        // Fields of later steps are still allowed to be null.
        assertNull(partial.officerName);
        assertNull(partial.officerEmail);
        assertNull(partial.officerPhone);
        assertNull(partial.filePath);
        assertNull(partial.fileExtension);
    }

    @Test
    @DisplayName("shouldRejectSubmissionWhenRequiredFieldsMissing")
    void shouldRejectSubmissionWhenRequiredFieldsMissing() {
        IRepository noopRepo = new IRepository() {
            @Override
            public boolean ExistsByHash(String hash) {
                return false;
            }

            @Override
            public Document GetDocumentById(UUID id) {
                return null;
            }

            @Override
            public Document GetLatestDraft() {
                return null;
            }

            @Override
            public List<Document> GetAllDocuments() {
                return List.of();
            }

            @Override
            public void CreateDocument(Document doc) {
                // no-op
            }

            @Override
            public void UpdateDocument(Document doc) {
                // no-op
            }

            @Override
            public void DeleteDocument(UUID id) {
                // no-op
            }
        };

        DocumentProcessor processor = new DocumentProcessor(noopRepo);

        documentBuilder = new DocumentBuilder();
        documentBuilder.SetPersonalInfo(applicantName, applicantEmail, applicantPhone,
                NotificationChannelType.defaultPreference());
        documentBuilder.SetFileInfo(documentType, filePath, fileExtension, fileSizeKB, digitalSignature);
        Document draft = documentBuilder.Build();

        // Missing required submission fields (must be empty string rather than null to
        // avoid NPE in current logic).
        draft.officerName = "";
        draft.officerEmail = "";
        draft.officerPhone = "";

        boolean ok = processor.processInsertSubmissionInfo(draft);
        assertFalse(ok, "Submission should be rejected when officer info is missing");
    }
}
