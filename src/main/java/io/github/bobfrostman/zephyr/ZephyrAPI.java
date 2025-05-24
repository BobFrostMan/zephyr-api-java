package io.github.bobfrostman.zephyr;

import io.github.bobfrostman.zephyr.client.ZephyrProjectClientBuilder;

public final class ZephyrAPI {

    public static ZephyrProjectClientBuilder createClient() {
        return new ZephyrProjectClientBuilder().withApiUrl("https://api.zephyrscale.smartbear.com/v2");
    }
}
