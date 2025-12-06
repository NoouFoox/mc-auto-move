package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.commands.AiCommand;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import fun.cyclesn.automove.client.entity.FindEntity;
import fun.cyclesn.automove.client.rag.LocalKnowledgeBase;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class AutomoveClient implements ClientModInitializer {
    private int tick = 0;
    public static final String MOD_ID = "EazyMCccc";
    public static final String kbFile = "./config/knowledge.csv";
    public static LocalKnowledgeBase kb;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private final AutoMoveConfig config = AutoMoveConfig.load();

    public AutomoveClient() throws IOException {
    }

    @Override
    public void onInitializeClient() {
        try {
            try {
                kb = new LocalKnowledgeBase(kbFile);
                kb.load();
                LOGGER.info("LocalKnowledgeBase 初始化成功: {}", kbFile);
            } catch (IOException e) {
                LOGGER.error("LocalKnowledgeBase 初始化失败", e);
            }
            TrialChamber.init();
            AutoEatAndRod.init();
            HighlightVault.init();
            AutoMoveConfig.INSTANCE = config;
            AutoMoveConfig.INSTANCE.save();
            AiCommand.register();
            FindEntity.init();
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.player == null) return;

                // tick 累加并重置
                tick++;
                if (tick == Integer.MAX_VALUE) tick = 0;


                // ================================
                //         自动挥剑（独立功能）
                // ================================
                if (AutoMoveConfig.INSTANCE.autoSword) {
                    if (tick % 20 == 0) {
                        // 必须存在世界和玩家
                        if (client.world == null) return;
                        ItemStack held = client.player.getMainHandStack();
                        if (!isSword(held)) return;
                        // 获取视线中的实体
                        var target = client.targetedEntity;
                        // 如果看到了一个实体
                        if (target != null) {
                            if (client.interactionManager != null) {
                                client.interactionManager.attackEntity(client.player, target);
                            }
                            client.player.swingHand(Hand.MAIN_HAND); // 播放动画
                        }
                    }
                }
                // ================================
                //         自动挂机移动（受 enabled 控制）
                // ================================
                if (!AutoMoveConfig.INSTANCE.enabled) return;

                if (tick % 100 == 0) {
                    movingLeft = true;
                    client.options.leftKey.setPressed(true);

                    if (AutoMoveConfig.INSTANCE.jumpEnabled) {
                        client.player.jump();
                    }
                }

                if (movingLeft && tick % 100 == 10) {
                    client.options.leftKey.setPressed(false);
                    movingLeft = false;

                    movingRight = true;
                    client.options.rightKey.setPressed(true);
                }

                if (movingRight && tick % 100 == 20) {
                    client.options.rightKey.setPressed(false);
                    movingRight = false;
                }
            });

        } catch (Exception e) {
            LOGGER.error("初始化失败", e);
        }
    }

    private boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem().toString().toLowerCase().contains("sword");
    }
}
