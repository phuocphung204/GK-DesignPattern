package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

public class FileValidationContext {

    // Thông tin hồ sơ
    public String documentType;
    public String filePath;
    public String fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;

    private FileValidationContext(String documentType, String filePath, String fileExtension, long fileSizeKB, String digitalSignature, String extractedContent) {
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSizeKB = fileSizeKB;
        this.digitalSignature = digitalSignature;
        this.extractedContent = extractedContent;
    }

    public static FileValidationContext create(String documentType, String filePath, String fileExtension, long fileSizeKB, String digitalSignature, String extractedContent) {
        return new FileValidationContext(documentType, filePath, fileExtension, fileSizeKB, digitalSignature, extractedContent);
    }
}
