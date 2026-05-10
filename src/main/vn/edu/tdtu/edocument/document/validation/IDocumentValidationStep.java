package vn.edu.tdtu.edocument.document.validation;

import vn.edu.tdtu.edocument.document.result.ValidationResult;

public interface IDocumentValidationStep<TContext> {
    IDocumentValidationStep<TContext> setNext(IDocumentValidationStep<TContext> next);
    ValidationResult handleValidation(TContext request);
}
