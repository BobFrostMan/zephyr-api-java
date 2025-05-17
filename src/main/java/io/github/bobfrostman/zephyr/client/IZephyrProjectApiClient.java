package io.github.bobfrostman.zephyr.client;

import io.github.bobfrostman.zephyr.client.builder.NewFolderBuilder;
import io.github.bobfrostman.zephyr.client.builder.NewTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.builder.UpdateTestCaseBuilder;
import io.github.bobfrostman.zephyr.client.response.*;

public interface IZephyrProjectApiClient {

    GetTestCasesResponse getTestCases();

    GetTestCasesResponse getTestCases(boolean includeSteps);

    GetTestCaseResponse getTestCase(String testCaseKey);

    GetTestCaseResponse getTestCase(String testCaseKey, boolean includeSteps);

    NewTestCaseBuilder newTestCase();

    UpdateTestCaseBuilder updateTestCase(String testCaseKey);

    NewFolderBuilder newTestCaseFolder();

    GetStatusesResponse getStatuses();

    GetPrioritiesResponse getPriorities();

    GetFoldersResponse getFolders();

    GetTestStepsResponse getTestSteps(String testCaseKey);

    GetProjectResponse getProject();

}