package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrProject;

public class GetProjectResponse {
    private final int statusCode;
    private final ZephyrProject project;
    private final String errorMessage;

    public GetProjectResponse(int statusCode, ZephyrProject project, String errorMessage) {
        this.statusCode = statusCode;
        this.project = project;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrProject getProject() {
        return project;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}