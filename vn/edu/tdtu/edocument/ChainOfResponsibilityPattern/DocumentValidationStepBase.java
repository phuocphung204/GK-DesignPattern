package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;

public abstract class DocumentValidationStepBase implements IDocumentValidationStep {

    protected IDocumentValidationStep nextStep;
    protected IRepository _repository;
    protected DocumentValidationStepBase(IRepository repository) {
        this._repository = repository;
    }

    @Override
    public IDocumentValidationStep setNext(IDocumentValidationStep next) {
        this.nextStep = next;
        return next;
    }

    @Override
    public boolean handleValidation(FileValidationContext request) {
        boolean current = performValidation(request);
        if (!current) {
            return false; // If current step fails, validation fails
        }
        if (nextStep == null) {
            return true; // If no more steps, validation is successful
        }
        return nextStep.handleValidation(request); // Move to next step
    }

    protected abstract boolean performValidation(FileValidationContext request);
    
}
