package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.BasicDocumentValidationStep;
import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.BasicFileValidationStep;
import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.DuplicateContentValidationStep;
import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.FileValidationContext;
import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.IDocumentValidationStep;
import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.VirusScanValidationStep;
import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.notification.impl.AppPushNotification;
import vn.edu.tdtu.edocument.notification.impl.DocumentPublisher;
import vn.edu.tdtu.edocument.notification.impl.BrevoEmailNotification;
import vn.edu.tdtu.edocument.notification.impl.SMSNotification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DocumentProcessor {
    private final DocumentPublisher publisher;
    private final IRepository repository;

    private IDocumentValidationStep fileValidationChain;
    private FileValidationContext context;

    public DocumentProcessor() {
        this(null, null);
    }

    public DocumentProcessor(DocumentPublisher publisher) {
        this(publisher, null);
    }

    public DocumentProcessor(IRepository repository) {
        this(null, repository);
    }

    private DocumentProcessor(DocumentPublisher publisher, IRepository repository) {
        this.publisher = (publisher == null) ? defaultPublisher() : publisher;
        this.repository = repository;
        if (repository != null) {
            this.fileValidationChain = new BasicDocumentValidationStep(repository);
        }
    }

    private static DocumentPublisher defaultPublisher() {
        DocumentPublisher publisher = new DocumentPublisher();
        publisher.attach(new BrevoEmailNotification());
        publisher.attach(new SMSNotification());
        publisher.attach(new AppPushNotification());
        return publisher;
    }

    public void process(Document doc) {
        System.out.println("\n=======================================================");
        System.out.println("BẮT ĐẦU XỬ LÝ HỒ SƠ ID: " + doc.id);

        if (doc == null || isBlank(doc.id) || isBlank(doc.applicantName) || isBlank(doc.applicantEmail)
                || isBlank(doc.applicantPhone) || isBlank(doc.officerName) || isBlank(doc.officerEmail)
                || isBlank(doc.officerPhone) || doc.documentType == null || isBlank(doc.filePath)
                || doc.fileExtension == null || isBlank(doc.digitalSignature)) {

            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin bắt buộc. Hủy tạo hồ sơ.");
            return;
        }

        doc.status = DocumentStatus.DA_TIEP_NHAN;
        notifyStatusChanged(doc);

        System.out.println("[KIỂM DUYỆT] Đang kiểm tra dung lượng và định dạng...");
        if (doc.fileSizeKB > 5120) {
            System.out.println("[TỪ CHỐI] Dung lượng file " + doc.fileSizeKB + "KB vượt quá 5MB.");
            doc.status = DocumentStatus.TU_CHOI;
            notifyStatusChanged(doc);
            return;
        }

        if (doc.fileExtension != DocumentExtension.TXT) {
            System.out.println("[TỪ CHỐI] Định dạng " + doc.fileExtension + " không được hỗ trợ ở v1.0.");
            doc.status = DocumentStatus.TU_CHOI;
            notifyStatusChanged(doc);
            return;
        }

        System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
        try {
            String content = new String(Files.readAllBytes(Paths.get(doc.filePath)));
            doc.extractedContent = content;
        } catch (IOException e) {
            System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
            doc.status = DocumentStatus.TU_CHOI;
            notifyStatusChanged(doc);
            return;
        }

        if (doc.extractedContent != null && !doc.extractedContent.isBlank()) {
            doc.extractedContentHash = Hash.encryptThisString(doc.extractedContent);
        }

        if (repository != null) {
            System.out.println("[LƯU TRỮ] Đang lưu dữ liệu qua repository...");
            repository.CreateOrUpdateDocument(doc);
        }

        System.out.println("[HOÀN TẤT] Hồ sơ hợp lệ và đã được lưu trữ thành công.");
        doc.status = DocumentStatus.DANG_XET_DUYET;
        notifyStatusChanged(doc);
    }

    public boolean proccessInsertPersonalInfo(Document doc) {
        if (repository == null) {
            System.out.println("[LỖI HỆ THỐNG] Repository chưa được cấu hình.");
            return false;
        }

        if (doc == null || isBlank(doc.applicantName) || isBlank(doc.applicantEmail) || isBlank(doc.applicantPhone)) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cá nhân. Hủy tạo hồ sơ.");
            return false;
        }

        if (doc.status == DocumentStatus.BAN_NHAP) {
            resetChain();
        }

        context = new FileValidationContext(doc.applicantName, doc.applicantEmail, doc.applicantPhone);
        if (fileValidationChain == null) {
            fileValidationChain = new BasicDocumentValidationStep(repository);
        }

        boolean validationResult = fileValidationChain.handleValidation(context);
        if (!validationResult) {
            return false;
        }

        if (doc.status == DocumentStatus.KHONG_XAC_DINH) {
            doc.status = DocumentStatus.BAN_NHAP;
            notifyStatusChanged(doc);
        }
        repository.CreateOrUpdateDocument(doc);
        return true;
    }

    public boolean proccessInsertDocumentFile(Document doc) {
        if (repository == null) {
            System.out.println("[LỖI HỆ THỐNG] Repository chưa được cấu hình.");
            return false;
        }
        if (doc == null || isBlank(doc.filePath)) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu tệp đính kèm. Hủy tạo hồ sơ.");
            return false;
        }
        if (context == null) {
            context = new FileValidationContext(doc.applicantName, doc.applicantEmail, doc.applicantPhone);
        }

        if (doc.extractedContent == null || doc.extractedContent.isBlank()) {
            System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
            try {
                String content = new String(Files.readAllBytes(Paths.get(doc.filePath)));
                doc.extractedContent = content;
            } catch (IOException e) {
                System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
                return false;
            }
            doc.extractedContentHash = Hash.encryptThisString(doc.extractedContent);
        }

        context.setFileInfo(
                doc.documentType == null ? null : doc.documentType.name(),
                doc.filePath,
                doc.fileExtension == null ? null : doc.fileExtension.name(),
                doc.fileSizeKB,
                doc.digitalSignature,
                doc.extractedContent);

        if (fileValidationChain == null) {
            fileValidationChain = new BasicDocumentValidationStep(repository);
        }

        fileValidationChain
                .setNext(new BasicFileValidationStep(repository))
                .setNext(new VirusScanValidationStep(repository))
                .setNext(new DuplicateContentValidationStep(repository));

        boolean validationResult = fileValidationChain.handleValidation(context);
        if (!validationResult) {
            return false;
        }

        if (doc.status == DocumentStatus.BAN_NHAP) {
            doc.status = DocumentStatus.DA_TAI_FILE;
            notifyStatusChanged(doc);
        }

        repository.CreateOrUpdateDocument(doc);
        return true;
    }

    public void proccessInsertSubmissionInfo(Document doc) {
        if (repository == null) {
            System.out.println("[LỖI HỆ THỐNG] Repository chưa được cấu hình.");
            return;
        }
        if (doc == null || isBlank(doc.officerName) || isBlank(doc.officerEmail) || isBlank(doc.officerPhone)) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cán bộ xử lý. Hủy tạo hồ sơ.");
            return;
        }
        if (doc.status == DocumentStatus.DA_TAI_FILE) {
            doc.status = DocumentStatus.DA_TIEP_NHAN;
            notifyStatusChanged(doc);
        }
        resetChain();
        repository.CreateOrUpdateDocument(doc);
    }

    private void resetChain() {
        if (repository == null) {
            return;
        }
        fileValidationChain = new BasicDocumentValidationStep(repository);
        if (context != null) {
            context.resetFileInfo();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void notifyStatusChanged(Document doc) {
        publisher.notifyObservers(doc);
    }
}