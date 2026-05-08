package vn.edu.tdtu.edocument.document.validation;

public interface IDocumentValidationStep {
    IDocumentValidationStep setNext(IDocumentValidationStep next);
    boolean handleValidation(FileValidationContext request);
}
