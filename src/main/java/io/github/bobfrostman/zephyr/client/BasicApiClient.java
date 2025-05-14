package io.github.bobfrostman.zephyr.client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.WriterConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BasicApiClient {

    private static boolean verbose;

    static void setVerbose(boolean isVerboseOutput) {
        verbose = isVerboseOutput;
    }

    static String executeGet(String url, String bearerToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);

        return getResponse(con);
    }

     static String executePost(String url, String requestBody, String bearerToken) throws IOException {
        return executeRequest("POST", url, requestBody, bearerToken);
    }

     static String executePut(String url, String requestBody, String bearerToken) throws IOException {
        return executeRequest("PUT", url, requestBody, bearerToken);
    }

     static String executeDelete(String url, String bearerToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);

        return getResponse(con);
    }

    private static String executeRequest(String method, String url, String requestBody, String bearerToken) throws IOException {
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

        return getResponse(con);
    }

    private static String getResponse(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        if (verbose) {
            System.out.println(con.getRequestMethod() + " " + con.getURL().toString());
            System.out.println("Response code: " + responseCode);
        }
        StringBuilder response = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            // Handle cases where there's no regular response body (e.g., 204 No Content)
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    response.append(errorLine);
                }
            } catch (NullPointerException ignored) {
                // No error stream
            }
        } finally {
            con.disconnect();
        }
        String respString = response.toString();
        StringWriter str = new StringWriter();
        Json.parse(respString).writeTo(str, WriterConfig.PRETTY_PRINT);
        if (verbose) {
            System.out.println("Response Body:\n" + str);
        }
        return respString;
    }
}
