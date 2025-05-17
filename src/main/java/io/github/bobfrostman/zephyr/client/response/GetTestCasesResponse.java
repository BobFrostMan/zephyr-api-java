package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;

import java.util.List;

public class GetTestCasesResponse {

    private final int statusCode;
    private final List<ZephyrTestCase> testCases;
    private final String errorMessage;

    public GetTestCasesResponse(int statusCode, List<ZephyrTestCase> testCases, String errorMessage) {
        this.statusCode = statusCode;
        this.testCases = testCases;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<ZephyrTestCase> getTestCases() {
        return testCases;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
