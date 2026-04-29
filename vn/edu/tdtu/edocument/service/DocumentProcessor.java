package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.RepositoryPattern.local_storage.JsonStorage;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DocumentProcessor {
    private IRepository _repository;
    public DocumentProcessor(IRepository repository) {
        _repository = repository;
    }

    public void process(Document doc) {
        System.out.println("\n=======================================================");
        System.out.println("BẮT ĐẦU XỬ LÝ HỒ SƠ ID: " + doc.id);

        if (doc.id == null || doc.id.isEmpty() ||
            doc.applicantName == null || doc.applicantName.isEmpty() ||
            doc.applicantEmail == null || doc.applicantEmail.isEmpty() ||
            doc.applicantPhone == null || doc.applicantPhone.isEmpty() ||
            doc.officerName == null || doc.officerName.isEmpty() ||
            doc.officerEmail == null || doc.officerEmail.isEmpty() ||
            doc.officerPhone == null || doc.officerPhone.isEmpty() ||
            doc.documentType == null || doc.documentType == DocumentTypes.CHUA_XAC_DINH ||
            doc.filePath == null || doc.filePath.isEmpty() ||
            doc.fileExtension == null ||
            doc.digitalSignature == null || doc.digitalSignature.isEmpty()) {
            
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin bắt buộc. Hủy tạo hồ sơ.");
            return;
        }

        doc.status = DocumentStatus.DA_NOP;
        sendNotifications(doc);

        System.out.println("[KIỂM DUYỆT] Đang kiểm tra dung lượng và định dạng...");
        if (doc.fileSizeKB > 5120) {
            System.out.println("[TỪ CHỐI] Dung lượng file " + doc.fileSizeKB + "KB vượt quá 5MB.");
            doc.status = DocumentStatus.TU_CHOI;
            sendNotifications(doc);
            return;
        }

        if (!doc.fileExtension.equals(DocumentExtension.TXT)) {
            System.out.println("[TỪ CHỐI] Định dạng " + doc.fileExtension + " không được hỗ trợ ở v1.0.");
            doc.status = DocumentStatus.TU_CHOI;
            sendNotifications(doc);
            return;
        }

        System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
        try {
            String content = new String(Files.readAllBytes(Paths.get(doc.filePath)));
            doc.extractedContent = content;
        } catch (IOException e) {
            System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
            doc.status = DocumentStatus.TU_CHOI;
            sendNotifications(doc);
            return;
        }

        System.out.println("[LƯU TRỮ] Đang sao chép file và xuất dữ liệu JSON...");
        saveToStorage(doc);

        System.out.println("[HOÀN TẤT] Hồ sơ hợp lệ và đã được lưu trữ thành công.");
        doc.status = DocumentStatus.DANG_XU_LY;
        sendNotifications(doc);
    }

    public void proccessInsertPersonalInfo(Document doc) {
        // Xử lý thông tin cá nhân nếu cần thiết
    }

    public void proccessInsertDocumentFile(Document doc) {
        // Xử lý tệp đính kèm nếu cần thiết
    }

    public void proccessInsertSubmissionInfo(Document doc) {
        // Xử lý thông tin nộp hồ sơ nếu cần thiết
    }

    private void saveToStorage(Document doc) {
        _repository.CreateDocument(doc); // Sử dụng lớp JsonStorage để lưu trữ vật lý và dữ liệu JSON
    }

    private void sendNotifications(Document doc) {
        System.out.println("  [GỬI EMAIL] -> Người nộp (" + doc.applicantEmail + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI EMAIL] -> Cán bộ xử lý (" + doc.officerEmail + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
    }
}