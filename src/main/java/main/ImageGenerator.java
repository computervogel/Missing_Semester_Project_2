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

public class ImageGenerator {
    private String llmEndpoint;
    private String imageEndpoint;
    private String fallbackImagePath;
    private String llmPromptTemplate;
    private String negativeImagePrompt;
    private String defaultImagePath;

    public ImageGenerator() {
        this.llmEndpoint = "http://localhost:8080/v1/chat/completions";
        this.imageEndpoint = "http://localhost:8080/v1/images/generations";
        this.fallbackImagePath = "default.jpeg";
        this.llmPromptTemplate = "Generate a short non detailed image description to visualize the passphrase: \"{passphrase}\".";
        this.negativeImagePrompt = "Text";
        this.defaultImagePath = "images/";

    }

    public void setLlmEndpoint(String llmEndpoint) {
        this.llmEndpoint = llmEndpoint;
    }

    public void setImageEndpoint(String imageEndpoint) {
        this.imageEndpoint = imageEndpoint;
    }

    public void setFallbackImagePath(String fallbackImagePath) {
        this.fallbackImagePath = fallbackImagePath;
    }

    public void setLlmPromptTemplate(String llmPromptTemplate) {
        this.llmPromptTemplate = llmPromptTemplate;
    }

    public void setNegativeImagePrompt(String negativeImagePrompt) {
        this.negativeImagePrompt = negativeImagePrompt;
    }

    public void setDefaultImagePath(String defaultImagePath) {
        this.defaultImagePath = defaultImagePath;
    }

    public String generateImage(String passphrase, String outputFilename) {
        String generatedPrompt = callLLMForPrompt(passphrase);
        System.out.println("Generated prompt: " + generatedPrompt);

        if (generatedPrompt == null) {
            System.out.println("Using fallback passphrase due to LLM failure");
            generatedPrompt = passphrase;
        }

        String imageUrl = callLocalAIAndReturnImage(generatedPrompt);

        if (imageUrl == null) {
            System.out.println("Image generation failed. Using fallback image.");
            File fallbackFile = new File(fallbackImagePath);
            if (fallbackFile.exists()) {
                imageUrl = fallbackFile.toURI().toString();
            } else {
                System.out.println("Fallback image file not found. Cannot load fallback image.");
                return null;
            }
        }

        if (imageUrl != null) {
            saveImage(imageUrl, outputFilename);
            return new File(defaultImagePath, outputFilename).getPath();
        } else {
            System.out.println("Failed to generate or fetch fallback image.");
            return null;
        }
    }

    private String callLLMForPrompt(String passphrase) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        String content = llmPromptTemplate.replace("{passphrase}", passphrase);
        message.addProperty("content", content);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("model", "gpt-4");
        requestBodyJson.add("messages", messages);

        RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(llmEndpoint)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
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

    private String callLocalAIAndReturnImage(String prompt) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("prompt", prompt + "|" + this.negativeImagePrompt);
        requestBodyJson.addProperty("model", "stablediffusion");
        requestBodyJson.addProperty("size", "512x512");

        RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(imageEndpoint)
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

    private void saveImage(String imageUrl, String destinationFile) {
        try {
            // create image save path if it does not exist
            File defaultImageDir = new File(defaultImagePath);
            if (!defaultImageDir.exists()) {
                boolean created = defaultImageDir.mkdirs();
                if (!created) {
                    System.out.println("Directory could not be created");
                }
            }
            URI uri = new URI(imageUrl);
            BufferedImage image = ImageIO.read(uri.toURL());
            File outputFile = new File(defaultImagePath, destinationFile);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved as: " + outputFile.getAbsolutePath());
        } catch (IOException | URISyntaxException e) {
            System.out.println("Failed to save image:");
            e.printStackTrace();
        }
    }
}