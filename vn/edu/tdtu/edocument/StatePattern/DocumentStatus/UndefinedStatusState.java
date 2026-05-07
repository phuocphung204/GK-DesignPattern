package vn.edu.tdtu.edocument.StatePattern.DocumentStatus;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import vn.edu.tdtu.edocument.model.enums.DocumentStatus;

public class UndefinedStatusState extends DocumentStatusStateBase {
    private static HashSet<DocumentStatus> _allowTransitions = new HashSet<DocumentStatus>(
        Arrays.asList(DocumentStatus.BAN_NHAP)
    );
    public UndefinedStatusState() {
        super(DocumentStatus.KHONG_XAC_DINH);
    }
    @Override
    protected Set<DocumentStatus> AllowTransitions() {
        return _allowTransitions;
    }
}
