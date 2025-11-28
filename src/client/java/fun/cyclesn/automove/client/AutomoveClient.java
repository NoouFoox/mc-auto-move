package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutomoveClient implements ClientModInitializer {
    private int tick = 0;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    @Override
    public void onInitializeClient() {
        try {
            AutoEatAndRod.init();
            ClientTickEvents.END_CLIENT_TICK.register(client -> {

                if (!AutoMoveConfig.INSTANCE.enabled) return;
                if (client.player == null) return;

                tick++;

                // 每 100 tick（5秒）执行一次
                if (tick % 100 == 0) {
                    movingLeft = true;
                    client.options.leftKey.setPressed(true);
                }

                if (movingLeft && tick % 100 == 5) {
                    client.options.leftKey.setPressed(false);
                    movingLeft = false;

                    movingRight = true;
                    client.options.rightKey.setPressed(true);
                }

                if (movingRight && tick % 100 == 10) {
                    client.options.rightKey.setPressed(false);
                    movingRight = false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
