package vn.edu.tdtu.edocument.document.validation.file.steps;

import java.util.Locale;

import vn.edu.tdtu.edocument.document.validation.FileValidationContext;
import vn.edu.tdtu.edocument.document.validation.file.FileValidationStepBase;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.document.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.result.Errors;

public class BasicFileValidationStep extends FileValidationStepBase {
    public BasicFileValidationStep(IRepository repository) {
        super(repository);
    }

    @Override
    public ValidationResult performValidation(FileValidationContext request) {

        // Kiểm tra dung lượng
        if (request.fileSizeKB > 5120) {
            return ValidationResult.fail(Errors.FILE_SIZE_EXCEEDS_LIMIT);
        }

        // Kiểm tra định dạng
        if (request.fileExtension == null || request.fileExtension.isBlank()) {
            return ValidationResult.fail(Errors.UNSUPPORTED_FILE_FORMAT);
        }
        try {
            DocumentExtension.valueOf(request.fileExtension.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ValidationResult.fail(Errors.UNSUPPORTED_FILE_FORMAT);
        }

        // Kiểm tra chữ ký số
        if (request.digitalSignature == null || request.digitalSignature.isBlank()) {
            return ValidationResult.fail(Errors.DIGITAL_SIGNATURE_MISSING);
        }
        return ValidationResult.ok();
    }
}
