package io.github.bobfrostman.zephyr.client.builder;

import io.github.bobfrostman.zephyr.client.response.CreateFolderResponse;

public interface NewFolderBuilder {
    NewFolderBuilder withName(String name);

    NewFolderBuilder withParentId(Long parentId);

    NewFolderBuilder withFolderType(String folderType);

    NewFolderBuilder withPath(String folderPath);

    CreateFolderResponse create();
}
