package vn.edu.tdtu.edocument.model;

import vn.edu.tdtu.edocument.model.enums.*;

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
    public DocumentStatus status;

    public Document(String id, String applicantName, String applicantEmail, String applicantPhone,
                    String officerName, String officerEmail, String officerPhone,
                    DocumentTypes documentType, String filePath, DocumentExtension fileExtension,
                    long fileSizeKB, String digitalSignature, String extractedContent, DocumentStatus status) {
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
        this.status = status;
    }

    public Document() {
        // Default constructor
    }

    @Override
    public String toString() {
        return String.format("Hồ sơ [%s] - Nộp bởi: %s - Trạng thái: %s", id, applicantName, status);
    }
}