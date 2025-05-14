package io.github.bobfrostman.zephyr.client;

import io.github.bobfrostman.zephyr.client.response.*;

import java.util.List;
import java.util.Map;

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

interface NewTestCaseBuilder {
    NewTestCaseBuilder withName(String name);

    NewTestCaseBuilder withObjective(String objective);

    NewTestCaseBuilder withPrecondition(String precondition);

    NewTestCaseBuilder withLabel(String label);

    NewTestCaseBuilder withLabels(List<String> labels);

    NewTestCaseBuilder withPriorityName(String priorityName);

    NewTestCaseBuilder withStatusName(String statusName);

    NewTestCaseBuilder withFolderId(Long folderId);

    NewTestCaseBuilder withStep(String step);

    NewTestCaseBuilder withSteps(List<String> steps);

    NewTestCaseBuilder withCustomField(String name, Object value);

    NewTestCaseBuilder withCustomFields(Map<String, Object> customFields);

    CreateTestCaseResponse create();
}

interface UpdateTestCaseBuilder {
    UpdateTestCaseBuilder withName(String name);

    UpdateTestCaseBuilder withObjective(String objective);

    UpdateTestCaseBuilder withPrecondition(String precondition);

    UpdateTestCaseBuilder withLabel(String label);

    UpdateTestCaseBuilder withLabels(List<String> labels);

    UpdateTestCaseBuilder withPriorityName(String priorityName);

    UpdateTestCaseBuilder withStatusName(String statusName);

    UpdateTestCaseBuilder withFolderId(Long folderId);

    UpdateTestCaseBuilder withStep(String step);

    UpdateTestCaseBuilder withSteps(List<String> steps);

    UpdateTestCaseBuilder withCustomField(String name, Object value);

    UpdateTestCaseBuilder withCustomFields(Map<String, Object> customFields);

    UpdateTestCaseResponse update();
}

interface NewFolderBuilder {
    NewFolderBuilder withName(String name);

    NewFolderBuilder withParentId(Long parentId);

    NewFolderBuilder withFolderType(String folderType);

    NewFolderBuilder withPath(String folderPath);

    CreateFolderResponse create();
}