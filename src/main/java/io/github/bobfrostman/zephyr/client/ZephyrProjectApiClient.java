package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.github.bobfrostman.zephyr.client.builder.NewFolderBuilder;
import io.github.bobfrostman.zephyr.client.builder.NewTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.builder.UpdateTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.cache.ZephyrProjectClientCache;
import io.github.bobfrostman.zephyr.client.response.*;
import io.github.bobfrostman.zephyr.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.bobfrostman.zephyr.client.ZephyrResponseParser.*;

public class ZephyrProjectApiClient implements IZephyrProjectApiClient {

    private static final String PROJECT_REQUEST_PARAMS = "%s%s?projectKey=%s&maxResults=1000";
    private static final String PROJECT_FOLDER_REQUEST_PARAMS = "%s%s?projectKey=%s&folderId=%s&maxResults=1000";

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
    public GetTestCasesResponse getTestCases() {
        return getTestCases("/");
    }

    @Override
    public GetTestCasesResponse getTestCases(boolean includeSteps) {
        return getTestCases("/", includeSteps);
    }

    @Override
    public GetTestCasesResponse getTestCases(String folder, boolean includeSteps) {
        ZephyrProjectClientCache cache = useCache();
        final String searchFolder = !folder.endsWith("/") ? folder + "/" : folder;
        List<ZephyrTestCaseFolder> folders = cache.getFoldersAsMap().values().stream().filter(e -> e.getPath().equals(searchFolder)).collect(Collectors.toList());
        if (folders.isEmpty()) {
            return new GetTestCasesResponse(200, new ArrayList<>(), null);
        }
        String url = searchFolder.equals("/")
                ? String.format(PROJECT_REQUEST_PARAMS, apiUrl, "testcases", projectKey)
                : String.format(PROJECT_FOLDER_REQUEST_PARAMS, apiUrl, "testcases", projectKey, folders.get(0).getId());
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();

            if (statusCode >= 200 && statusCode < 300) {
                JsonObject jsonResponse = Json.parse(responseBody).asObject();
                JsonArray valuesArray = jsonResponse.get("values").asArray();
                List<ZephyrTestCase> testCases = new ArrayList<>();
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
    public GetTestCasesResponse getTestCases(String folder) {
        return getTestCases(folder, true);
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
        return getTestCase(testCaseKey, true);
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
                    .withId(tc.getId())
                    .withName(tc.getName())
                    .withObjective(tc.getObjective())
                    .withFolderId(tc.getFolderId())
                    .withFolderPath(tc.getPath())
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
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "statuses", projectKey) + "&statusType=TEST_CASE";
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCaseStatus> statuses = parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCaseStatus.class, clientCache);
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
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "priorities", projectKey);
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCasePriority> priorities = parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCasePriority.class, clientCache);

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
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "folders", projectKey) + "&folderType=TEST_CASE";
        try {
            ApiResponse response = BasicApiClient.executeGet(url, token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            JsonObject jsonResponse = Json.parse(responseBody).asObject();
            List<ZephyrTestCaseFolder> folders = parseFolders(jsonResponse.asObject().get("values").asArray());
            if (statusCode >= 200 && statusCode < 300) {
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
            Long folderId = createFolderByPathIfNotExists(testCase.getPath(), cache);
            object.add("folderId", folderId);
        }
        JsonObject customFields = Json.object();
        for (Map.Entry<String, Object> entry : testCase.getCustomFields().entrySet()) {
            customFields.add(entry.getKey(), String.valueOf(entry.getValue()));
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
                        .id(jsonResponse.getLong("id", -1))
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

    private Long createFolderByPathIfNotExists(String path, ZephyrProjectClientCache cache) {
        List<ZephyrTestCaseFolder> folderList = cache.getFolders().stream().filter(folder -> path.equals(folder.getPath())).collect(Collectors.toList());
        if (!folderList.isEmpty()) {
            return folderList.get(0).getId();
        } else {
            Long parentId = null;
            for (String name : path.split("/")) {
                name = name.trim();
                if (name.isEmpty()) {
                    continue;
                }
                Long id = getFolderByName(name, parentId, cache);
                if (id == null) {
                    id = createTestCaseFolder(ZephyrTestCaseFolder.builder().name(name).parentId(parentId).build()).getCreatedFolder().getId();
                }
                parentId = id;
            }
            return parentId;
        }
    }

    private Long getFolderByName(String name, Long parentId, ZephyrProjectClientCache cache) {
        for (ZephyrTestCaseFolder folder : cache.getFolders()) {
            if (folder.getName().equals(name)) {
                if (parentId != null && parentId.equals(folder.getParentId())) {
                    return folder.getId();
                }
                if (parentId == null && folder.getParentId() == null) {
                    return folder.getId();
                }
            }
        }
        return null;
    }

    private UpdateTestCaseResponse updateTestCase(String testCaseKey, ZephyrTestCase testCase) {
        String url = String.format("%stestcases/%s", apiUrl, testCaseKey);
        ZephyrProjectClientCache cache = useCache();
        JsonObject object = Json.object()
                .add("key", testCaseKey)
                .add("id", testCase.getId())
                .add("project", Json.object().add("id", cache.getProject().getId()))
                .add("name", testCase.getName())
                .add("objective", testCase.getObjective())
                .add("priority", Json.object().add("id", cache.getPriorities().stream().filter(p -> p.getName().equals(testCase.getPriorityName())).collect(Collectors.toList()).get(0).getId()))
                .add("status", Json.object().add("id", cache.getStatuses().stream().filter(s -> s.getName().equals(testCase.getStatusName())).collect(Collectors.toList()).get(0).getId()))
                .add("labels", Json.parse(testCase.getLabels().toString()));

        if (testCase.getFolderId() == null || testCase.getPath() != null) {
            Long folderId = createFolderByPathIfNotExists(testCase.getPath(), cache);
            object.add("folder", Json.object().add("id", folderId));
        } else {
            object.add("folder", Json.object().add("id", testCase.getFolderId()));
        }

        if (testCase.getCustomFields() != null) {
            object.add("customFields", toJsonCustomFields(testCase.getCustomFields()));
        } else {
            object.add("customFields", Json.NULL);
        }
        try {
            ApiResponse response = BasicApiClient.executePut(url, object.toString(), token);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();
            if (statusCode >= 200 && statusCode < 300) {
                ZephyrTestCase tc = getTestCase(testCaseKey).getTestCase();
                if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
                    CreateTestStepsResponse stepsResponse = createTestScript(tc.getKey(), "bdd", testCase.getSteps());
                    if (!stepsResponse.isSuccessful()) {
                        throw new RuntimeException("Cannot create test script steps: " + stepsResponse.getErrorMessage());
                    } else {
                        tc.setSteps(getTestSteps(tc.getKey()).getTestScript().getSteps());
                        refreshFoldersCache();
                    }
                }
                return new UpdateTestCaseResponse(statusCode, tc, null);
            } else {
                return new UpdateTestCaseResponse(statusCode, null, responseBody);
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
                        .path(resolveTestCaseFolderPath(jsonResponse.getLong("id", -1), cache.getFoldersAsMap()))
                        .name(folder.getName()).build();
                refreshFoldersCache();
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
        public UpdateTestCaseBuilder withId(Long id) {
            builder.id(id);
            return this;
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
        public UpdateTestCaseBuilder withFolderPath(String folderPath) {
            builder.path(folderPath);
            return this;
        }

        @Override
        public UpdateTestCaseBuilder addStep(String step) {
            builder.step(step);
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
            Map<Long, ZephyrTestCaseFolder> folderMap = new HashMap<>();
            for (ZephyrTestCaseFolder folder : response.getFolders()) {
                folderMap.put(folder.getId(), folder);
            }
            clientCache.setFoldersMapCache(folderMap);
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

    static JsonObject toJsonCustomFields(Map<String, Object> customFieldsMap) {
        JsonObject customFieldsJson = new JsonObject();
        if (customFieldsMap != null) {
            for (Map.Entry<String, Object> entry : customFieldsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                customFieldsJson.add(key, convertJavaObjectToJsonValue(value));
            }
        }
        return customFieldsJson;
    }

    private static JsonValue convertJavaObjectToJsonValue(Object obj) {
        if (obj == null) {
            return JsonValue.NULL;
        } else if (obj instanceof String) {
            return Json.value((String) obj);
        } else if (obj instanceof Integer) {
            return Json.value((Integer) obj);
        } else if (obj instanceof Long) {
            return Json.value((Long) obj);
        } else if (obj instanceof Double) {
            return Json.value((Double) obj);
        } else if (obj instanceof Float) {
            return Json.value((Float) obj);
        } else if (obj instanceof Boolean) {
            return Json.value((Boolean) obj);
        } else if (obj instanceof List) {
            JsonArray jsonArray = new JsonArray();
            for (Object item : (List<?>) obj) {
                jsonArray.add(convertJavaObjectToJsonValue(item));
            }
            return jsonArray;
        } else if (obj instanceof Map) {
            return toJsonCustomFields((Map<String, Object>) obj);
        } else {
            return Json.value(obj.toString());
        }
    }

}
