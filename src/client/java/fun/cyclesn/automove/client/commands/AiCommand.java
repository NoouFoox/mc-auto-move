package fun.cyclesn.automove.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import fun.cyclesn.automove.client.ai.AiService;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
        });
    }
}
