package fun.cyclesn.automove.client.ai;

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

public class AiService {
    private static final String API_URL = AutoMoveConfig.INSTANCE.apiUrl;
    private static final String API_KEY = AutoMoveConfig.INSTANCE.apiKey;
    private static final String model = AutoMoveConfig.INSTANCE.model;
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void ask(String question, FabricClientCommandSource source) {
        String mcVersion = MinecraftClient.getInstance().getGameVersion();
        if (API_KEY.isEmpty() || model.isEmpty() || API_URL.isEmpty()) {
            source.sendFeedback(Text.literal("§c请检查你的 API 密钥、模型和 API 地址是否填写正确！"));
        }
        String systemPrompt = "你是 Minecraft " + mcVersion + " 专家，只回答具体参数值，回答尽量短，不加解释。";
        new Thread(() -> {
            try {
                String body = """
                        {
                            "model": %s,
                            "messages": [
                                {"role": "system", "content": "%s"},
                                {"role": "user", "content": "%s"}
                            ]
                        }
                        """.formatted(model, systemPrompt, question);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                LOGGER.info(response.toString());
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray choices = json.getAsJsonArray("choices");

                if (choices == null || choices.isEmpty()) {
                    send(source, "§cAI 返回了空的结果！");
                    return;
                }

                String answer = choices.get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                send(source, "§a[AI 回答]§f " + answer);
                LOGGER.info(answer);
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§cAI 请求失败：§f" + e.getMessage()));
                LOGGER.error(e.getMessage());
            }
        }).start();
    }

    private static void send(FabricClientCommandSource src, String msg) {
        MinecraftClient.getInstance().execute(() -> {
            src.sendFeedback(Text.literal(msg));
        });
    }
}
