package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCasePriority;
import java.util.List;

public class GetPrioritiesResponse {
    private final int statusCode;
    private final List<ZephyrTestCasePriority> priorities;
    private final String errorMessage;

    public GetPrioritiesResponse(int statusCode, List<ZephyrTestCasePriority> priorities, String errorMessage) {
        this.statusCode = statusCode;
        this.priorities = priorities;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<ZephyrTestCasePriority> getPriorities() {
        return priorities;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}