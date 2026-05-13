package vn.edu.tdtu.edocument.document.validation.personal_info.steps;

import vn.edu.tdtu.edocument.document.validation.personal_info.PersonalInfoValidationStepBase;
import vn.edu.tdtu.edocument.repository.IRepository;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.result.Errors;
import vn.edu.tdtu.edocument.document.validation.PersonalInfoValidationContext;

public class BasicInfoValidationStep extends PersonalInfoValidationStepBase {
    public BasicInfoValidationStep(IRepository repository) {
        super(repository);
    }

    @Override
    public ValidationResult performValidation(PersonalInfoValidationContext request) {
        // Kiểm tra email hợp lệ
        if (request == null || request.email == null || !request.email.contains("@")) {
            return ValidationResult.fail(Errors.EMAIL_IS_INVALID);
        }
        // Kiểm tra sđt hợp lệ
        if (request.phone == null || !request.phone.matches("\\d{10}")) {
            return ValidationResult.fail(Errors.PHONE_IS_INVALID);
        }
        return ValidationResult.ok();
    }
}
