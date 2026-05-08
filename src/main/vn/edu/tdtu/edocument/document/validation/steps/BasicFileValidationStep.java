package vn.edu.tdtu.edocument.document.validation.steps;

import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.validation.*;
import vn.edu.tdtu.edocument.document.model.enums.*;

public class BasicFileValidationStep extends DocumentValidationStepBase {
    public BasicFileValidationStep(IRepository repository) {
        super(repository);
    }

    @Override
    public boolean performValidation(FileValidationContext request) {
        
        // Kiểm tra dung lượng
        System.out.println("[KIỂM DUYỆT] Đang kiểm tra dung lượng và định dạng...");
        if (request.fileSizeKB > 5120) {
            System.out.println("[TỪ CHỐI] Dung lượng file " + request.fileSizeKB + "KB vượt quá 5MB.");
			return false;
        }
        
        // Kiểm tra định dạng
        if (DocumentExtension.valueOf(request.fileExtension) == null) {
            System.out.println("[TỪ CHỐI] Định dạng file " + request.fileExtension + " không hỗ trợ.");
            return false;
        }

        // Kiểm tra chữ ký số
        if (request.digitalSignature.isBlank()) {
            System.out.println("[TỪ CHỐI] File thiếu chữ ký số hợp lệ.");
            return false;
        }
        System.out.println("[THÀNH CÔNG] File hợp lệ.");
        return true;
    }
}
