package io.github.bobfrostman.zephyr.entity;

import java.util.Objects;

public class ZephyrTestCaseFolder {

    private Long id;
    private String name;
    private String path;
    private String folderType;
    private Long parentId;

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

    public String getFolderType() {
        return folderType;
    }

    public void setFolderType(String folderType) {
        this.folderType = folderType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZephyrTestCaseFolder that = (ZephyrTestCaseFolder) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(path, that.path) && Objects.equals(folderType, that.folderType) && Objects.equals(parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, path, folderType, parentId);
    }

    @Override
    public String toString() {
        return "ZephyrTestCaseFolder{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", folderType='" + folderType + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
