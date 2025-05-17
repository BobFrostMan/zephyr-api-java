package io.github.bobfrostman.zephyr.entity;

import java.util.Objects;

public class ZephyrTestCaseStatus {

    private Long id;
    private String name;
    private String description;
    private boolean archived;
    private boolean isDefault;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZephyrTestCaseStatus status = (ZephyrTestCaseStatus) o;
        return archived == status.archived && isDefault == status.isDefault && Objects.equals(id, status.id) && Objects.equals(name, status.name) && Objects.equals(description, status.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, archived, isDefault);
    }

    @Override
    public String toString() {
        return "ZephyrTestCaseStatus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", archived=" + archived +
                ", isDefault=" + isDefault +
                '}';
    }
}
