package vn.edu.tdtu.edocument.document.model;

import java.util.List;
import java.util.UUID;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.enums.NotificationChannelType;

public class Document {
    public UUID id;
    public String applicantName;
    public String applicantEmail;
    public String applicantPhone;
    public String officerName;
    public String officerEmail;
    public String officerPhone;
    public DocumentTypes documentType;
    public String filePath;
    public String fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;
    public String extractedContentHash;
    public DocumentStatus status;
    public List<NotificationChannelType> applicantPreference;
    public List<NotificationChannelType> officerPreference;

    public Document(UUID id,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            String officerName,
            String officerEmail,
            String officerPhone,
            DocumentTypes documentType,
            String filePath,
            String fileExtension,
            long fileSizeKB,
            String digitalSignature,
            String extractedContent,
            String extractedContentHash,
            DocumentStatus status,
            List<NotificationChannelType> applicantPreference,
            List<NotificationChannelType> officerPreference) {
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
        this.applicantPreference = applicantPreference;
        this.officerPreference = officerPreference;
    }

    public Document(UUID id,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            String officerName,
            String officerEmail,
            String officerPhone,
            DocumentTypes documentType,
            String filePath,
            String fileExtension,
            long fileSizeKB,
            String digitalSignature,
            String extractedContent,
            DocumentStatus status) {
        this(id, applicantName, applicantEmail, applicantPhone, officerName, officerEmail, officerPhone, documentType,
                filePath, fileExtension, fileSizeKB, digitalSignature, extractedContent, null, status,
                NotificationChannelType.defaultPreference(), NotificationChannelType.defaultPreference());
    }

    public Document(UUID id) {
        this.id = id;
        this.status = DocumentStatus.KHONG_XAC_DINH;
        this.applicantPreference = NotificationChannelType.defaultPreference();
        this.officerPreference = NotificationChannelType.defaultPreference();
    }

    @Override
    public String toString() {
        return String.format("Hồ sơ [%s] - Nộp bởi: %s - Trạng thái: %s", id, applicantName, status);
    }
}