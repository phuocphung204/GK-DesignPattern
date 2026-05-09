package vn.edu.tdtu.edocument.repositories.cloud.AWS;

public class AWSConfiguration {
    private String accessKey;
    private String secretKey;
    private String region;

    public AWSConfiguration(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }
    
}
