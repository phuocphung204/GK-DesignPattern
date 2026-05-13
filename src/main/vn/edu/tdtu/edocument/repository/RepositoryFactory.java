package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.document.model.enums.RepositoryType;
import vn.edu.tdtu.edocument.repository.cloud.AWS.AWSRepository;
import vn.edu.tdtu.edocument.repository.database.MongoDB.MongoDBConfiguration;
import vn.edu.tdtu.edocument.repository.database.MongoDB.MongoDBDocumentRepository;
import vn.edu.tdtu.edocument.repository.local_storage.LocalJsonRepository;

public class RepositoryFactory {
    public static IRepository createRepository(RepositoryType type) {
        switch (type) {
        case MONGODB:
            try {
                // Attempt to create MongoDB repository
                MongoDBConfiguration mongoConfig = MongoDBConfiguration.getInstance();
                return new MongoDBDocumentRepository(mongoConfig);
            } catch (Exception e) {
                System.err.println("[Factory] MongoDB configuration failed: " + e.getMessage());
            }
            break;
        case AWS:
            try {
                // Fake AWS repository (stores JSON metadata under server_storage/aws)
                return AWSRepository.getInstance();
            } catch (Exception e) {
                System.err.println("[Factory] AWS configuration failed: " + e.getMessage());
            }
            break;
        case JSON:
            return LocalJsonRepository.getInstance();
        }
        throw new IllegalArgumentException("Unsupported repository type: " + type);
    }
}
