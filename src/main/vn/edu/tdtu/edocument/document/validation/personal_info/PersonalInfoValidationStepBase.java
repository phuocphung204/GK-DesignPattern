package vn.edu.tdtu.edocument.document.validation.personal_info;

import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.validation.PersonalInfoValidationContext;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.validation.IDocumentValidationStep;

public abstract class PersonalInfoValidationStepBase implements IDocumentValidationStep<PersonalInfoValidationContext> {

    protected IDocumentValidationStep<PersonalInfoValidationContext> nextStep;
    protected IRepository _repository;
    protected PersonalInfoValidationStepBase(IRepository repository) {
        this._repository = repository;
    }

    @Override
    public IDocumentValidationStep<PersonalInfoValidationContext> setNext(IDocumentValidationStep<PersonalInfoValidationContext> next) {
        this.nextStep = next;
        return next;
    }

    @Override
    public ValidationResult handleValidation(PersonalInfoValidationContext request) {
        ValidationResult current = performValidation(request);
        if (!current.isValid()) {
            return current;
        }
        if (nextStep == null) {
            return ValidationResult.ok(); // If no more steps, validation is successful
        }
        return nextStep.handleValidation(request); // Move to next step
    }

    protected abstract ValidationResult performValidation(PersonalInfoValidationContext request);
    
}
