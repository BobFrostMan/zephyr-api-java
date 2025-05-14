package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.github.bobfrostman.zephyr.client.cache.IZephyrCacheProvider;
import io.github.bobfrostman.zephyr.client.cache.ZephyrProjectClientCache;
import io.github.bobfrostman.zephyr.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static io.github.bobfrostman.zephyr.client.BasicApiClient.executeGet;
import static io.github.bobfrostman.zephyr.client.BasicApiClient.setVerbose;
import static io.github.bobfrostman.zephyr.client.ZephyrResponseParser.getParserFunction;
import static io.github.bobfrostman.zephyr.client.ZephyrResponseParser.parseValues;

public class ZephyrScaleProjectApiClient implements IZephyrProjectApiClient, IZephyrCacheProvider {


    private static final String PROJECT_REQUEST_PARAMS = "%s%s?projectKey=%s&maxResults=1000";

    private String apiUrl;
    private String token;
    private String projectKey;

    private ZephyrProjectClientCache clientCache;

    ZephyrScaleProjectApiClient() {
        //no need to introduce internal constructors
    }

    @Override
    public IZephyrProjectApiClient createProjectClient(String url, String token, String projectKey, boolean verbose) {
        this.apiUrl = url.endsWith("/") ? url : url + "/";
        this.token = token;
        this.projectKey = projectKey;
        setVerbose(verbose);
        return this;
    }

    @Override
    public List<ZephyrTestCase> getTestCases() {
       return getTestCases(true);
    }

    @Override
    public List<ZephyrTestCase> getTestCases(boolean includeSteps) {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "testcases", projectKey);
        BiFunction<JsonObject, ZephyrProjectClientCache, ZephyrTestCase> parser = getParserFunction(ZephyrTestCase.class);
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            JsonArray valuesArray = jsonResponse.asObject().get("values").asArray();
            List<ZephyrTestCase> result = new ArrayList<>();
            for (int i = 0; i < valuesArray.size(); i++) {
                ZephyrTestCase testCase = parser.apply(valuesArray.get(i).asObject(), useCache());
                if (includeSteps) {
                    try {
                        testCase.setSteps(getTestSteps(testCase.getKey()).getSteps());
                    } catch (Throwable ignored) {
                        //do nothing if wrong type to parse
                    }
                }
                result.add(testCase);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Cannot get testcases from request to " + url, e);
        }
    }

    @Override
    public ZephyrTestCase getTestCase(String testCaseKey) {
        return getTestCase(testCaseKey, true);
    }

    @Override
    public ZephyrTestCase getTestCase(String testCaseKey, boolean includeSteps) {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "testcases/" + testCaseKey, projectKey);
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            ZephyrTestCase testCase = getParserFunction(ZephyrTestCase.class).apply(jsonResponse.asObject(), useCache());
            if (includeSteps) {
                testCase.setSteps(getTestSteps(testCase.getKey()).getSteps());
            }
            return testCase;
        } catch (IOException e) {
            throw new RuntimeException("Cannot get testcase from request to " + url, e);
        }
    }

    @Override
    public ZephyrTestCase.Builder createTestCase() {
        return null;
    }

    @Override
    public ZephyrTestScript createTestScript() {
        return null;
    }

    @Override
    public ZephyrTestCase.Builder updateTestCase(String testCaseKey) {
        return null;
    }

    @Override
    public List<ZephyrTestCaseStatus> getStatuses() {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "statuses", projectKey);
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            return parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCaseStatus.class, clientCache);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get statuses from request to " + url, e);
        }
    }

    @Override
    public List<ZephyrTestCasePriority> getPriorities() {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "priorities", projectKey);
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            return parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCasePriority.class, clientCache);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get priorities from request to " + url, e);
        }
    }

    @Override
    public List<ZephyrTestCaseFolder> getFolders() {
        String url = String.format(PROJECT_REQUEST_PARAMS, apiUrl, "folders", projectKey) + "&folderType=TEST_CASE";
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            return parseValues(jsonResponse.asObject().get("values").asArray(), ZephyrTestCaseFolder.class, clientCache);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get folders from request to " + url, e);
        }
    }

    @Override
    public ZephyrTestScript getTestSteps(String testCaseKey) {
        String url = apiUrl + "testcases/" + testCaseKey + "/testscript";
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            return getParserFunction(ZephyrTestScript.class).apply(jsonResponse.asObject(), useCache());
        } catch (IOException e) {
            throw new RuntimeException("Cannot get test steps (testscript) from request to " + url, e);
        }
    }

    @Override
    public ZephyrProject getProject() {
        String url = apiUrl + "projects/" + projectKey;
        try {
            JsonValue jsonResponse = Json.parse(executeGet(url, token));
            return getParserFunction(ZephyrProject.class).apply(jsonResponse.asObject(), clientCache);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get test project from request to " + url, e);
        }
    }

    private ZephyrProjectClientCache updateCache() {
        return setCache(getStatuses(), getPriorities(), getFolders(), getProject());
    }

    private ZephyrProjectClientCache useCache() {
        if (clientCache == null) {
            clientCache = updateCache();
        }
        return clientCache;
    }

}
