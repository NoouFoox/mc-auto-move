package fun.cyclesn.automove.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import fun.cyclesn.automove.client.ai.AiService;
import fun.cyclesn.automove.client.rag.EmbeddingClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;


public class AiCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("ai")
                            .then(argument("question", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        FabricClientCommandSource source = context.getSource();
                                        String question = StringArgumentType.getString(context, "question");
                                        source.sendFeedback(Text.literal("§7[AI] §f你的问题：§e" + question));
                                        AiService.ask(question, source);
                                        return 1;
                                    }))
            );

            dispatcher.register(
                    ClientCommandManager.literal("ai")
                            .then(ClientCommandManager.literal("read")
                                    .executes(context -> {
                                        MinecraftClient client = MinecraftClient.getInstance();
                                        if (client.player == null) return 0;

                                        ItemStack stack = client.player.getMainHandStack();
                                        StringBuilder bookText = new StringBuilder();

                                        var writable = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
                                        if (writable != null) {
                                            var pages = writable.pages();
                                            for (var page : pages) {
                                                bookText.append(page.raw()).append("\n");
                                            }
                                        } else {
                                            var written = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
                                            if (written != null) {
                                                var pages = written.pages();
                                                for (var page : pages) {
                                                    bookText.append(page.raw()).append("\n");
                                                }
                                            }
                                        }

                                        if (bookText.isEmpty()) {
                                            client.inGameHud.getChatHud().addMessage(Text.literal("§c你手上没有书，或书没有内容！"));
                                            return 1;
                                        }
                                        FabricClientCommandSource source = context.getSource();
                                        EmbeddingClient.embed(bookText.toString(), source);
                                        return 1;
                                    })
                            )
            );
        });
    }
}
