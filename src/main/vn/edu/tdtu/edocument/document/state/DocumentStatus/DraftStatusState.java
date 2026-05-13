package vn.edu.tdtu.edocument.document.state.DocumentStatus;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;

public class DraftStatusState extends DocumentStatusStateBase {
    private static HashSet<DocumentStatus> _allowTransitions = new HashSet<DocumentStatus>(
            Arrays.asList(DocumentStatus.DA_TAI_FILE));

    public DraftStatusState() {
        super(DocumentStatus.BAN_NHAP);
    }

    @Override
    protected Set<DocumentStatus> AllowTransitions() {
        return _allowTransitions;
    }
}
