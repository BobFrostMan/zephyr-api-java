package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.github.bobfrostman.zephyr.client.cache.ZephyrProjectClientCache;
import io.github.bobfrostman.zephyr.entity.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ZephyrResponseParser {

    private static final Map<Class<?>, BiFunction<JsonObject, ZephyrProjectClientCache, ?>> PARSER_MAP = new HashMap<>();

    static {
        PARSER_MAP.put(ZephyrTestCase.class, ZephyrResponseParser::parseTestCase);
        PARSER_MAP.put(ZephyrTestCaseStatus.class, ZephyrResponseParser::parseStatus);
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
        String priorityName = priorityId != -1 ? cache.getPriorities().stream().filter(item -> item.getId() == priorityId).collect(Collectors.toList()).get(0).getName() : null;
        String statusName = statusId != -1 ? cache.getStatuses().stream().filter(item -> item.getId() == statusId).collect(Collectors.toList()).get(0).getName() : null;
        String path = folderId != -1 ? resolveTestCaseFolderPath(folderId, cache.getFoldersAsMap()) : null;
        List<String> labels = jsonResponseObject.get("labels").asArray().values().stream().map(JsonValue::asString).collect(Collectors.toList());
        return ZephyrTestCase.builder()
                .id(jsonResponseObject.getLong("id", -1))
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

    static List<ZephyrTestCaseFolder> parseFolders(JsonArray jsonArray) {
        List<ZephyrTestCaseFolder> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonFolderObject = jsonArray.get(i).asObject();
            ZephyrTestCaseFolder folder = new ZephyrTestCaseFolder();
            folder.setFolderType(jsonFolderObject.getString("folderType"));
            folder.setId(jsonFolderObject.getLong("id", -1));
            folder.setName(jsonFolderObject.getString("name"));
            folder.setParentId(jsonFolderObject.get("parentId").isNull() ? null : jsonFolderObject.getLong("parentId", -1));
            folder.setPath(resolveFolderPath(jsonFolderObject, jsonArray));
            result.add(folder);
        }
        return result;
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
            List<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(jsonScriptObject.getString("text").split("\\n")));
            script.setSteps(list);
        }
        return script;
    }

    private static Map<String, Object> parseCustomFields(JsonObject jsonCustomFieldsObject) {
        Map<String, Object> customFields = new HashMap<>();
        for (int i = 0; i < jsonCustomFieldsObject.names().size(); i++) {
            String name = jsonCustomFieldsObject.names().get(i);
            customFields.put(name, jsonCustomFieldsObject.get(name) != null ? jsonCustomFieldsObject.get(name).asString() : null);
        }
        return customFields;
    }

    static String resolveTestCaseFolderPath(Long folderId, Map<Long, ZephyrTestCaseFolder> foldersMap) {
        if (folderId == null || folderId == -1) {
            return "/";
        }
        LinkedList<String> pathSegments = new LinkedList<>();

        Long currentFolderId = folderId;
        while (currentFolderId != null && foldersMap.containsKey(currentFolderId)) {
            ZephyrTestCaseFolder currentFolder = foldersMap.get(currentFolderId);
            pathSegments.addFirst(currentFolder.getName());
            currentFolderId = currentFolder.getParentId();
        }
        return "/" + String.join("/", pathSegments);
    }

    static String resolveFolderPath(JsonObject jsonFolderObject, JsonArray jsonArray) {
        String name = jsonFolderObject.getString("name");
        String jsonPath = "/" + name + "/";
        JsonObject parent = getParentById(jsonFolderObject.get("parentId").isNull() ? null : jsonFolderObject.getLong("parentId", -1), jsonArray);
        while (parent != null) {
            jsonPath = "/" + parent.getString("name") + jsonPath;
            Long parentId = parent.get("parentId").isNull() ? null : parent.getLong("parentId", -1);
            parent = getParentById(parentId, jsonArray);
        }
        return jsonPath;
    }

    static JsonObject getParentById(Long id, JsonArray jsonArray) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            if (id.equals(jsonArray.get(i).asObject().getLong("id", -1L))) {
                return jsonArray.get(i).asObject();
            }
        }
        return null;
    }

}