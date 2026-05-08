package vn.edu.tdtu.edocument.document.state.DocumentStatusern.DocumentStatus;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;

public class ReceivedState extends DocumentStatusStateBase {
    private static HashSet<DocumentStatus> _allowTransitions = new HashSet<DocumentStatus>(
        Arrays.asList(DocumentStatus.DA_TIEP_NHAN, DocumentStatus.TU_CHOI)
    );
    public ReceivedState() {
        super(DocumentStatus.DA_TIEP_NHAN);
    }
    @Override
    protected Set<DocumentStatus> AllowTransitions() {
        return _allowTransitions;
    }
}

