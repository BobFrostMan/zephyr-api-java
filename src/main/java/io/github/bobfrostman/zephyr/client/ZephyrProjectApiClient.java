package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import io.github.bobfrostman.zephyr.client.builder.NewFolderBuilder;
import io.github.bobfrostman.zephyr.client.builder.NewTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.builder.UpdateTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.cache.ZephyrProjectClientCache;
import io.github.bobfrostman.zephyr.client.response.*;
import io.github.bobfrostman.zephyr.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.bobfrostman.zephyr.client.ZephyrResponseParser.*;

public class ZephyrProjectApiClient implements IZephyrProjectApiClient {

    private static final String PROJECT_REQUEST_PARAMS = "%s%s?projectKey=%s&maxResults=1000";

    private String apiUrl;
    private String token;
    private String projectKey;

    private ZephyrProjectClientCache clientCache;

    ZephyrProjectApiClient(String url, String token, String projectKey) {
        this.apiUrl = url.endsWith("/") ? url : url + "/";
        this.token = token;
        this.projectKey = projectKey;
    }

    @Override
    public GetTestCasesResponse getTestCases(boolean includeSteps) {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "testcases", projectKey);
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();

            if (statusCode >= 200 && statusCode < 300) {
                JsonObject jsonResponse = Json.parse(responseBody).asObject();
                JsonArray valuesArray = jsonResponse.get("values").asArray();
                List<ZephyrTestCase> testCases = new ArrayList<>();
                ZephyrProjectClientCache cache = useCache();
                for (int i = 0; i < valuesArray.size(); i++) {
                    ZephyrTestCase testCase = ZephyrResponseParser.parseTestCase(valuesArray.get(i).asObject(), cache);
                    if (includeSteps) {
                        GetTestStepsResponse stepsResponse = getTestSteps(testCase.getKey());
                        if (stepsResponse.isSuccessful()) {
                            testCase.setSteps(stepsResponse.getTestScript().getSteps());
                        } else {
                            System.err.printf("Failed to fetch steps for testcase '%s': %s%n", testCase.getKey(), stepsResponse.getErrorMessage());
                        }
                    }
                    testCases.add(testCase);
                }
                return new GetTestCasesResponse(statusCode, testCases, null);
            } else {
                String errorMessage = Json.parse(responseBody).asObject().getString("message", "Failed to receive testcases");
                return new GetTestCasesResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetTestCasesResponse(-1, null, "Error during testcases fetching: " + e.getMessage());
        }
    }

    @Override
    public GetTestCasesResponse getTestCases() {
        return getTestCases(true);
    }

    @Override
    public GetTestCaseResponse getTestCase(String testCaseKey, boolean includeSteps) {
        String url = String.format("%stestcases/%s", apiUrl, testCaseKey);
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();

            if (statusCode >= 200 && statusCode < 300) {
                JsonObject jsonResponse = Json.parse(responseBody).asObject();
                ZephyrTestCase testCase = ZephyrResponseParser.parseTestCase(jsonResponse, useCache());
                if (includeSteps) {
                    GetTestStepsResponse stepsResponse = getTestSteps(testCaseKey);
                    if (stepsResponse.isSuccessful()) {
                        testCase.setSteps(stepsResponse.getTestScript().getSteps());
                    } else {
                        System.err.printf("Failed to receive teststeps for testcase '%s': %s%n", testCaseKey, stepsResponse.getErrorMessage());
                    }
                }
                return new GetTestCaseResponse(statusCode, testCase, null);
            } else {
                String errorMessage = Json.parse(responseBody).asObject().getString("message", String.format("Failed to fetch testcase '%s'", testCaseKey));
                return new GetTestCaseResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetTestCaseResponse(-1, null, String.format("Error during testcase fetch '%s': %s", testCaseKey, e.getMessage()));
        }
    }

    @Override
    public GetTestCaseResponse getTestCase(String testCaseKey) {
        return getTestCase(testCaseKey, false);
    }

    @Override
    public NewTestCaseBuilder newTestCase() {
        return new ConcreteNewTestCaseBuilder(projectKey);
    }

    @Override
    public UpdateTestCaseBuilder updateTestCase(String testCaseKey) {
        GetTestCaseResponse response = getTestCase(testCaseKey);
        if (response.isSuccessful()) {
            ZephyrTestCase tc = response.getTestCase();
            return new ConcreteUpdateTestCaseBuilder(testCaseKey)
                    .withName(tc.getName())
                    .withObjective(tc.getObjective())
                    .withFolderId(tc.getFolderId())
                    .withPrecondition(tc.getPrecondition())
                    .withPriorityName(tc.getPriorityName())
                    .withStatusName(tc.getStatusName())
                    .withSteps(tc.getSteps())
                    .withLabels(tc.getLabels())
                    .withCustomFields(tc.getCustomFields());
        } else {
            throw new RuntimeException("Cannot obtain information about testcase with Id '" + testCaseKey + "'. " + response.getErrorMessage());
        }
    }

    @Override
    public GetStatusesResponse getStatuses() {
        ZephyrProjectClientCache cache = useCache();
        if (cache != null && cache.getStatuses() != null && !cache.getStatuses().isEmpty()) {
            return new GetStatusesResponse(200, cache.getStatuses(), null);
        }
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "statuses", projectKey);
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCaseStatus> statuses = parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCaseStatus.class, cache);
            if (statusCode >= 200 && statusCode < 300) {
                if (clientCache != null) {
                    clientCache.setTestCaseStatuses(statuses);
                }
                return new GetStatusesResponse(statusCode, statuses, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive statuses");
                return new GetStatusesResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetStatusesResponse(-1, null, "Error during fetching statuses: " + e.getMessage());
        }
    }


    @Override
    public GetPrioritiesResponse getPriorities() {
        ZephyrProjectClientCache cache = useCache();
        if (cache != null && cache.getPriorities() != null && !cache.getPriorities().isEmpty()) {
            return new GetPrioritiesResponse(200, cache.getPriorities(), null);
        }

        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "priorities", projectKey);
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCasePriority> priorities = parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCasePriority.class, cache);

            if (statusCode >= 200 && statusCode < 300) {
                if (clientCache != null) {
                    clientCache.setPrioritiesCache(priorities);
                }
                return new GetPrioritiesResponse(statusCode, priorities, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive priorities");
                return new GetPrioritiesResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetPrioritiesResponse(-1, null, "Error during fetching priorities: " + e.getMessage());
        }
    }

    @Override
    public GetFoldersResponse getFolders() {
        ZephyrProjectClientCache cache = useCache();
        if (cache != null && cache.getFolders() != null && !cache.getFolders().isEmpty()) {
            return new GetFoldersResponse(200, cache.getFolders(), null);
        }

        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "folders", projectKey) + "&folderType=TEST_CASE";
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCaseFolder> folders = parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCaseFolder.class, cache);

            if (statusCode >= 200 && statusCode < 300) {
                if (clientCache != null) {
                    clientCache.setFoldersCache(folders);
                }
                return new GetFoldersResponse(statusCode, folders, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new GetFoldersResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetFoldersResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }

    @Override
    public GetTestStepsResponse getTestSteps(String testCaseKey) {
        String url = apiUrl + "testcases/" + testCaseKey + "/testscript";
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrTestScript script = getParserFunction(ZephyrTestScript.class).apply(jsonResponse.asObject(), clientCache);
                return new GetTestStepsResponse(statusCode, script, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new GetTestStepsResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetTestStepsResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }

    private CreateTestStepsResponse createTestScript(String testCaseKey, String type, List<String> steps) {
        String url = apiUrl + "testcases/" + testCaseKey + "/testscript";
        JsonObject object = Json.object()
                .add("type", type)
                .add("text", String.join("\n", steps));
        try {
            ApiResponse response = BasicApiClient.executePost(url, object.toString(), token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrTestScript script = new ZephyrTestScript();
                script.setId(jsonResponse.getLong("id", -1));
                script.setType(type);
                script.setSteps(steps);
                return new CreateTestStepsResponse(statusCode, script, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot create test script");
                return new CreateTestStepsResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new CreateTestStepsResponse(-1, null, "Error during test script creation: " + e.getMessage());
        }
    }

    @Override
    public GetProjectResponse getProject() {
        ZephyrProjectClientCache cache = useCache();
        if (cache != null && cache.getProject() != null) {
            return new GetProjectResponse(200, cache.getProject(), null);
        }
        String url = apiUrl + "projects/" + projectKey;
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrProject script = getParserFunction(ZephyrProject.class).apply(jsonResponse.asObject(), cache);
                return new GetProjectResponse(statusCode, script, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new GetProjectResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new GetProjectResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }

    private CreateTestCaseResponse createTestCase(ZephyrTestCase testCase) {
        ZephyrProjectClientCache cache = useCache();
        JsonObject object = Json.object()
                .add("projectKey", testCase.getProjectKey())
                .add("name", testCase.getName())
                .add("objective", testCase.getObjective())
                .add("priorityName", testCase.getPriorityName())
                .add("statusName", testCase.getStatusName())
                .add("labels", Json.parse(testCase.getLabels().toString()));
        if (testCase.getFolderId() == null) {
            List<ZephyrTestCaseFolder> folderList = cache.getFolders().stream().filter(folder -> testCase.getPath().equals(folder.getPath())).toList();
            if (!folderList.isEmpty()) {
                Long folderId = folderList.get(0).getId();
                object.add("folderId", folderId);
            } else {
                //TODO: find folder recursively
                int index = testCase.getPath().length()-1;
                if (testCase.getPath().endsWith("/")){
                    index -= 1;
                }
                String prevFolder = testCase.getPath().substring(0, testCase.getPath().lastIndexOf("/", index)) + "/";
                //TODO: path is not resolved properly for inner folders
            }
        }
        JsonObject customFields = Json.object();
        for (Map.Entry<String, Object> entry : testCase.getCustomFields().entrySet()) {
            object.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        object.add("customFields", customFields);

        String url = String.format("%stestcases", apiUrl);
        try {
            ApiResponse response = BasicApiClient.executePost(url, object.toString(), token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrTestCase tc = ZephyrTestCase.builder()
                        .key(jsonResponse.getString("key"))
                        .name(testCase.getName())
                        .projectKey(projectKey)
                        .objective(testCase.getObjective())
                        .precondition(testCase.getPrecondition())
                        .labels(testCase.getLabels())
                        .priorityName(testCase.getPriorityName())
                        .statusName(testCase.getStatusName())
                        .path(testCase.getPath())
                        .folderId(testCase.getFolderId())
                        .customFields(testCase.getCustomFields())
                        .steps(testCase.getSteps())
                        .build();
                //getParserFunction(ZephyrTestCase.class).apply(jsonResponse.asObject(), cache);
                if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
                    CreateTestStepsResponse stepsResponse = createTestScript(tc.getKey(), "bdd", testCase.getSteps());
                    if (!stepsResponse.isSuccessful()) {
                        throw new RuntimeException("Cannot create test script steps: " + stepsResponse.getErrorMessage());
                    } else {
                        refreshFoldersCache();
                    }
                }
                return new CreateTestCaseResponse(statusCode, tc, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new CreateTestCaseResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new CreateTestCaseResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }


    private UpdateTestCaseResponse updateTestCase(String testCaseKey, ZephyrTestCase testCase) {
        String url = String.format("%stestcases/%s", apiUrl, testCaseKey);
        ZephyrProjectClientCache cache = useCache();
        JsonObject object = Json.object()
                .add("projectKey", testCase.getProjectKey())
                .add("name", testCase.getName())
                .add("objective", testCase.getObjective())
                .add("priorityName", testCase.getPriorityName())
                .add("statusName", testCase.getStatusName())
                .add("labels", Json.parse(testCase.getLabels().toString()));
        if (testCase.getFolderId() == null) {
            Long folderId = cache.getFolders().stream().filter(folder -> testCase.getPath().equals(folder.getPath())).toList().get(0).getId();
            object.add("folderId", folderId);
        }
        JsonObject customFields = Json.object();
        for (Map.Entry<String, Object> entry : testCase.getCustomFields().entrySet()) {
            object.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        object.add("customFields", customFields);
        try {
            ApiResponse response = BasicApiClient.executePut(url, object.toString(), token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrTestCase tc = getParserFunction(ZephyrTestCase.class).apply(jsonResponse.asObject(), cache);
                if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
                    CreateTestStepsResponse stepsResponse = createTestScript(tc.getKey(), "bdd", testCase.getSteps());
                    if (!stepsResponse.isSuccessful()) {
                        throw new RuntimeException("Cannot create test script steps: " + stepsResponse.getErrorMessage());
                    } else {
                        refreshFoldersCache();
                    }
                }
                return new UpdateTestCaseResponse(statusCode, tc, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new UpdateTestCaseResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new UpdateTestCaseResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }

    private CreateFolderResponse createTestCaseFolder(ZephyrTestCaseFolder folder) {
        ZephyrProjectClientCache cache = useCache();
        String url = String.format("%sfolders", apiUrl);
        JsonObject object = Json.object()
                .add("name", folder.getName())
                .add("projectKey", projectKey);
        if (folder.getFolderType() == null) {
            object.add("folderType", "TEST_CASE");
        }
        if (folder.getParentId() != null) {
            object.add("parentId", folder.getParentId());
        } else {
            object.add("parentId", Json.NULL);
        }

        try {
            ApiResponse response = BasicApiClient.executePost(url, object.toString(), token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            if (statusCode >= 200 && statusCode < 300) {

                ZephyrTestCaseFolder folder1 = ZephyrTestCaseFolder.builder()
                        .id(jsonResponse.getLong("id", -1))
                        .parentId(folder.getParentId())
                        .folderType(folder.getFolderType())
                        .path(resolvePath(jsonResponse.getLong("id", -1), cache.getFolders()))
                        .name(folder.getName()).build();
                return new CreateFolderResponse(statusCode, folder1, null);
            } else {
                String errorMessage = jsonResponse.getString("message", "Cannot receive folders");
                return new CreateFolderResponse(statusCode, null, errorMessage);
            }
        } catch (IOException e) {
            return new CreateFolderResponse(-1, null, "Error during fetching folders: " + e.getMessage());
        }
    }

    @Override
    public NewFolderBuilder newTestCaseFolder() {
        return new ConcreteNewFolderBuilder();
    }

    private class ConcreteNewTestCaseBuilder implements NewTestCaseBuilder {
        private final ZephyrTestCase.Builder builder;

        public ConcreteNewTestCaseBuilder(String projectKey) {
            this.builder = ZephyrTestCase.builder().projectKey(projectKey);
        }

        @Override
        public NewTestCaseBuilder withName(String name) {
            builder.name(name);
            return this;
        }

        @Override
        public NewTestCaseBuilder withObjective(String objective) {
            builder.objective(objective);
            return this;
        }

        @Override
        public NewTestCaseBuilder withPrecondition(String precondition) {
            builder.precondition(precondition);
            return this;
        }

        @Override
        public NewTestCaseBuilder withLabel(String label) {
            builder.label(label);
            return this;
        }

        @Override
        public NewTestCaseBuilder withLabels(List<String> labels) {
            builder.labels(labels);
            return this;
        }

        @Override
        public NewTestCaseBuilder withPriorityName(String priorityName) {
            builder.priorityName(priorityName);
            return this;
        }

        @Override
        public NewTestCaseBuilder withStatusName(String statusName) {
            builder.statusName(statusName);
            return this;
        }

        @Override
        public NewTestCaseBuilder withFolderId(Long folderId) {
            builder.folderId(folderId);
            return this;
        }

        @Override
        public NewTestCaseBuilder withFolderPath(String folderPath) {
            builder.path(folderPath);
            return this;
        }

        @Override
        public NewTestCaseBuilder withStep(String step) {
            builder.step(step);
            return this;
        }

        @Override
        public NewTestCaseBuilder withSteps(List<String> steps) {
            builder.steps(steps);
            return this;
        }

        @Override
        public NewTestCaseBuilder withCustomField(String name, Object value) {
            builder.customField(name, value);
            return this;
        }

        @Override
        public NewTestCaseBuilder withCustomFields(Map<String, Object> customFields) {
            builder.customFields(customFields);
            return this;
        }

        @Override
        public CreateTestCaseResponse create() {
            return ZephyrProjectApiClient.this.createTestCase(builder.build());
        }
    }

    private class ConcreteUpdateTestCaseBuilder implements UpdateTestCaseBuilder {
        private final ZephyrTestCase.Builder builder;
        private final String testCaseKey;

        public ConcreteUpdateTestCaseBuilder(String testCaseKey) {
            this.testCaseKey = testCaseKey;
            this.builder = ZephyrTestCase.builder().key(testCaseKey);
        }

        @Override
        public UpdateTestCaseBuilder withName(String name) {
            builder.name(name);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withObjective(String objective) {
            builder.objective(objective);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withPrecondition(String precondition) {
            builder.precondition(precondition);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withLabel(String label) {
            builder.label(label);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withLabels(List<String> labels) {
            builder.labels(labels);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withPriorityName(String priorityName) {
            builder.priorityName(priorityName);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withStatusName(String statusName) {
            builder.statusName(statusName);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withFolderId(Long folderId) {
            builder.folderId(folderId);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withStep(String step) {
            builder.step(step);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withSteps(List<String> steps) {
            builder.steps(steps);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withCustomField(String name, Object value) {
            builder.customField(name, value);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder withCustomFields(Map<String, Object> customFields) {
            builder.customFields(customFields);
            return this;
        }

        @Override
        public UpdateTestCaseResponse update() {
            return ZephyrProjectApiClient.this.updateTestCase(testCaseKey, builder.build());
        }
    }

    private class ConcreteNewFolderBuilder implements NewFolderBuilder {
        private final ZephyrTestCaseFolder.Builder builder = ZephyrTestCaseFolder.builder();

        @Override
        public NewFolderBuilder withName(String name) {
            builder.name(name);
            return this;
        }

        @Override
        public NewFolderBuilder withParentId(Long parentId) {
            builder.parentId(parentId);
            return this;
        }

        @Override
        public NewFolderBuilder withFolderType(String folderType) {
            builder.folderType(folderType);
            return this;
        }

        @Override
        public NewFolderBuilder withPath(String folderPath) {
            builder.path(folderPath);
            return this;
        }

        @Override
        public CreateFolderResponse create() {
            return ZephyrProjectApiClient.this.createTestCaseFolder(builder.build());
        }
    }

    private ZephyrProjectClientCache useCache() {
        if (clientCache == null) {
            clientCache = new ZephyrProjectClientCache();
            refreshAllCache();
        }
        return clientCache;
    }

    private void refreshStatusesCache() {
        GetStatusesResponse response = getStatuses();
        if (!response.isSuccessful()) {
            System.err.println("Failed to refresh cache for test cases statuses: " + response.getErrorMessage());
        }
    }

    private void refreshPrioritiesCache() {
        GetPrioritiesResponse response = getPriorities();
        if (!response.isSuccessful()) {
            System.err.println("Failed to refresh cache for test cases priorities: " + response.getErrorMessage());
        }
    }

    private void refreshFoldersCache() {
        GetFoldersResponse response = getFolders();
        if (!response.isSuccessful()) {
            System.err.println("Failed to refresh cache for test case folders: " + response.getErrorMessage());
        } else if (clientCache != null) {
            clientCache.setFoldersCache(response.getFolders());
        }
    }

    private void refreshProjectCache() {
        GetProjectResponse response = getProject();
        if (!response.isSuccessful()) {
            System.err.println("Failed to refresh cache for project: " + response.getErrorMessage());
        } else if (clientCache != null) {
            clientCache.setProject(response.getProject());
        }
    }

    private void refreshAllCache() {
        refreshProjectCache();
        refreshStatusesCache();
        refreshPrioritiesCache();
        refreshFoldersCache();
    }
}
