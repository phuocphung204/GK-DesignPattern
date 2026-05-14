package vn.edu.tdtu.edocument.document.validation;

import java.util.UUID;

public class FileValidationContext {
    public UUID documentId;
    // Thông tin tài liệu
    public String documentType;
    public String filePath;
    public String fileExtension;
    public long fileSizeKB;
    public String digitalSignature;
    public String extractedContent;

    private FileValidationContext(UUID documentId, String documentType, String filePath, String fileExtension,
            long fileSizeKB, String digitalSignature, String extractedContent) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSizeKB = fileSizeKB;
        this.digitalSignature = digitalSignature;
        this.extractedContent = extractedContent;
    }

    public static FileValidationContext create(UUID documentId, String documentType, String filePath,
            String fileExtension, long fileSizeKB, String digitalSignature, String extractedContent) {
        return new FileValidationContext(documentId, documentType, filePath, fileExtension, fileSizeKB,
                digitalSignature, extractedContent);
    }
}
