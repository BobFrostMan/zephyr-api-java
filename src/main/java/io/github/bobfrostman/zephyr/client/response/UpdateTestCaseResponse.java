package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;

public class UpdateTestCaseResponse {
    private final int statusCode;
    private final ZephyrTestCase updatedTestCase;
    private final String errorMessage;

    public UpdateTestCaseResponse(int statusCode, ZephyrTestCase updatedTestCase, String errorMessage) {
        this.statusCode = statusCode;
        this.updatedTestCase = updatedTestCase;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrTestCase getUpdatedTestCase() {
        return updatedTestCase;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}