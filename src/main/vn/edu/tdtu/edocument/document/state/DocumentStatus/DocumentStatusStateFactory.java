package vn.edu.tdtu.edocument.document.state.DocumentStatus;

import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;

public class DocumentStatusStateFactory {
    public static IDocumentStatusState Create(DocumentStatus status) {
        switch (status) {
        case BAN_NHAP:
            return new DraftStatusState();
        case DA_TAI_FILE:
            return new FileLoadedState();
        case DA_TIEP_NHAN:
            return new ReceivedState();
        case KHONG_XAC_DINH:
            return new UndefinedStatusState();
        default:
            return new UndefinedStatusState();
        }
    }
}
