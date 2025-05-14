package io.github.bobfrostman.zephyr.client.cache;

import io.github.bobfrostman.zephyr.entity.ZephyrProject;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseFolder;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCasePriority;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseStatus;

import java.util.List;

public class ZephyrCacheProvider {

    private static final ThreadLocal<ZephyrProjectClientCache> cacheMap = new ThreadLocal<>();

    static ZephyrProjectClientCache getCache() {
        return cacheMap.get();
    }

    static ZephyrProjectClientCache setCache(List<ZephyrTestCaseStatus> statuses, List<ZephyrTestCasePriority> priorities, List<ZephyrTestCaseFolder> folders, ZephyrProject project) {
        ZephyrProjectClientCache cache = new ZephyrProjectClientCache();
        cache.setTestCaseStatuses(statuses);
        cache.setPrioritiesCache(priorities);
        cache.setProject(project);
        cache.setFoldersCache(folders);
        cacheMap.set(cache);
        return cache;
    }
}
