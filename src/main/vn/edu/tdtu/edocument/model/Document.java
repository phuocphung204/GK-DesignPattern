package vn.edu.tdtu.edocument.model;

import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.model.enums.DocumentTypes;

public class Document {
    public String id;
    public String applicantName;
    public String applicantEmail;
    public String applicantPhone;
    public String officerName;
    public String officerEmail;
    public String officerPhone;
    public DocumentTypes documentType;
    public String filePath;
    public DocumentExtension fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;
    public String extractedContentHash;
    public DocumentStatus status;
    public UserPreference userPreference;

    public Document(String id, String applicantName, String applicantEmail, String applicantPhone,
            String officerName, String officerEmail, String officerPhone,
            DocumentTypes documentType, String filePath, DocumentExtension fileExtension,
            long fileSizeKB, String digitalSignature, String extractedContent, String extractedContentHash,
            DocumentStatus status, UserPreference userPreference) {
        this.id = id;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.officerName = officerName;
        this.officerEmail = officerEmail;
        this.officerPhone = officerPhone;
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSizeKB = fileSizeKB;
        this.digitalSignature = digitalSignature;
        this.extractedContent = extractedContent;
        this.extractedContentHash = extractedContentHash;
        this.status = status;
        this.userPreference = userPreference;
    }

    public Document(String id, String applicantName, String applicantEmail, String applicantPhone,
            String officerName, String officerEmail, String officerPhone,
            DocumentTypes documentType, String filePath, DocumentExtension fileExtension,
            long fileSizeKB, String digitalSignature, String extractedContent, DocumentStatus status) {
        this(id, applicantName, applicantEmail, applicantPhone, officerName, officerEmail, officerPhone,
                documentType, filePath, fileExtension, fileSizeKB, digitalSignature, extractedContent, null,
                status, UserPreference.defaultPreference());
    }

    public Document(String id) {
        this.id = id;
        this.status = DocumentStatus.KHONG_XAC_DINH;
        this.userPreference = UserPreference.defaultPreference();
    }

    @Override
    public String toString() {
        return String.format("Hồ sơ [%s] - Nộp bởi: %s - Trạng thái: %s", id, applicantName, status);
    }
}