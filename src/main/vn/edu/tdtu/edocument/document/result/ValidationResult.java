package vn.edu.tdtu.edocument.document.result;

public class ValidationResult {
	private final boolean isValid;
	private final String messageError;

	private ValidationResult(boolean isValid, String messageError) {
		this.isValid = isValid;
		this.messageError = messageError;
	}

	public static ValidationResult ok() {
		return new ValidationResult(true, null);
	}

	public static ValidationResult fail(String messageError) {
		return new ValidationResult(false, messageError);
	}

	public boolean isValid() {
		return isValid;
	}

	public String getMessageError() {
		return messageError;
	}
}
