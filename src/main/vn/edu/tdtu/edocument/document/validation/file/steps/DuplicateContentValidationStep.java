package vn.edu.tdtu.edocument.document.validation.file.steps;

import vn.edu.tdtu.edocument.document.validation.FileValidationContext;
import vn.edu.tdtu.edocument.document.validation.file.FileValidationStepBase;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.result.Errors;
import vn.edu.tdtu.edocument.service.Hash;

public class DuplicateContentValidationStep extends FileValidationStepBase {
    public DuplicateContentValidationStep(IRepository repository) {
        super(repository);
    }

    @Override
    protected ValidationResult performValidation(FileValidationContext request) {
        // Giả sử chúng ta có một phương thức để kiểm tra trùng lặp trong database
        if (request == null) {
            return ValidationResult.fail(Errors.DUPLICATE_CONTENT);
        }
        boolean isDuplicate = checkDuplicate(request.extractedContent);
        if (isDuplicate) {
            return ValidationResult.fail(Errors.DUPLICATE_CONTENT);
        }
        return ValidationResult.ok();
    }

    private boolean checkDuplicate(String extractedContent) {
        // Gọi phương thức từ repository để kiểm tra trùng lặp
        if (extractedContent == null || extractedContent.isBlank()) {
            return false;
        }
        return _repository.ExistsByHash(Hash.encryptThisString(extractedContent));
    }
}
