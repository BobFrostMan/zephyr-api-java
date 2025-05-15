package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseStatus;

import java.util.List;

public class GetStatusesResponse {
    private final int statusCode;
    private final List<ZephyrTestCaseStatus> statuses;
    private final String errorMessage;

    public GetStatusesResponse(int statusCode, List<ZephyrTestCaseStatus> statuses, String errorMessage) {
        this.statusCode = statusCode;
        this.statuses = statuses;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<ZephyrTestCaseStatus> getStatuses() {
        return statuses;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}