package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.github.bobfrostman.zephyr.client.cache.ZephyrProjectClientCache;
import io.github.bobfrostman.zephyr.entity.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

public class ZephyrResponseParser {

    private static final Map<Class<?>, BiFunction<JsonObject, ZephyrProjectClientCache, ?>> PARSER_MAP = new HashMap<>();

    static {
        PARSER_MAP.put(ZephyrTestCase.class, ZephyrResponseParser::parseTestCase);
        PARSER_MAP.put(ZephyrTestCaseStatus.class, ZephyrResponseParser::parseStatus);
        PARSER_MAP.put(ZephyrTestCaseFolder.class, ZephyrResponseParser::parseFolder);
        PARSER_MAP.put(ZephyrTestCasePriority.class, ZephyrResponseParser::parsePriority);
        PARSER_MAP.put(ZephyrTestScript.class, ZephyrResponseParser::parseTestScript);
        PARSER_MAP.put(ZephyrProject.class, ZephyrResponseParser::parseProject);
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> parseValues(JsonArray jsonArray, Class<?> clazz, ZephyrProjectClientCache cache) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            T item = (T) getParserFunction(clazz).apply(jsonArray.get(i).asObject(), cache);
            result.add(item);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static <T> BiFunction<JsonObject, ZephyrProjectClientCache, T> getParserFunction(Class<T> clazz) {
        BiFunction<JsonObject, ZephyrProjectClientCache, ?> parser = PARSER_MAP.get(clazz);
        if (parser == null) {
            throw new RuntimeException("Class parsing " + clazz.getSimpleName() + " is not implemented");
        }
        return (BiFunction<JsonObject, ZephyrProjectClientCache, T>) parser;
    }

    static ZephyrTestCase parseTestCase(JsonObject jsonResponseObject, ZephyrProjectClientCache cache) {
        final long priorityId = jsonResponseObject.get("priority").asObject().getLong("id", -1);
        final long statusId = jsonResponseObject.get("status").asObject().getLong("id", -1);
        final long folderId = jsonResponseObject.get("folder").isNull() ? -1 : jsonResponseObject.get("folder").asObject().getLong("id", -1);
        String priorityName = priorityId != -1 ? cache.getPriorities().stream().filter(item -> item.getId() == priorityId).toList().get(0).getName() : null;
        String statusName = statusId != -1 ? cache.getStatuses().stream().filter(item -> item.getId() == statusId).toList().get(0).getName() : null;
        String path = folderId != -1 ? resolvePath(folderId, cache.getFolders()) : null;
        List<String> labels = jsonResponseObject.get("labels").asArray().values().stream().map(JsonValue::asString).toList();

        return ZephyrTestCase.builder()
                .key(jsonResponseObject.getString("key"))
                .projectKey(cache.getProject().getKey())
                .name(jsonResponseObject.getString("name"))
                .objective(jsonResponseObject.get("objective").isNull() ? null : jsonResponseObject.getString("objective"))
                .precondition(jsonResponseObject.get("precondition").isNull() ? null : jsonResponseObject.getString("precondition"))
                .labels(labels)
                .priorityName(priorityName)
                .path(path)
                .folderId(folderId == -1 ? null : folderId)
                .statusName(statusName)
                .priorityName(priorityName)
                .customFields(parseCustomFields(jsonResponseObject.get("customFields").asObject()))
                .build();
    }

    static ZephyrProject parseProject(JsonObject jsonProjectObject, ZephyrProjectClientCache cache) {
        ZephyrProject project = new ZephyrProject();
        project.setId(jsonProjectObject.getLong("id", -1));
        project.setJiraProjectId(jsonProjectObject.getLong("jiraProjectId", -1));
        project.setKey(jsonProjectObject.getString("key"));
        project.setEnabled(jsonProjectObject.getBoolean("enabled", true));
        return project;
    }

    static ZephyrTestCasePriority parsePriority(JsonObject jsonPriorityObject, ZephyrProjectClientCache cache) {
        ZephyrTestCasePriority priority = new ZephyrTestCasePriority();
        priority.setId(jsonPriorityObject.getLong("id", -1));
        priority.setName(jsonPriorityObject.getString("name"));
        priority.setDescription(jsonPriorityObject.get("description").toString());
        return priority;
    }

    static ZephyrTestCaseFolder parseFolder(JsonObject jsonFolderObject, ZephyrProjectClientCache cache) {
        ZephyrTestCaseFolder folder = new ZephyrTestCaseFolder();
        folder.setId(jsonFolderObject.getLong("id", -1));
        folder.setName(jsonFolderObject.getString("name"));
        if (jsonFolderObject.get("parentId").isNull()) {
            folder.setParentId(null);
            folder.setPath("/" + folder.getName() + "/");
        } else {
            folder.setParentId(jsonFolderObject.getLong("parentId", -1));
            if (cache == null) {
                folder.setPath(null);
            } else {
                folder.setPath(resolvePath(folder.getId(), cache.getFolders()));
            }
        }
        folder.setFolderType(jsonFolderObject.getString("folderType"));
        return folder;
    }

    static ZephyrTestCaseStatus parseStatus(JsonObject jsonStatusObject, ZephyrProjectClientCache cache) {
        ZephyrTestCaseStatus status = new ZephyrTestCaseStatus();
        status.setId(jsonStatusObject.getLong("id", -1));
        status.setName(jsonStatusObject.getString("name"));
        status.setDescription(jsonStatusObject.get("description").toString());
        status.setArchived(jsonStatusObject.getBoolean("archived", false));
        status.setDefault(jsonStatusObject.getBoolean("default", false));
        return status;
    }

    static ZephyrTestScript parseTestScript(JsonObject jsonScriptObject, ZephyrProjectClientCache cache) {
        ZephyrTestScript script = new ZephyrTestScript();
        script.setId(jsonScriptObject.getLong("id", -1));
        script.setType(jsonScriptObject.getString("type"));
        if (!jsonScriptObject.get("text").isNull()) {
            script.setSteps(Arrays.stream(jsonScriptObject.getString("text").split("\\n")).toList());
        }
        return script;
    }

    private static Map<String, Object> parseCustomFields(JsonObject jsonCustomFieldsObject) {
        Map<String, Object> customFields = new HashMap<>();
        for (int i = 0; i < jsonCustomFieldsObject.names().size(); i++) {
            String name = jsonCustomFieldsObject.names().get(i);
            customFields.put(name, jsonCustomFieldsObject.get(name).toString());
        }
        return customFields;
    }

    private static String resolvePath(Long tcFolderId, List<ZephyrTestCaseFolder> folders) {
        if (tcFolderId != null) {
            List<ZephyrTestCaseFolder> filteredFolders = folders.stream().filter(item -> tcFolderId.equals(item.getId())).toList();
            if (filteredFolders.isEmpty()) {
                return resolvePath(null, folders);
            } else {
                paths.add(filteredFolders.get(0).getName());
                return resolvePath(filteredFolders.get(0).getParentId(), folders);
            }
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("/");
            for (int i = paths.size() - 1; i >= 0; i--) {
                builder.append(paths.get(i));
                builder.append("/");
            }
            String result = builder.toString();
            paths.clear();
            return result;
        }
    }

    //TODO: find another palce for it
    private static List<String> paths = new CopyOnWriteArrayList<>();


}
