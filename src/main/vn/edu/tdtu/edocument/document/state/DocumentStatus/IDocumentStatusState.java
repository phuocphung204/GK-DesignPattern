package vn.edu.tdtu.edocument.document.state.DocumentStatus;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;

public interface IDocumentStatusState {
    DocumentStatus getStatus();

    boolean ValidateTransition(DocumentStatus newStatus);
}