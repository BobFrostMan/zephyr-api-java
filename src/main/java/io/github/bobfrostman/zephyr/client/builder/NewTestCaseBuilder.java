package io.github.bobfrostman.zephyr.client.builder;


import io.github.bobfrostman.zephyr.client.response.CreateTestCaseResponse;

import java.util.List;
import java.util.Map;

public interface NewTestCaseBuilder {
    NewTestCaseBuilder withName(String name);

    NewTestCaseBuilder withObjective(String objective);

    NewTestCaseBuilder withPrecondition(String precondition);

    NewTestCaseBuilder withLabel(String label);

    NewTestCaseBuilder withLabels(List<String> labels);

    NewTestCaseBuilder withPriorityName(String priorityName);

    NewTestCaseBuilder withStatusName(String statusName);

    NewTestCaseBuilder withFolderId(Long folderId);

    NewTestCaseBuilder withFolderPath(String folderPath);

    NewTestCaseBuilder withStep(String step);

    NewTestCaseBuilder withSteps(List<String> steps);

    NewTestCaseBuilder withCustomField(String name, Object value);

    NewTestCaseBuilder withCustomFields(Map<String, Object> customFields);

    CreateTestCaseResponse create();
}