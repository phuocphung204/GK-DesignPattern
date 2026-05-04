package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;
import vn.edu.tdtu.edocument.model.enums.*;

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
        
        return true;
    }
}
