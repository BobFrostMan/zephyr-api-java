package io.github.bobfrostman.zephyr.client;

public class ZephyrProjectClientBuilder {

    private String apiUrl;
    private String token;
    private String projectKey;
    private boolean verbose;

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

    public ZephyrProjectClientBuilder withVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public ZephyrProjectApiClient build() {
        return new ZephyrProjectApiClient(apiUrl, token, projectKey, verbose);
    }
}