// io.github.bobfrostman.zephyr.client.response.GetFoldersResponse.java
package io.github.bobfrostman.zephyr.client.response;

import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseFolder;
import java.util.List;

public class GetFoldersResponse {
    private final int statusCode;
    private final List<ZephyrTestCaseFolder> folders;
    private final String errorMessage;

    public GetFoldersResponse(int statusCode, List<ZephyrTestCaseFolder> folders, String errorMessage) {
        this.statusCode = statusCode;
        this.folders = folders;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<ZephyrTestCaseFolder> getFolders() {
        return folders;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}