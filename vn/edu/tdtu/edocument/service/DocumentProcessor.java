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
    private IDocumentValidationStep _fileValidationChain; // Chuỗi kiểm tra tệp đính kèm, sẽ được khởi tạo khi cần thiết
    private FileValidationContext _context;

    public DocumentProcessor(IRepository repository) {
        _repository = repository;
        _fileValidationChain = new BasicDocumentValidationStep(_repository);
    }

    private void resetChain() {
        _fileValidationChain = new BasicDocumentValidationStep(_repository);
        _context.resetFileInfo(); // Reset lại thông tin file trong context để tránh lỗi khi nhập lại thông tin cá nhân sau khi đã nhập tệp đính kèm trước đó
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
        // Nếu đang ở trạng thái "Bản nháp" do nhập lại thông tin cá nhân sau khi đã vào trạng thái khác, reset lại chuỗi kiểm tra để đảm bảo các bước kiểm tra được thực hiện đầy đủ khi nhập lại thông tin cá nhân
        if (doc.status == DocumentStatus.BAN_NHAP) {
            resetChain(); // Nếu đang ở trạng thái "Bản nháp", reset lại chuỗi kiểm tra để đảm bảo các bước kiểm tra được thực hiện đầy đủ khi nhập lại thông tin cá nhân
        }
        _context = new FileValidationContext(doc.applicantName, doc.applicantEmail, doc.applicantPhone); // Tạo context với thông tin người nộp để truyền vào chuỗi kiểm tra sau này
        // Thực hiện chuỗi kiểm tra thông tin cá nhân ngay sau khi nhập, nếu không hợp lệ sẽ trả về false và dừng quy trình tiếp nhận
        boolean validationResult = _fileValidationChain.handleValidation(_context);
        if (!validationResult) {
            return false;
        }
        // Nếu chưa có trạng thái, khởi tạo bản nháp, nếu đã có trạng thái thì giữ nguyên (trường hợp cập nhật thông tin cá nhân sau khi đã tạo hồ sơ)
        if (doc.status == DocumentStatus.KHONG_XAC_DINH) {
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
        _context.setFileInfo(
            doc.documentType.toString(),
            doc.filePath,
            doc.fileExtension.toString(),
            doc.fileSizeKB,
            doc.digitalSignature,
            doc.extractedContent
        );

        // Tạo chuỗi kiểm tra (giữ lại step đầu làm "head" của chain)
        _fileValidationChain
            .setNext(new BasicFileValidationStep(_repository))
            .setNext(new VirusScanValidationStep(_repository))
            .setNext(new DuplicateContentValidationStep(_repository));
        // Thực hiện chuỗi kiểm tra
        boolean validationResult = _fileValidationChain.handleValidation(_context);

        if (!validationResult) {
            return false;
        }
        // Nếu đang ở trạng thái "Bản nháp" và đã nhập tệp đính kèm thành công, chuyển sang trạng thái "Đã tải file"
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
        // Nếu đang ở trạng thái "Đã tải file" và đã nhập thông tin nộp hồ sơ thành công, chuyển sang trạng thái "Đã tiếp nhận"
        if (doc.status == DocumentStatus.DA_TAI_FILE) {
            doc.status = DocumentStatus.DA_TIEP_NHAN;
        }
        resetChain(); // Sau khi hoàn thành quy trình tiếp nhận, reset lại chuỗi kiểm tra để sẵn sàng cho hồ sơ tiếp theo
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