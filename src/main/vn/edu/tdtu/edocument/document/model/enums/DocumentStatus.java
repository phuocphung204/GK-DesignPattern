package vn.edu.tdtu.edocument.document.model.enums;

public enum DocumentStatus {
    KHONG_XAC_DINH,
    BAN_NHAP,
    DA_TAI_FILE,
    DA_TIEP_NHAN,
    TU_CHOI,
    DANG_XET_DUYET
}
// Version 1.0: Chỉ có trạng thái "Đã tiếp nhận", "Từ chối" và "Đang xét duyệt".
// Version 2.0: Thêm trạng thái "Bản nháp" và "Đã nộp file" để phản ánh rõ hơn các giai đoạn trong quy trình xử lý hồ sơ.

// Ban nhap -> Da tai file -> Da tiep nhan -> Dang xet duyet
// Ban nhap -> Da tai file -> Da tiep nhan -> Tu choi