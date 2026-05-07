package vn.edu.tdtu.edocument.StatePattern.DocumentStatus;

import vn.edu.tdtu.edocument.model.enums.DocumentStatus;

public interface IDocumentStatusState {
    DocumentStatus getStatus();
    boolean ValidateTransition(DocumentStatus newStatus);
}