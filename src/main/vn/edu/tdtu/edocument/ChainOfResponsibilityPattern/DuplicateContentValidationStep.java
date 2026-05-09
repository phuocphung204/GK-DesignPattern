package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

import vn.edu.tdtu.edocument.repositories.IRepository;
import vn.edu.tdtu.edocument.service.Hash;

public class DuplicateContentValidationStep extends DocumentValidationStepBase {
    public DuplicateContentValidationStep(IRepository repository) {
        super(repository);
    }
    @Override
    protected boolean performValidation(FileValidationContext request) {
        // Giả sử chúng ta có một phương thức để kiểm tra trùng lặp trong database
        System.out.println("[BƯỚC KIỂM TRA TRÙNG LẶP] Kiểm tra nội dung đã trích xuất...\n");
        boolean isDuplicate = checkDuplicate(request.extractedContent);
        if (isDuplicate) {
            System.out.println("[TỪ CHỐI] Hồ sơ đã tồn tại trong hệ thống.");
            return false;
        }
        System.out.println("[THÀNH CÔNG] Không phát hiện trùng lặp. Hồ sơ hợp lệ.");
        return true;
    }
    private boolean checkDuplicate(String extractedContent) {
        // Gọi phương thức từ repository để kiểm tra trùng lặp
        if (extractedContent == null || extractedContent.isBlank()) {
            return false;
        }
        return _repository.ExistsByHash(Hash.encryptThisString(extractedContent));
    }
}
