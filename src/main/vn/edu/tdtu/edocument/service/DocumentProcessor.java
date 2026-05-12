package vn.edu.tdtu.edocument.service;

import java.io.File;
import java.util.Locale;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.repository.FileStorageHelper;
import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.validation.FileValidationContext;
import vn.edu.tdtu.edocument.document.validation.IDocumentValidationStep;
import vn.edu.tdtu.edocument.document.validation.PersonalInfoValidationContext;
import vn.edu.tdtu.edocument.document.validation.file.steps.BasicFileValidationStep;
import vn.edu.tdtu.edocument.document.validation.file.steps.DuplicateContentValidationStep;
import vn.edu.tdtu.edocument.document.validation.file.steps.VirusScanValidationStep;
import vn.edu.tdtu.edocument.document.validation.personal_info.steps.BasicInfoValidationStep;
 

public class DocumentProcessor {
    private IRepository _repository;
    private IDocumentValidationStep _personalInfoValidationChain; // Chuỗi kiểm tra thông tin người dùng, sẽ được khởi tạo khi cần thiết
    private PersonalInfoValidationContext _personalInfoContext; // Context dùng chung cho cả chuỗi kiểm tra thông tin cá nhân và tệp đính kèm, sẽ được cập nhật dần theo từng bước nhập liệu
    private IDocumentValidationStep _fileValidationChain; // Chuỗi kiểm tra tệp đính kèm, sẽ được khởi tạo khi cần thiết
    private FileValidationContext _fileContext;

    public DocumentProcessor(IRepository repository) {
        _repository = repository;
    }
    private IDocumentValidationStep buildPersonalInfoValidationChain() {
        // Tạo chuỗi kiểm tra thông tin cá nhân, có thể mở rộng thêm các bước kiểm tra khác nếu cần
        BasicInfoValidationStep head = new BasicInfoValidationStep(_repository);
        return head;
    }
    private IDocumentValidationStep buildFileValidationChain() {
        // Tạo chuỗi kiểm tra mới cho mỗi lần nhập tệp đính kèm để đảm bảo tính độc lập giữa các hồ sơ
        // Lưu ý: setNext(...) hiện trả về "next" (để fluent build), nên phải giữ reference tới head.
        BasicFileValidationStep head = new BasicFileValidationStep(_repository);
        head.setNext(new VirusScanValidationStep(_repository))
            .setNext(new DuplicateContentValidationStep(_repository));
        return head;
    }

    // Các phương thức xử lý từng bước của quy trình tiếp nhận hồ sơ
    // B1: Nhập thông tin cá nhân
    // B2: Nhập tệp đính kèm
    // B3: Nhập thông tin nộp hồ sơ
    // Mỗi bước sẽ có một phương thức riêng để xử lý, có thể gọi tuần tự hoặc độc
    // lập tùy theo luồng xử lý của ứng dụng

    public boolean proccessInsertPersonalInfo(Document doc) {
        // Xử lý thông tin cá nhân nếu cần thiết
        if (doc.applicantName.isEmpty() || doc.applicantEmail.isEmpty() || doc.applicantPhone.isEmpty()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cá nhân. Hủy tạo hồ sơ.");
            return false;
        }
        _personalInfoContext = PersonalInfoValidationContext.create(
            doc.applicantName, 
            doc.applicantEmail, 
            doc.applicantPhone);
        _personalInfoValidationChain = buildPersonalInfoValidationChain();
        ValidationResult result = _personalInfoValidationChain.handleValidation(_personalInfoContext);
        System.out.println("[KIỂM DUYỆT THÔNG TIN CÁ NHÂN] Đang kiểm tra thông tin cá nhân...");
        if (!result.isValid()) {
            System.out.println("[TỪ CHỐI] " + result.getMessageError());
            return false;
        }
        System.out.println("[THÀNH CÔNG] Thông tin cá nhân hợp lệ.");
        // Nếu chưa có trạng thái, khởi tạo bản nháp, nếu đã có trạng thái thì giữ
        // nguyên (trường hợp cập nhật thông tin cá nhân sau khi đã tạo hồ sơ)
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

        if (doc.id == null) {
            System.out.println("[LỖI TIẾP NHẬN] Hồ sơ chưa có ID. Hủy tạo hồ sơ.");
            return false;
        }

        if (doc.fileExtension == null || doc.fileExtension.isBlank()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu định dạng tệp đính kèm. Hủy tạo hồ sơ.");
            return false;
        }

        if (doc.extractedContent == null || doc.extractedContent.isBlank()) {
            System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
            // Trích xuất nội dung tệp đính kèm để phục vụ cho các bước kiểm tra tiếp theo (ví dụ: quét virus, kiểm tra trùng lặp)
            try {
                DocumentExtension docExtension = DocumentExtension.valueOf(doc.fileExtension.trim().toUpperCase(Locale.ROOT));
                File docFile = new File(doc.filePath);
                FileExtractorStrategy extractor = ExtractorFactory.getExtractor(docExtension);

                doc.extractedContent = extractor.extractContent(docFile);
            } catch (Exception e) {
                System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
                // return false;
            }

            if (doc.extractedContent == null || doc.extractedContent.isBlank()) {
                System.out.println("[LỖI] Nội dung trích xuất rỗng. Hủy tạo hồ sơ.");
                return false;
            }
            // Tính mã băm của nội dung đã trích xuất để kiểm tra trùng lặp
            doc.extractedContentHash = Hash.encryptThisString(doc.extractedContent);
        }

        // Tạo context với thông tin file để truyền vào chuỗi kiểm tra
        _fileContext = FileValidationContext.create(
            doc.documentType == null ? null : doc.documentType.toString(),
            doc.filePath,
            doc.fileExtension,
            doc.fileSizeKB,
            doc.digitalSignature,
            doc.extractedContent);

        // Tạo chuỗi kiểm tra file mới cho mỗi lần nhập tệp đính kèm để đảm bảo tính độc lập giữa các hồ sơ
        _fileValidationChain = buildFileValidationChain();
        // Thực hiện chuỗi kiểm tra
        ValidationResult result = _fileValidationChain.handleValidation(_fileContext);
        System.out.println("[KIỂM DUYỆT TỆP ĐÍNH KÈM] Đang kiểm tra tệp đính kèm...");
        if (!result.isValid()) {
            System.out.println("[TỪ CHỐI] " + result.getMessageError());
            return false;
        }
        System.out.println("[THÀNH CÔNG] Tệp đính kèm hợp lệ.");

        // Sau khi xác thực thành công, copy file đính kèm vào server_storage để dùng
        // chung cho mọi repository (JSON/MongoDB/...) và tránh phụ thuộc vào đường dẫn máy người dùng.
        try {
            // Chỉ copy file vào storage nếu tất cả các bước kiểm tra đều hợp lệ, tránh copy file không cần thiết khi đã có lỗi ở bước kiểm tra nào đó
            if (result.isValid()) {
                doc.filePath = FileStorageHelper.copyToDefaultStorage(doc.id, doc.filePath);
            }
        } catch (Exception e) {
            System.out.println("[LỖI] Không thể lưu file đính kèm vào server_storage: " + e.getMessage());
            return false;
        }
        // Nếu đang ở trạng thái "Bản nháp" và đã nhập tệp đính kèm thành công, chuyển
        // sang trạng thái "Đã tải file"
        if (doc.status == DocumentStatus.BAN_NHAP) {
            doc.status = DocumentStatus.DA_TAI_FILE;
        }
        saveToStorage(doc); // Lưu tạm hồ sơ sau khi nhập tệp đính kèm, có thể là bản nháp
        return true;
    }

    public boolean proccessInsertSubmissionInfo(Document doc) {
        // Xử lý thông tin nộp hồ sơ nếu cần thiết
        if (doc.officerName.isEmpty() || doc.officerEmail.isEmpty() || doc.officerPhone.isEmpty()) {
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin cán bộ xử lý. Hủy tạo hồ sơ.");
            return false;
        }
        _personalInfoContext = PersonalInfoValidationContext.create(
            doc.officerName, 
            doc.officerEmail, 
            doc.officerPhone);
        _personalInfoValidationChain = buildPersonalInfoValidationChain();
        ValidationResult result = _personalInfoValidationChain.handleValidation(_personalInfoContext);
        System.out.println("[KIỂM DUYỆT THÔNG TIN CÁN BỘ] Đang kiểm tra thông tin cán bộ...");
        if (!result.isValid()) {
            System.out.println("[TỪ CHỐI] " + result.getMessageError());
            return false;
        }
        System.out.println("[THÀNH CÔNG] Thông tin cán bộ hợp lệ.");
        // Nếu đang ở trạng thái "Đã tải file" và đã nhập thông tin nộp hồ sơ thành
        // công, chuyển sang trạng thái "Đã tiếp nhận"
        if (doc.status == DocumentStatus.DA_TAI_FILE) {
            doc.status = DocumentStatus.DA_TIEP_NHAN;
        }
        sendNotifications(doc);
        saveToStorage(doc); // Lưu hồ sơ sau khi nhập đầy đủ thông tin, có thể là bản nháp hoặc chính thức
                            // tùy theo logic của ứng dụng
        return true;
    }

    private void saveToStorage(Document doc) {
        var existingDoc = _repository.GetDocumentById(doc.id);
        if (existingDoc == null) {
            _repository.CreateDocument(doc);
        } else {
            _repository.UpdateDocument(doc);
        }
        // Sử dụng repository để lưu trữ hồ sơ, có thể là lưu vào file JSON hoặc cơ sở
        // dữ liệu tùy theo implementation của repository
        // Nếu Document đã tồn tại (lưu draft trước đó), repository sẽ cập nhật lại
        // thông tin, nếu chưa tồn tại sẽ tạo mới
    }

    private void sendNotifications(Document doc) {
        // Gửi thông báo qua email và SMS cho người nộp và cán bộ xử lý
        // TODO: sửa lại khi xong yc4, hiện tại chỉ in ra console để mô phỏng như demo
        // ban đầu
        System.out.println(
                "  [GỬI EMAIL] -> Người nộp (" + doc.applicantEmail + "): Hồ sơ đã được tiếp nhận và đang chờ xử lý.");
        System.out.println(
                "  [GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ đã được tiếp nhận và đang chờ xử lý.");
        System.out.println("  [GỬI EMAIL] -> Cán bộ xử lý (" + doc.officerEmail + "): Bạn có hồ sơ mới cần xử lý.");
        System.out.println("  [GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Bạn có hồ sơ mới cần xử lý.");
    }
}