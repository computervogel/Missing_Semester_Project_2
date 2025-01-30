package main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;

public class PictureGeneration {
    private static final String LOCALAI_IMAGE_ENDPOINT = "http://localhost:8080/v1/images/generations";
    private static final String LOCALAI_CHAT_ENDPOINT = "http://localhost:8080/v1/chat/completions";

    public static void main(String[] args) {
        String keywords = "Correct, Horse, Battery, Staple";
        String negativePrompt = "Text";

        // Generate prompt using LLM
        String generatedPrompt = callLLMForPrompt(keywords);
        System.out.println(generatedPrompt);

        if (generatedPrompt == null) {
            System.out.println("Using fallback keywords due to LLM failure");
            generatedPrompt = keywords;
        }

        String imageUrl = callLocalAIAndReturnImage(generatedPrompt, negativePrompt);

        if (imageUrl != null) {
            System.out.println("Image URL: " + imageUrl);
            saveImage(imageUrl, "generated_image.png");
        } else {
            System.out.println("Failed to generate image.");
        }
    }

    private static String callLLMForPrompt(String keywords) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Create chat messages
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Generate a stabel diffusion image generation prompt that depicts these concepts: "
                + keywords + ".");

        JsonArray messages = new JsonArray();
        messages.add(message);

        // Create request body
        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("model", "gpt-4"); // Adjust based on your local model
        requestBodyJson.add("messages", messages);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson.toString());

        Request request = new Request.Builder()
                .url(LOCALAI_CHAT_ENDPOINT)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                // Extract generated prompt from response
                return responseJson.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .get("message").getAsJsonObject()
                        .get("content").getAsString();
            } else {
                System.out.println("LLM API failed: " + response.code());
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error calling LLM:");
            e.printStackTrace();
            return null;
        }
    }

    private static String callLocalAIAndReturnImage(String prompt, String negativePrompt) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("prompt", prompt + "|" + negativePrompt);
        requestBodyJson.addProperty("model", "stablediffusion");
        requestBodyJson.addProperty("size", "512x512");

        //RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), MediaType.parse("application/json"));
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson.toString());
        Request request = new Request.Builder()
                .url(LOCALAI_IMAGE_ENDPOINT)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                return responseJson.getAsJsonArray("data")
                        .get(0).getAsJsonObject()
                        .get("url").getAsString();
            } else {
                System.out.println("Image API failed: " + response.body().string());
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error generating image:");
            e.printStackTrace();
            return null;
        }
    }

    private static void saveImage(String imageUrl, String destinationFile) {
        try {
            URI uri = new URI(imageUrl);
            BufferedImage image = ImageIO.read(uri.toURL());
            File outputFile = new File(destinationFile);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved as: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save image:");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}