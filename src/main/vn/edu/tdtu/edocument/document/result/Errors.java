package vn.edu.tdtu.edocument.document.result;

public class Errors {
    public static final String NAME_IS_EMPTY = "Tên không được để trống";
    public static final String EMAIL_IS_EMPTY = "Email không được để trống";
    public static final String PHONE_IS_EMPTY = "Số điện thoại không được để trống";
    public static final String EMAIL_IS_INVALID = "Email không hợp lệ";
    public static final String PHONE_IS_INVALID = "Số điện thoại không hợp lệ. Phải là 10 chữ số.";
    public static final String FILE_SIZE_EXCEEDS_LIMIT = "Dung lượng file vượt quá giới hạn cho phép (5MB)";
    public static final String UNSUPPORTED_FILE_FORMAT = "Định dạng file không được hỗ trợ";
    public static final String DIGITAL_SIGNATURE_MISSING = "File thiếu chữ ký số hợp lệ";
    public static final String FILE_PATH_MISSING = "Thiếu đường dẫn file để quét.";
    public static final String VIRUS_SCAN_ERROR = "Lỗi khi quét tệp";
    public static final String DETECTED_MALICIOUS_SIGNATURE = "Phát hiện chữ ký độc hại trong tệp";
    public static final String DUPLICATE_CONTENT = "Hồ sơ đã tồn tại trong hệ thống";
}
