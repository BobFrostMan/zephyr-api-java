package io.github.bobfrostman.zephyr.client;

public class ZephyrProjectClientBuilder {

    private String apiUrl;
    private String token;
    private String projectKey;

    public ZephyrProjectClientBuilder() {

    }

    public ZephyrProjectClientBuilder withApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public ZephyrProjectClientBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public ZephyrProjectClientBuilder withProjectKey(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }

    public ZephyrProjectApiClient build() {
        return new ZephyrProjectApiClient(apiUrl, token, projectKey);
    }
}