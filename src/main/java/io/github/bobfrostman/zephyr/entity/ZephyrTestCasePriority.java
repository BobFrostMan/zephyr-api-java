package io.github.bobfrostman.zephyr.entity;

import java.util.Objects;

public class ZephyrTestCasePriority {

    private Long id;
    private String name;
    private String description;
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
        ZephyrTestCasePriority priority = (ZephyrTestCasePriority) o;
        return isDefault == priority.isDefault && Objects.equals(id, priority.id) && Objects.equals(name, priority.name) && Objects.equals(description, priority.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isDefault);
    }

    @Override
    public String toString() {
        return "ZephyrTestCasePriority{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
