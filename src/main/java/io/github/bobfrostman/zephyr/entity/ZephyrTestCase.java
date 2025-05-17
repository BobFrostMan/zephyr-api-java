package io.github.bobfrostman.zephyr.entity;

import java.util.*;

public class ZephyrTestCase {

    private Long id;
    private String key;
    private String name;
    private String projectKey;
    private String objective;
    private String precondition;
    private List<String> labels;
    private String priorityName;
    private String statusName;
    private String path;
    private Long folderId;
    private List<String> steps;
    private Map<String, Object> customFields;

    private ZephyrTestCase(Builder builder) {
        this.id = builder.id;
        this.key = builder.key;
        this.name = builder.name;
        this.projectKey = builder.projectKey;
        this.objective = builder.objective;
        this.precondition = builder.precondition;
        this.labels = builder.labels;
        this.priorityName = builder.priorityName;
        this.statusName = builder.statusName;
        this.path = builder.path;
        this.customFields = builder.customFields;
        this.steps = builder.steps;
        this.folderId = builder.folderId;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getObjective() {
        return objective;
    }

    public String getPrecondition() {
        return precondition;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getPath() {
        return path;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public List<String> getSteps() {
        return this.steps;
    }

    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    public Long getId() {
        return id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String key;
        private String name;
        private String projectKey;
        private String objective;
        private String precondition;
        private List<String> labels = new ArrayList<>();
        private String priorityName;
        private String statusName;
        private String path;
        private Long folderId;
        private List<String> steps = new ArrayList<>();
        private Map<String, Object> customFields = new HashMap<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder projectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public Builder objective(String objective) {
            this.objective = objective;
            return this;
        }

        public Builder precondition(String precondition) {
            this.precondition = precondition;
            return this;
        }

        public Builder label(String label) {
            this.labels.add(label);
            return this;
        }

        public Builder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder steps(List<String> steps) {
            this.steps = steps;
            return this;
        }

        public Builder step(String step) {
            steps.add(step);
            return this;
        }

        public Builder priorityName(String priorityName) {
            this.priorityName = priorityName;
            return this;
        }

        public Builder statusName(String statusName) {
            this.statusName = statusName;
            return this;
        }

        public Builder path(String folderName) {
            this.path = folderName;
            return this;
        }

        public Builder folderId(Long folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder customField(String key, Object value) {
            this.customFields.put(key, value);
            return this;
        }

        public Builder customFields(Map<String, Object> customFields) {
            this.customFields = customFields;
            return this;
        }

        public ZephyrTestCase build() {
            return new ZephyrTestCase(this);
        }
    }

}