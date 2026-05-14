package document.requirement3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.result.ValidationResult;
import vn.edu.tdtu.edocument.document.validation.FileValidationContext;
import vn.edu.tdtu.edocument.document.validation.file.FileValidationStepBase;
import vn.edu.tdtu.edocument.repository.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationChainBehaviorTest {

    private static final IRepository NOOP_REPOSITORY = new IRepository() {
        @Override
        public boolean ExistsByHash(String hash) {
            return false;
        }

        @Override
        public Document GetDocumentById(UUID id) {
            return null;
        }

        @Override
        public Document GetLatestDraft() {
            return null;
        }

        @Override
        public List<Document> GetAllDocuments() {
            return List.of();
        }

        @Override
        public void CreateDocument(Document doc) {
            // no-op
        }

        @Override
        public void UpdateDocument(Document doc) {
            // no-op
        }

        @Override
        public void DeleteDocument(UUID id) {
            // no-op
        }
    };

    private static FileValidationContext sampleContext() {
        return FileValidationContext.create(UUID.randomUUID(), "BAO_CAO", "support_test/passAll.txt", "TXT", 1,
            "signature", "extracted-content");
    }

    private static final class RecordingStep extends FileValidationStepBase {
        private final String name;
        private final List<String> calls;
        private final ValidationResult toReturn;

        private RecordingStep(IRepository repository, String name, List<String> calls, ValidationResult toReturn) {
            super(repository);
            this.name = name;
            this.calls = calls;
            this.toReturn = toReturn;
        }

        @Override
        protected ValidationResult performValidation(FileValidationContext request) {
            calls.add(name);
            return toReturn;
        }
    }

    @Test
    @DisplayName("shouldAllowReorderingHandlers")
    void shouldAllowReorderingHandlers() {
        FileValidationContext context = sampleContext();

        List<String> calls1 = new ArrayList<>();
        RecordingStep stepA1 = new RecordingStep(NOOP_REPOSITORY, "A", calls1, ValidationResult.ok());
        RecordingStep stepB1 = new RecordingStep(NOOP_REPOSITORY, "B", calls1, ValidationResult.ok());
        stepA1.setNext(stepB1);

        ValidationResult result1 = stepA1.handleValidation(context);
        assertTrue(result1.isValid());
        assertEquals(List.of("A", "B"), calls1);

        List<String> calls2 = new ArrayList<>();
        RecordingStep stepA2 = new RecordingStep(NOOP_REPOSITORY, "A", calls2, ValidationResult.ok());
        RecordingStep stepB2 = new RecordingStep(NOOP_REPOSITORY, "B", calls2, ValidationResult.ok());
        stepB2.setNext(stepA2);

        ValidationResult result2 = stepB2.handleValidation(context);
        assertTrue(result2.isValid());
        assertEquals(List.of("B", "A"), calls2);
    }

    @Test
    @DisplayName("shouldAllowAddingNewValidationHandler")
    void shouldAllowAddingNewValidationHandler() {
        FileValidationContext context = sampleContext();

        List<String> calls = new ArrayList<>();
        RecordingStep step1 = new RecordingStep(NOOP_REPOSITORY, "1", calls, ValidationResult.ok());
        RecordingStep step2 = new RecordingStep(NOOP_REPOSITORY, "2", calls, ValidationResult.ok());
        RecordingStep step3 = new RecordingStep(NOOP_REPOSITORY, "3", calls, ValidationResult.ok());

        step1.setNext(step2);
        step2.setNext(step3); // add new handler without changing existing steps

        ValidationResult result = step1.handleValidation(context);
        assertTrue(result.isValid());
        assertEquals(List.of("1", "2", "3"), calls);
    }

    @Test
    @DisplayName("shouldSkipRemainingValidationWhenOneStepFails")
    void shouldSkipRemainingValidationWhenOneStepFails() {
        FileValidationContext context = sampleContext();

        List<String> calls = new ArrayList<>();
        RecordingStep step1 = new RecordingStep(NOOP_REPOSITORY, "1", calls, ValidationResult.ok());
        RecordingStep step2 = new RecordingStep(NOOP_REPOSITORY, "2", calls, ValidationResult.fail("FAILED_AT_2"));
        RecordingStep step3 = new RecordingStep(NOOP_REPOSITORY, "3", calls, ValidationResult.ok());

        step1.setNext(step2);
        step2.setNext(step3);

        ValidationResult result = step1.handleValidation(context);
        assertFalse(result.isValid());
        assertEquals("FAILED_AT_2", result.getMessageError());
        assertEquals(List.of("1", "2"), calls, "Step 3 must be skipped after a failure");
    }

    @Test
    @DisplayName("shouldReturnValidationSuccessWhenAllHandlersPass")
    void shouldReturnValidationSuccessWhenAllHandlersPass() {
        FileValidationContext context = sampleContext();

        List<String> calls = new ArrayList<>();
        RecordingStep step1 = new RecordingStep(NOOP_REPOSITORY, "1", calls, ValidationResult.ok());
        RecordingStep step2 = new RecordingStep(NOOP_REPOSITORY, "2", calls, ValidationResult.ok());

        step1.setNext(step2);

        ValidationResult result = step1.handleValidation(context);
        assertTrue(result.isValid());
        assertEquals(List.of("1", "2"), calls);
    }
}
