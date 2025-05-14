package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestScript;

public class GetTestStepsResponse {
    private final int statusCode;
    private final ZephyrTestScript testScript;
    private final String errorMessage;

    public GetTestStepsResponse(int statusCode, ZephyrTestScript testScript, String errorMessage) {
        this.statusCode = statusCode;
        this.testScript = testScript;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrTestScript getTestScript() {
        return testScript;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}