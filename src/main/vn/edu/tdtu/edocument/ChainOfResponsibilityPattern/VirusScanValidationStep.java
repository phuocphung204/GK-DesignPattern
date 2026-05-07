package vn.edu.tdtu.edocument.ChainOfResponsibilityPattern;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import vn.edu.tdtu.edocument.RepositoryPattern.IRepository;

public class VirusScanValidationStep extends DocumentValidationStepBase {

	public VirusScanValidationStep(IRepository repository) {
		super(repository);
	}

	@Override
	public boolean performValidation(FileValidationContext request) {
		System.out.println("[KIỂM DUYỆT] Đang quét an toàn bảo mật (Antivirus) tệp đính kèm...");
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(request.filePath));
			String content = new String(bytes, StandardCharsets.UTF_8).toLowerCase();

			String[] signatures = new String[] {
				"virus", "malware", "trojan", "ransom", "worm", "phishing", "keylogger"
			};

			for (String signature : signatures) {
				if (content.contains(signature)) {
					System.out.println("[TỪ CHỐI] Phát hiện chữ ký độc hại: " + signature + ".");
					return false;
				}
			}
		} catch (Exception e) {
			System.out.println("[TỪ CHỐI] Lỗi khi quét tệp: " + e.getMessage());
			return false;
		}
		System.out.println("[THÀNH CÔNG] Không phát hiện mối đe dọa nào. Tệp an toàn.");
		return true;
	}
}
