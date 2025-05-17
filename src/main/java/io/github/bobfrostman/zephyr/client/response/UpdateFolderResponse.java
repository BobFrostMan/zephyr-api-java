package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseFolder;

public class UpdateFolderResponse {

    private final int statusCode;
    private final ZephyrTestCaseFolder folder;
    private final String errorMessage;

    public UpdateFolderResponse(int statusCode, ZephyrTestCaseFolder folder, String errorMessage) {
        this.statusCode = statusCode;
        this.folder = folder;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ZephyrTestCaseFolder getUpdatedFolder() {
        return folder;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
