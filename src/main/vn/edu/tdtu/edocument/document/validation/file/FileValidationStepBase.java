package vn.edu.tdtu.edocument.document.validation.file;

import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.validation.IDocumentValidationStep;
import vn.edu.tdtu.edocument.document.validation.FileValidationContext;

public abstract class FileValidationStepBase implements IDocumentValidationStep<FileValidationContext> {

    protected IDocumentValidationStep<FileValidationContext> nextStep;
    protected IRepository _repository;
    protected FileValidationStepBase(IRepository repository) {
        this._repository = repository;
    }

    @Override
    public IDocumentValidationStep<FileValidationContext> setNext(IDocumentValidationStep<FileValidationContext> next) {
        this.nextStep = next;
        return next;
    }

    @Override
    public ValidationResult handleValidation(FileValidationContext request) {
        ValidationResult current = performValidation(request);
        if (!current.isValid()) {
            return current;
        }
        if (nextStep == null) {
            return ValidationResult.ok(); // If no more steps, validation is successful
        }
        return nextStep.handleValidation(request); // Move to next step
    }

    protected abstract ValidationResult performValidation(FileValidationContext request);
    
}
