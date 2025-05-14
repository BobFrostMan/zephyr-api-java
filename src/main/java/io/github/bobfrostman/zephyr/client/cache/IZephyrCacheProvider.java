package io.github.bobfrostman.zephyr.client.cache;

import io.github.bobfrostman.zephyr.entity.ZephyrProject;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseFolder;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCasePriority;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseStatus;

import java.util.List;

public interface IZephyrCacheProvider {

    default ZephyrProjectClientCache getCache() {
        return ZephyrCacheProvider.getCache();
    }

    default ZephyrProjectClientCache setCache(List<ZephyrTestCaseStatus> statuses, List<ZephyrTestCasePriority> priorities, List<ZephyrTestCaseFolder> folders, ZephyrProject project){
        return ZephyrCacheProvider.setCache(statuses, priorities, folders, project);
    }

}
