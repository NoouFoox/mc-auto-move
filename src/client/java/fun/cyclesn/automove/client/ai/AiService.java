package fun.cyclesn.automove.client.ai;

import com.google.gson.*;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import fun.cyclesn.automove.client.rag.EmbeddingClient;
import fun.cyclesn.automove.client.rag.LocalKnowledgeBase;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;
import static fun.cyclesn.automove.client.AutomoveClient.kb;

public class AiService {
    private static final ChatHistory chatHistory = new ChatHistory();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().create();

    public static void ask(String question, FabricClientCommandSource source) {
        AutoMoveConfig config = AutoMoveConfig.INSTANCE;
        String mcVersion = MinecraftClient.getInstance().getGameVersion();

        if (config.apiKey.isEmpty() || config.model.isEmpty() || config.apiUrl.isEmpty()) {
            source.sendFeedback(Text.literal("§c请检查你的 API 密钥、模型和 API 地址是否填写正确！"));
            LOGGER.error("请检查你的 API 密钥{}、模型{}和 API 地址{}是否填写正确！",
                    config.apiKey, config.model, config.apiUrl);
            return;
        }
        new Thread(() -> {
            try {
                float[] queryEmbedding = EmbeddingClient.getEmbeddingSync(question);
                LOGGER.info("queryEmbedding: {}", queryEmbedding.length);
                LocalKnowledgeBase.Entry kbEntry = kb.search(queryEmbedding);
                String systemPrompt =
                        "你是 Minecraft " + mcVersion +
                                " 版本的专家。回答要自然、简洁、贴近玩家语言，只基于该版本事实。" +
                                "禁止重复前一条消息，不要总结，不要啰嗦，只根据玩家问题给一个直接且有温度的回答。" +
                                "当回答涉及以下内容时自动着色：" +
                                "\n物品名 → §e金色" +
                                "\n方块名 → §a绿色" +
                                "\n生物名 → §b蓝色" +
                                "\n重要动作（掉落、生成、使用、合成）→ §d紫色" +
                                "\n数字、层数、概率 → §c红色" +
                                "\n输出要自然、口语化，但保持信息准确。" +
                                "\n只输出简短答案。";
                if (kbEntry != null) {
                    systemPrompt += "\n参考知识库内容: " + kbEntry.text;
                    LOGGER.info(kbEntry.text);
                }
                LOGGER.info(systemPrompt);
                JsonArray messages = getJsonElements(question, systemPrompt);
                // 保存到历史
                chatHistory.add(new ChatCompletionMessage("user", question));

                // 请求 body
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.model);
                requestBody.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.apiUrl + "/chat/completions"))
                        .header("Authorization", "Bearer " + config.apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody)))
                        .build();

                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                String answer = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                send(source, "§c[问题]§f " + question);
                send(source, "§a[" + config.model + " 回答]§f \n" + answer);
                LOGGER.info(answer);

            } catch (IOException | InterruptedException e) {
                source.sendFeedback(Text.literal("§cAI 请求失败：§f" + e.getMessage()));
                LOGGER.error(e.getMessage());
            }
        }).start();
    }

    private static @NotNull JsonArray getJsonElements(String question, String systemPrompt) {
        JsonArray messages = new JsonArray();

        // 系统消息
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        // 历史消息
        for (ChatCompletionMessage msg : chatHistory.getMessages()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("role", msg.getRole());
            obj.addProperty("content", msg.getContent());
            messages.add(obj);
        }

        // 用户当前消息
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", question);
        messages.add(userMsg);
        return messages;
    }

    public static void send(FabricClientCommandSource src, String msg) {
        MinecraftClient.getInstance().execute(() -> src.sendFeedback(Text.literal(msg)));
    }

    // 用于历史消息存储
    public static class ChatCompletionMessage {
        private final String role;
        private final String content;

        public ChatCompletionMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public static class ChatHistory {
        private static final int MAX_HISTORY = AutoMoveConfig.INSTANCE.AI_MAX_HISTORY;
        private final LinkedList<ChatCompletionMessage> messages = new LinkedList<>();

        public void add(ChatCompletionMessage msg) {
            messages.add(msg);
            if (messages.size() > MAX_HISTORY) {
                messages.removeFirst();
            }
        }

        public List<ChatCompletionMessage> getMessages() {
            return new LinkedList<>(messages);
        }

        public void clear() {
            messages.clear();
        }
    }
}
