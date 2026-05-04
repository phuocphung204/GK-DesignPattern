package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

public interface IDocumentValidationStep {
    IDocumentValidationStep setNext(IDocumentValidationStep next);
    boolean handleValidation(FileValidationContext request);
}
