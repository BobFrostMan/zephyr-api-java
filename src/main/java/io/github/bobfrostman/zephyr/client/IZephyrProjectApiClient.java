package io.github.bobfrostman.zephyr.client;

import io.github.bobfrostman.zephyr.entity.*;

import java.util.List;

public interface IZephyrProjectApiClient {

    IZephyrProjectApiClient createProjectClient(String url, String token, String projectKey, boolean verbose);

    List<ZephyrTestCase> getTestCases();

    List<ZephyrTestCase> getTestCases(boolean includeSteps);

    ZephyrTestCase getTestCase(String testCaseKey);

    //If builder implemented
    ZephyrTestCase getTestCase(String testCaseKey, boolean includeSteps);

    ZephyrTestCase.Builder createTestCase();

    //TODO: remove for simplicity
    ZephyrTestScript createTestScript();

    ZephyrTestCase.Builder updateTestCase(String testCaseKey);

    List<ZephyrTestCaseStatus> getStatuses();

    List<ZephyrTestCasePriority> getPriorities();

    List<ZephyrTestCaseFolder> getFolders();

    ZephyrTestScript getTestSteps(String testCaseKey);

    ZephyrProject getProject();

}
