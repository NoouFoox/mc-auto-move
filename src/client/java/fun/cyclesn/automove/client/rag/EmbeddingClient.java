package fun.cyclesn.automove.client.rag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;
import static fun.cyclesn.automove.client.AutomoveClient.kb;

public class EmbeddingClient {

    public static void embed(String text, FabricClientCommandSource source) {
        new Thread(() -> {
            AutoMoveConfig config = AutoMoveConfig.INSTANCE;
            float[] embedding = new float[0];
            if (config.EmbeddingKey.isEmpty() || config.EmbeddingUrl.isEmpty()) {
                LOGGER.error("EmbeddingClient: {}", "EmbeddingClient error");
                send(source, "嵌入模型配置错误，请检查。");
                return;
            }
            try {
                HttpClient client = HttpClient.newHttpClient();
                JsonObject body = new JsonObject();
                body.addProperty("model", config.EmbeddingModel);
                body.addProperty("input", text);
                body.addProperty("encoding_format", "float");
                body.addProperty("dimensions", 1024);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.EmbeddingUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + config.EmbeddingKey)
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();
                String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                JsonArray dataArray = json.getAsJsonArray("data");
                if (dataArray.isEmpty()) {
                    LOGGER.error("EmbeddingClient: {}", "EmbeddingClient error");
                    send(source, "EmbeddingClient error");
                }
                JsonObject firstObj = dataArray.get(0).getAsJsonObject();
                JsonArray embeddingArray = firstObj.getAsJsonArray("embedding");
                LOGGER.info("EmbeddingClient: {}", embeddingArray);
                float[] vec = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    vec[i] = embeddingArray.get(i).getAsFloat();
                }
                embedding = vec;
                LOGGER.info("向量长度: {}", embedding.length);
                send(source, "向量长度: " + embedding.length);
                kb.load();
                kb.add(text, embedding);
                kb.save();
            } catch (Exception e) {
                LOGGER.error("EmbeddingClient error", e);
                send(source, "EmbeddingClient error");
            }
        }).start();
    }

    public static float[] getEmbeddingSync(String text) {
        try {
            AutoMoveConfig config = AutoMoveConfig.INSTANCE;
            HttpClient client = HttpClient.newHttpClient();
            JsonObject body = new JsonObject();
            body.addProperty("model", config.EmbeddingModel);
            body.addProperty("input", text);
            body.addProperty("encoding_format", "float");
            body.addProperty("dimensions", 1024);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.EmbeddingUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.EmbeddingKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray dataArray = json.getAsJsonArray("data");
            if (dataArray.isEmpty()) return new float[0];

            JsonArray embeddingArray = dataArray.get(0).getAsJsonObject().getAsJsonArray("embedding");
            float[] vec = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                vec[i] = embeddingArray.get(i).getAsFloat();
            }
            return vec;
        } catch (Exception e) {
            LOGGER.error("EmbeddingClient error", e);
            return new float[0];
        }
    }

    public static void send(FabricClientCommandSource src, String msg) {
        MinecraftClient.getInstance().execute(() -> src.sendFeedback(Text.literal(msg)));
    }
}
