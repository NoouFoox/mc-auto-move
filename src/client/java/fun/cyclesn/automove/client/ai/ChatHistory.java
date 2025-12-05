package fun.cyclesn.automove.client.ai;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import fun.cyclesn.automove.client.config.AutoMoveConfig;

import java.util.LinkedList;
import java.util.List;

public class ChatHistory {
    private static final int MAX_HISTORY = AutoMoveConfig.INSTANCE.AI_MAX_HISTORY;
    private final LinkedList<ChatCompletionMessageParam> messages = new LinkedList<>();

    public void add(ChatCompletionMessageParam msg) {
        messages.add(msg);
        if (messages.size() > MAX_HISTORY) {
            messages.removeFirst();
        }
    }

    public List<ChatCompletionMessageParam> getMessages() {
        return new LinkedList<>(messages);
    }

    public void clear() {
        messages.clear();
    }
}
