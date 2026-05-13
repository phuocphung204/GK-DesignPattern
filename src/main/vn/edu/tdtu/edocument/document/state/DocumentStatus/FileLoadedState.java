package vn.edu.tdtu.edocument.document.state.DocumentStatus;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;

public class FileLoadedState extends DocumentStatusStateBase {
    private static HashSet<DocumentStatus> _allowTransitions = new HashSet<DocumentStatus>(
            Arrays.asList(DocumentStatus.DA_TIEP_NHAN));

    public FileLoadedState() {
        super(DocumentStatus.DA_TAI_FILE);
    }

    @Override
    protected Set<DocumentStatus> AllowTransitions() {
        return _allowTransitions;
    }
}
