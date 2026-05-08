package vn.edu.tdtu.edocument.document.validation;

public class FileValidationContext {
    // thông tin người nộp
    public String applicantName;
    public String applicantEmail;
    public String applicantPhone;

    // Thông tin hồ sơ
    public String documentType;
    public String filePath;
    public String fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;

    // Thông tin cán bộ tiếp nhận (nếu đã có)
    // public String officerName;
    // public String officerEmail;
    // public String officerPhone;

    public FileValidationContext(String applicantName, String applicantEmail, String applicantPhone) {
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
    }

    public void setFileInfo(String documentType, String filePath, String fileExtension, long fileSizeKB, String digitalSignature, String extractedContent) {
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSizeKB = fileSizeKB;
        this.digitalSignature = digitalSignature;
        this.extractedContent = extractedContent;
    }
    public void resetFileInfo() {
        this.documentType = null;
        this.filePath = null;
        this.fileExtension = null;
        this.fileSizeKB = 0;
        this.digitalSignature = null;
        this.extractedContent = null;
    }
}
