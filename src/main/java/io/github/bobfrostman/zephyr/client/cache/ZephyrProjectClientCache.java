package io.github.bobfrostman.zephyr.client.cache;

import io.github.bobfrostman.zephyr.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZephyrProjectClientCache {

    private ZephyrProject projectCache;
    private List<ZephyrTestCasePriority> prioritiesCache;
    private List<ZephyrTestCaseStatus> statusesCache;
    private List<ZephyrTestCaseFolder> foldersCache;

    public ZephyrProjectClientCache() {
        statusesCache = new ArrayList<>();
        prioritiesCache = new ArrayList<>();
        foldersCache = new ArrayList<>();
    }

    public void setProject(ZephyrProject project) {
        projectCache = project;
    }

    public void setTestCaseStatuses(List<ZephyrTestCaseStatus> statuses) {
        statusesCache = statuses;
    }

    public void setPrioritiesCache(List<ZephyrTestCasePriority> priorities) {
        prioritiesCache = priorities;
    }

    public ZephyrProject getProject() {
        return projectCache;
    }

    public List<ZephyrTestCasePriority> getPriorities() {
        return prioritiesCache;
    }

    public List<ZephyrTestCaseStatus> getStatuses() {
        return statusesCache;
    }

    public List<ZephyrTestCaseFolder> getFolders() {
        return foldersCache;
    }

    public void setFoldersCache(List<ZephyrTestCaseFolder> foldersCache) {
        this.foldersCache = foldersCache;
    }
}
