package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;

public class BasicDocumentValidationStep extends DocumentValidationStepBase {
    public BasicDocumentValidationStep(IRepository repository) {
        super(repository);
    }

    @Override
    public boolean performValidation(FileValidationContext request) {
        
        // Kiểm tra email hợp lệ
        System.out.println("[KIỂM DUYỆT] Đang kiểm tra thông tin cá nhân...");
        if (!request.applicantEmail.contains("@")) {
            System.out.println("[TỪ CHỐI] Email không hợp lệ.");
            return false;
        }
        // Kiểm tra sđt hợp lệ
        if (!request.applicantPhone.matches("\\d{10}")) {
            System.out.println("[TỪ CHỐI] Số điện thoại không hợp lệ. Phải là 10 chữ số.");
            return false;
        }
        System.out.println("[KIỂM DUYỆT] Thông tin cá nhân hợp lệ.");
        return true;
    }
}
