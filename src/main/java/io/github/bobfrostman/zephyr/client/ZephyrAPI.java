package io.github.bobfrostman.zephyr.client;

public class ZephyrAPI {

    //TODO: implement builder with verbose include all resolved entities
    public static IZephyrProjectApiClient projectClient(String bearerToken, String projectName) {
        return new ZephyrScaleProjectApiClient().createProjectClient("https://api.zephyrscale.smartbear.com/v2", bearerToken, projectName, false);
    }

    public static IZephyrProjectApiClient projectClient(String baseUrl, String bearerToken, String projectName) {
        return new ZephyrScaleProjectApiClient().createProjectClient(baseUrl, bearerToken, projectName, false);
    }

    public static IZephyrProjectApiClient projectClient(String baseUrl, String bearerToken, String projectName, boolean verbose) {
        return new ZephyrScaleProjectApiClient().createProjectClient(baseUrl, bearerToken, projectName, verbose);
    }
}
