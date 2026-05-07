package vn.edu.tdtu.edocument.document.model;

public class Document {
    public String id;
    public String applicantName;
    public String applicantEmail;
    public String applicantPhone;
    public String officerName;
    public String officerEmail;
    public String officerPhone;
    public String documentType;
    public String filePath;
    public String fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;
    public String status;
        public vn.edu.tdtu.edocument.model.UserPreference userPreference;

    public Document(String id, String applicantName, String applicantEmail, String applicantPhone,
            String officerName, String officerEmail, String officerPhone,
            String documentType, String filePath, String fileExtension,
            long fileSizeKB, String digitalSignature, String extractedContent, String status) {
        this(id, applicantName, applicantEmail, applicantPhone,
            officerName, officerEmail, officerPhone,
            documentType, filePath, fileExtension,
            fileSizeKB, digitalSignature, extractedContent, status,
            vn.edu.tdtu.edocument.model.UserPreference.defaultPreference());
        }

        public Document(String id, String applicantName, String applicantEmail, String applicantPhone,
            String officerName, String officerEmail, String officerPhone,
            String documentType, String filePath, String fileExtension,
            long fileSizeKB, String digitalSignature, String extractedContent, String status,
            vn.edu.tdtu.edocument.model.UserPreference userPreference) {
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
        this.userPreference = userPreference;
    }

    @Override
    public String toString() {
        return String.format("Hồ sơ [%s] - Nộp bởi: %s - Trạng thái: %s", id, applicantName, status);
    }
}