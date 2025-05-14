package io.github.bobfrostman.zephyr.client;

import io.github.bobfrostman.zephyr.client.response.ApiResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BasicApiClient {

    private static boolean verbose;

    static void setVerbose(boolean isVerboseOutput) {
        verbose = isVerboseOutput;
    }

    static ApiResponse executeGet(String url, String bearerToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);

        String responseBody = getResponseBody(con);
        int responseCode = con.getResponseCode();
        con.disconnect();

        return new ApiResponse(responseCode, responseBody);
    }

     static ApiResponse executePost(String url, String requestBody, String bearerToken) throws IOException {
        return executeRequest("POST", url, requestBody, bearerToken);
    }

     static ApiResponse executePut(String url, String requestBody, String bearerToken) throws IOException {
        return executeRequest("PUT", url, requestBody, bearerToken);
    }

     static String executeDelete(String url, String bearerToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);

        return getResponseBody(con);
    }

    private static ApiResponse executeRequest(String method, String url, String requestBody, String bearerToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json"); // Or other content type
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        String responseBody = getResponseBody(con);
        int responseCode = con.getResponseCode();
        con.disconnect();
        return new ApiResponse(responseCode, responseBody);
    }

    private static String getResponseBody(HttpURLConnection con) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            InputStream errorStream = con.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        response.append(errorLine);
                    }
                }
            }
        }
        return response.toString();
    }
}
