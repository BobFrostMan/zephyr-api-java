package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;

public class CreateTestCaseResponse {
    private final int statusCode;
    private final ZephyrTestCase createdTestCase;
    private final String errorMessage;

    public CreateTestCaseResponse(int statusCode, ZephyrTestCase createdTestCase, String errorMessage) {
        this.statusCode = statusCode;
        this.createdTestCase = createdTestCase;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrTestCase getCreatedTestCase() {
        return createdTestCase;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}