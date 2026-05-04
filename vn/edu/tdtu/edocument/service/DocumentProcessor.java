package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.ChainOfResponsibilityPattern.*;
import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.enums.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DocumentProcessor {
    private IRepository _repository;

    public DocumentProcessor(IRepository repository) {
        _repository = repository;
    }
    // Các phương thức xử lý từng bước của quy trình tiếp nhận hồ sơ
    // B1: Nhập thông tin cá nhân
    // B2: Nhập tệp đính kèm
    // B3: Nhập thông tin nộp hồ sơ
    // Mỗi bước sẽ có một phương thức riêng để xử lý, có thể gọi tuần tự hoặc độc lập tùy theo luồng xử lý của ứng dụng
    
    public boolean proccessInsertPersonalInfo(Document doc) {
        // Xử lý thông tin cá nhân nếu cần thiết
        if (doc.applicantName.isEmpty() || doc.applicantEmail.isEmpty() || doc.applicantPhone.isEmpty()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cá nhân. Hủy tạo hồ sơ.");
            return false;
        }
        // Cần nâng cấp lên State pattern để quản lý trạng thái hồ sơ tốt hơn, tránh việc set status ở nhiều nơi như thế này
        if (doc.status == null) {
            doc.status = DocumentStatus.BAN_NHAP;
        }
        saveToStorage(doc); // Lưu tạm hồ sơ sau khi nhập thông tin cá nhân, có thể là bản nháp
        return true;
    }

    public boolean proccessInsertDocumentFile(Document doc) {
        // Xử lý tệp đính kèm nếu cần thiết
        if (doc.filePath == null || doc.filePath.isEmpty()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu tệp đính kèm. Hủy tạo hồ sơ.");
            return false;
        }

        if (doc.extractedContent == null || doc.extractedContent.isBlank()) {
            System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
            //TODO: Trích xuất file json trên local, Khi nào xong yc2 thì đổi lại
            try {
                String content = new String(Files.readAllBytes(Paths.get(doc.filePath)));
                doc.extractedContent = content;
            } catch (IOException e) {
                System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
                return false;
            }
            // Tính mã băm của nội dung đã trích xuất để kiểm tra trùng lặp
            doc.extractedContentHash = Hash.encryptThisString(doc.extractedContent);
        }

        // Tạo context với thông tin file để truyền vào chuỗi kiểm tra
        FileValidationContext context = FileValidationContext.create(
            doc.documentType.toString(),
            doc.filePath,
            doc.fileExtension.toString(),
            doc.fileSizeKB,
            doc.digitalSignature,
            doc.extractedContent
        );

        // Tạo chuỗi kiểm tra (giữ lại step đầu làm "head" của chain)
        IDocumentValidationStep fileValidation = new BasicFileValidationStep(_repository);
        fileValidation
            .setNext(new VirusScanValidationStep(_repository))
            .setNext(new DuplicateContentValidationStep(_repository));
        // Thực hiện chuỗi kiểm tra
        boolean validationResult = fileValidation.handleValidation(context);

        if (!validationResult) {
            return false;
        }
        if (doc.status == DocumentStatus.BAN_NHAP) {
            doc.status = DocumentStatus.DA_TAI_FILE;
        }
        saveToStorage(doc); // Lưu tạm hồ sơ sau khi nhập tệp đính kèm, có thể là bản nháp
        return true;
    }

    public void proccessInsertSubmissionInfo(Document doc) {
        // Xử lý thông tin nộp hồ sơ nếu cần thiết
        if (doc.officerName.isEmpty() || doc.officerEmail.isEmpty() || doc.officerPhone.isEmpty()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cán bộ xử lý. Hủy tạo hồ sơ.");
            return;
        }
        if (doc.status == DocumentStatus.DA_TAI_FILE) {
            doc.status = DocumentStatus.DA_TIEP_NHAN;
        }
        sendNotifications(doc);
        saveToStorage(doc); // Lưu hồ sơ sau khi nhập đầy đủ thông tin, có thể là bản nháp hoặc chính thức tùy theo logic của ứng dụng
    }

    private void saveToStorage(Document doc) {
        _repository.CreateOrUpdateDocument(doc); 
        // Sử dụng repository để lưu trữ hồ sơ, có thể là lưu vào file JSON hoặc cơ sở dữ liệu tùy theo implementation của repository
        // Nếu Document đã tồn tại (lưu draft trước đó), repository sẽ cập nhật lại thông tin, nếu chưa tồn tại sẽ tạo mới
    }

    private void sendNotifications(Document doc) {
        // Gửi thông báo qua email và SMS cho người nộp và cán bộ xử lý
        //TODO: sửa lại khi xong yc4, hiện tại chỉ in ra console để mô phỏng như demo ban đầu
        System.out.println("  [GỬI EMAIL] -> Người nộp (" + doc.applicantEmail + "): Hồ sơ đã được tiếp nhận và đang chờ xử lý.");
        System.out.println("  [GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ đã được tiếp nhận và đang chờ xử lý.");
        System.out.println("  [GỬI EMAIL] -> Cán bộ xử lý (" + doc.officerEmail + "): Bạn có hồ sơ mới cần xử lý.");
        System.out.println("  [GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Bạn có hồ sơ mới cần xử lý.");
    }
}