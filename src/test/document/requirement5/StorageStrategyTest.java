package src.test.document.requirement5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import vn.edu.tdtu.edocument.document.builder.DocumentBuilder;
import vn.edu.tdtu.edocument.document.builder.IDocumentBuilder;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.document.model.enums.DocumentStatus;
import vn.edu.tdtu.edocument.document.model.enums.DocumentTypes;
import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.document.repository.IRepository;
import vn.edu.tdtu.edocument.document.repository.RepositoryFactory;
import vn.edu.tdtu.edocument.service.DocumentProcessor;

import java.util.List;
import java.util.UUID;

import javax.print.Doc;

import static org.junit.jupiter.api.Assertions.*;

public class StorageStrategyTest {

	private static Document buildSampleDocument(String marker) {
		IDocumentBuilder builder = new DocumentBuilder();
		builder.SetPersonalInfo(
				"Applicant " + marker,
				("applicant_" + marker + "@example.com"),
				"0123456789"
		);
		builder.SetFileInfo(
				DocumentTypes.BAO_CAO,
				"server_storage" + java.io.File.separator + marker + "_dummy.txt",
				"TXT",
				1,
				"signature-" + marker
		);
		builder.SetSubmissionInfo(
				"Officer " + marker,
				("officer_" + marker + "@example.com"),
				"0987654321"
		);
		Document doc = builder.Build();
		doc.extractedContent = "content-" + marker;
		doc.extractedContentHash = "hash-" + marker;
		doc.status = DocumentStatus.BAN_NHAP;
		return doc;
	}

	private static void deleteQuietly(IRepository repo, UUID id) {
		try {
			if (repo != null && id != null) {
				repo.DeleteDocument(id);
			}
		} catch (Exception ignored) {
			// ignore cleanup errors
		}
	}

	@Test
	@DisplayName("shouldSaveDocumentToLocalStorage")
	void shouldSaveDocumentToLocalStorage() {
		IRepository repo = RepositoryFactory.createRepository(RepositoryType.JSON);
		Document doc = buildSampleDocument("local-" + UUID.randomUUID());

		try {
			Document existing = repo.GetDocumentById(doc.id);
			if (existing == null) {
				repo.CreateDocument(doc);
			}
			else {
				repo.UpdateDocument(doc);
			}
			Document loaded = repo.GetDocumentById(doc.id);

			assertNotNull(loaded);
			assertEquals(doc.id, loaded.id);
			assertEquals(doc.applicantEmail, loaded.applicantEmail);
			assertEquals(doc.documentType, loaded.documentType);
		} finally {
			deleteQuietly(repo, doc.id);
		}
	}

	@Test
	@DisplayName("shouldSaveDocumentToMongoDbStorage")
	void shouldSaveDocumentToMongoDbStorage() {
		IRepository repo;
		try {
			repo = RepositoryFactory.createRepository(RepositoryType.MONGODB);
		} catch (Exception ex) {
			Assumptions.assumeTrue(false, "MongoDB not configured/available: " + ex.getMessage());
			return;
		}

		Document doc = buildSampleDocument("mongo-" + UUID.randomUUID());
		try {
			Document existing = repo.GetDocumentById(doc.id);
			if (existing == null) {
				repo.CreateDocument(doc);
			} else {
				repo.UpdateDocument(doc);
			}

			Document loaded;
			try {
				loaded = repo.GetDocumentById(doc.id);
			} catch (Exception ex) {
				Assumptions.assumeTrue(false, "MongoDB read failed (skipped): " + ex.getMessage());
				return;
			}

			assertNotNull(loaded);
			assertEquals(doc.id, loaded.id);
			assertEquals(doc.applicantEmail, loaded.applicantEmail);
		} finally {
			deleteQuietly(repo, doc.id);
		}
	}

	@Test
	@DisplayName("shouldSaveDocumentToAwsStorage")
	void shouldSaveDocumentToAwsStorage() {
		IRepository repo = RepositoryFactory.createRepository(RepositoryType.AWS);
		Document doc = buildSampleDocument("aws-" + UUID.randomUUID());

		try {
			Document existing = repo.GetDocumentById(doc.id);
			if (existing == null) {
				repo.CreateDocument(doc);
			} else {
				repo.UpdateDocument(doc);
			}
			Document loaded = repo.GetDocumentById(doc.id);

			assertNotNull(loaded);
			assertEquals(doc.id, loaded.id);
			assertEquals(doc.officerEmail, loaded.officerEmail);
			assertEquals(doc.fileExtension, loaded.fileExtension);
		} finally {
			deleteQuietly(repo, doc.id);
		}
	}

	@Test
	@DisplayName("shouldSwitchStorageWithoutChangingBusinessLogic")
	void shouldSwitchStorageWithoutChangingBusinessLogic() {
		IRepository localRepo = RepositoryFactory.createRepository(RepositoryType.JSON);
		IRepository awsRepo = RepositoryFactory.createRepository(RepositoryType.AWS);

		Document localDoc = buildSampleDocument("switch-local-" + UUID.randomUUID());
		Document awsDoc = buildSampleDocument("switch-aws-" + UUID.randomUUID());

		DocumentProcessor localProcessor = new DocumentProcessor(localRepo);
		DocumentProcessor awsProcessor = new DocumentProcessor(awsRepo);

		try {
			// Same business API (DocumentProcessor) but different storage strategies (IRepository).
			boolean localOk = localProcessor.proccessInsertPersonalInfo(localDoc);
			boolean awsOk = awsProcessor.proccessInsertPersonalInfo(awsDoc);

			assertTrue(localOk);
			assertTrue(awsOk);

			assertNotNull(localRepo.GetDocumentById(localDoc.id));
			assertNotNull(awsRepo.GetDocumentById(awsDoc.id));
		} finally {
			deleteQuietly(localRepo, localDoc.id);
			deleteQuietly(awsRepo, awsDoc.id);
		}
	}

	@Test
	@DisplayName("shouldFailWhenStorageUnavailable")
	void shouldFailWhenStorageUnavailable() {
		IRepository failingRepo = new IRepository() {
			@Override
			public boolean ExistsByHash(String hash) {
				return false;
			}

			@Override
			public Document GetDocumentById(UUID id) {
				throw new IllegalStateException("Storage unavailable");
			}

			@Override
			public Document GetLatestDraftOrUploaded() {
				throw new IllegalStateException("Storage unavailable");
			}

			@Override
			public List<Document> GetAllDocuments() {
				throw new IllegalStateException("Storage unavailable");
			}

			@Override
			public void CreateDocument(Document doc) {
				throw new IllegalStateException("Storage unavailable");
			}

			@Override
			public void UpdateDocument(Document doc) {
				throw new IllegalStateException("Storage unavailable");
			}

			@Override
			public void DeleteDocument(UUID id) {
				throw new IllegalStateException("Storage unavailable");
			}
		};

		DocumentProcessor processor = new DocumentProcessor(failingRepo);
		Document doc = buildSampleDocument("fail-" + UUID.randomUUID());

		assertThrows(IllegalStateException.class, () -> processor.proccessInsertPersonalInfo(doc));
	}

	@Test
	@DisplayName("shouldLoadDocumentFromSelectedStorage")
	void shouldLoadDocumentFromSelectedStorage() {
		IRepository localRepo = RepositoryFactory.createRepository(RepositoryType.JSON);
		IRepository awsRepo = RepositoryFactory.createRepository(RepositoryType.AWS);

		Document localDoc = buildSampleDocument("load-local-" + UUID.randomUUID());
		Document awsDoc = buildSampleDocument("load-aws-" + UUID.randomUUID());

		try {
			
			if (localRepo.GetDocumentById(localDoc.id) == null) {
				localRepo.CreateDocument(localDoc);
			} else {
				localRepo.UpdateDocument(localDoc);
			}

			if (awsRepo.GetDocumentById(awsDoc.id) == null) {
				awsRepo.CreateDocument(awsDoc);
			} else {
				awsRepo.UpdateDocument(awsDoc);
			}

			// Selected storage = local
			assertNotNull(localRepo.GetDocumentById(localDoc.id));
			assertNull(localRepo.GetDocumentById(awsDoc.id));

			// Selected storage = aws
			assertNotNull(awsRepo.GetDocumentById(awsDoc.id));
			assertNull(awsRepo.GetDocumentById(localDoc.id));
		} finally {
			deleteQuietly(localRepo, localDoc.id);
			deleteQuietly(awsRepo, awsDoc.id);
		}
	}

	@Test
	@DisplayName("shouldDeleteDocumentFromSelectedStorage")
	void shouldDeleteDocumentFromSelectedStorage() {
		IRepository localRepo = RepositoryFactory.createRepository(RepositoryType.JSON);
		IRepository awsRepo = RepositoryFactory.createRepository(RepositoryType.AWS);

		Document localDoc = buildSampleDocument("del-local-" + UUID.randomUUID());
		Document awsDoc = buildSampleDocument("del-aws-" + UUID.randomUUID());

		try {
			if (localRepo.GetDocumentById(localDoc.id) == null) {
				localRepo.CreateDocument(localDoc);
			} else {
				localRepo.UpdateDocument(localDoc);
			}

			if (awsRepo.GetDocumentById(awsDoc.id) == null) {
				awsRepo.CreateDocument(awsDoc);
			} else {
				awsRepo.UpdateDocument(awsDoc);
			}

			assertNotNull(localRepo.GetDocumentById(localDoc.id));
			assertNotNull(awsRepo.GetDocumentById(awsDoc.id));

			// Delete from local only
			localRepo.DeleteDocument(localDoc.id);
			assertNull(localRepo.GetDocumentById(localDoc.id));
			assertNotNull(awsRepo.GetDocumentById(awsDoc.id));

			// Delete from aws only
			awsRepo.DeleteDocument(awsDoc.id);
			assertNull(awsRepo.GetDocumentById(awsDoc.id));
		} finally {
			deleteQuietly(localRepo, localDoc.id);
			deleteQuietly(awsRepo, awsDoc.id);
		}
	}
}
