package io.github.bobfrostman.zephyr;

import io.github.bobfrostman.zephyr.client.ZephyrProjectClientBuilder;

public class ZephyrAPI {

    public static ZephyrProjectClientBuilder createClient(){
        return new ZephyrProjectClientBuilder();
    }
}
