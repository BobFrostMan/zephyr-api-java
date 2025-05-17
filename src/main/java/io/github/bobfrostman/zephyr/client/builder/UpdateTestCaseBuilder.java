package io.github.bobfrostman.zephyr.client.builder;

import io.github.bobfrostman.zephyr.client.response.UpdateTestCaseResponse;

import java.util.List;
import java.util.Map;

public interface UpdateTestCaseBuilder {

    UpdateTestCaseBuilder withId(Long id);

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
