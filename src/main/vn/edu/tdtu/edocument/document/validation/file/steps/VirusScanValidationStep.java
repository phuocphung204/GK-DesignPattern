package vn.edu.tdtu.edocument.document.validation.file.steps;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.validation.FileValidationContext;
import vn.edu.tdtu.edocument.document.validation.file.FileValidationStepBase;
import vn.edu.tdtu.edocument.document.model.enums.DocumentExtension;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.result.Errors;

public class VirusScanValidationStep extends FileValidationStepBase {

	public VirusScanValidationStep(IRepository repository) {
		super(repository);
	}

	@Override
	public ValidationResult performValidation(FileValidationContext request) {
		try {
			if (request == null || request.filePath == null || request.filePath.isBlank()) {
				return ValidationResult.fail(Errors.FILE_PATH_MISSING);
			}
			byte[] bytes = Files.readAllBytes(Paths.get(request.filePath));
			String content = new String(bytes, StandardCharsets.UTF_8).toLowerCase();

			String[] signatures = new String[] {
				"virus", "malware", "trojan", "ransom", "worm", "phishing", "keylogger"
			};

			for (String signature : signatures) {
				if (content.contains(signature)) {
					return ValidationResult.fail(Errors.DETECTED_MALICIOUS_SIGNATURE);
				}
			}
		} catch (Exception e) {
			return ValidationResult.fail(Errors.VIRUS_SCAN_ERROR);
		}
		return ValidationResult.ok();
	}
}
