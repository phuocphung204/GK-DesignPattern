package vn.edu.tdtu.edocument.StatePattern.DocumentStatus;

import java.util.Set;

import vn.edu.tdtu.edocument.model.enums.DocumentStatus;

public abstract class DocumentStatusStateBase implements IDocumentStatusState {
    public final DocumentStatus Status;

    protected DocumentStatusStateBase(DocumentStatus status) {
        Status = status;
    }

    @Override
    public DocumentStatus getStatus() {
        return Status;
    }

    protected abstract Set<DocumentStatus> AllowTransitions();

    public boolean ValidateTransition(DocumentStatus newStatus) {
        if (AllowTransitions().contains(newStatus)) {
            return true;
        }
        return false;
    }
}
