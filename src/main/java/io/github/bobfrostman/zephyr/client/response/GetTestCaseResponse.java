package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;

public class GetTestCaseResponse {
    private final int statusCode;
    private final ZephyrTestCase testCase;
    private final String errorMessage;

    public GetTestCaseResponse(int statusCode, ZephyrTestCase testCase, String errorMessage) {
        this.statusCode = statusCode;
        this.testCase = testCase;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrTestCase getTestCase() {
        return testCase;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}