package vn.edu.tdtu.edocument.document.repository.cloud.AWS;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Fake AWS configuration.
 *
 * For this project we keep it lightweight and permissive: if environment values
 * are missing we fall back to placeholder defaults.
 */
public class AWSConfiguration {
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucketName;

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private static class Holder {
        private static final AWSConfiguration INSTANCE = new AWSConfiguration();
    }

    /**
     * Creates a configuration with explicit values (used in tests/demos).
     */
    public AWSConfiguration(String accessKey, String secretKey, String region) {
        this(accessKey, secretKey, region, "fake-bucket");
    }

    public AWSConfiguration(String accessKey, String secretKey, String region, String bucketName) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucketName = bucketName;
    }

    /**
     * Creates a configuration from env vars / system props / .env with safe defaults.
     */
    private AWSConfiguration() {
        this(
                resolve("AWS_ACCESS_KEY", "ACCESS_KEY", "AWS_ACCESS_KEY_ID"),
                resolve("AWS_SECRET_KEY", "SECRET_KEY", "AWS_SECRET_ACCESS_KEY"),
                resolve("AWS_REGION", "REGION"),
                resolve("AWS_BUCKET", "BUCKET", "AWS_BUCKET_NAME")
        );
    }

    public static AWSConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    public String getAccessKey() {
        return accessKey == null || accessKey.isBlank() ? "FAKE_ACCESS_KEY" : accessKey;
    }

    public String getSecretKey() {
        return secretKey == null || secretKey.isBlank() ? "FAKE_SECRET_KEY" : secretKey;
    }

    public String getRegion() {
        return region == null || region.isBlank() ? "ap-southeast-1" : region;
    }

    public String getBucketName() {
        return bucketName == null || bucketName.isBlank() ? "fake-bucket" : bucketName;
    }

    private static String resolve(String... keys) {
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = System.getenv(key);
            if (value == null || value.isBlank()) {
                value = System.getProperty(key);
            }
            if (value == null || value.isBlank()) {
                value = DOTENV.get(key);
            }
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
