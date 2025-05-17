package io.github.bobfrostman.zephyr.entity;

import java.util.Objects;

public class ZephyrProject {

    private Long id;
    private String key;
    private Long jiraProjectId;
    private boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZephyrProject that = (ZephyrProject) o;
        return enabled == that.enabled && Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(jiraProjectId, that.jiraProjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, jiraProjectId, enabled);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public void setJiraProjectId(Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
