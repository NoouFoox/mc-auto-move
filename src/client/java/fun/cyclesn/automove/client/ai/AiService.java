package fun.cyclesn.automove.client.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.services.blocking.chat.ChatCompletionService;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.LinkedList;
import java.util.List;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class AiService {
    private static final ChatHistory chatHistory = new ChatHistory();

    public static void ask(String question, FabricClientCommandSource source) {
        AutoMoveConfig config = AutoMoveConfig.INSTANCE;
        String mcVersion = MinecraftClient.getInstance().getGameVersion();
        if (config.apiKey.isEmpty() || config.model.isEmpty() || config.apiUrl.isEmpty()) {
            source.sendFeedback(Text.literal("§c请检查你的 API 密钥、模型和 API 地址是否填写正确！"));
            LOGGER.error("请检查你的 API 密钥{}、模型{}和 API 地址{}是否填写正确！",
                    config.apiKey,
                    config.model,
                    config.apiUrl
            );
            return;
        }
        String systemPrompt =
                "你是 Minecraft " + mcVersion + " 版本的专家。" +
                        "回答必须简短、自然、像玩家聊天一样，不要机械罗列。" +
                        "不要复述历史消息，不要总结，不要解释你的行为。" +
                        "当回答涉及以下内容时自动着色：" +
                        "\n物品名 → §e金色" +
                        "\n方块名 → §a绿色" +
                        "\n生物名 → §b蓝色" +
                        "\n重要动作（掉落、生成、使用、合成）→ §d紫色" +
                        "\n数字、层数、概率 → §c红色" +
                        "\n输出要自然、口语化，但保持信息准确。" +
                        "\n格式示例：" +
                        "\n“去找苦力怕掉落火药吧” → “去找 §b苦力怕 §d掉落 §e火药 就行”" +
                        "\n“烈焰棒从哪来？” → “打 §b烈焰人 就会 §d掉落 §e烈焰棒”" +
                        "\n只输出简短答案。";


        new Thread(() -> {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(config.apiKey)
                    .baseUrl(config.apiUrl)
                    .build();
            ChatCompletionSystemMessageParam systemMsgParam =
                    ChatCompletionSystemMessageParam.builder()
                            .content(systemPrompt)
                            .build();
            ChatCompletionMessageParam systemMsg = ChatCompletionMessageParam.ofSystem(systemMsgParam);
            ChatCompletionUserMessageParam userMsgParam =
                    ChatCompletionUserMessageParam.builder()
                            .content(question)
                            .build();
            ChatCompletionMessageParam userMsg = ChatCompletionMessageParam.ofUser(userMsgParam);
            chatHistory.add(userMsg);
            List<ChatCompletionMessageParam> messages = new LinkedList<>();
            messages.add(systemMsg);
            messages.addAll(chatHistory.getMessages());
            try {
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(config.model)
                        .messages(messages)
                        .build();
                ChatCompletionService chatCompletions = client.chat().completions();
                var completion = chatCompletions.create(params);
                if (!completion.choices().isEmpty()) {
                    var messageOpt = completion.choices().getFirst().message().content();
                    String answer = messageOpt.orElse("无回答");
                    send(source, "§c[问题]§f " + question);
                    send(source, "§a[" + config.model + " 回答]§f " + "\n" + answer);
                    LOGGER.info(answer);
                }

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
