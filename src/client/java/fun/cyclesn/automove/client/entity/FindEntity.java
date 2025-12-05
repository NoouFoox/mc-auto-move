package fun.cyclesn.automove.client.entity;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class FindEntity {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null
                    || !AutoMoveConfig.INSTANCE.findEntity
                    || AutoMoveConfig.INSTANCE.findEntityName.isEmpty()) return;

            String[] names = AutoMoveConfig.INSTANCE.findEntityName.split("\\s*,\\s*");

            // 每次 tick 清理高亮，重新添加
            EntityHighlighter.clear();

            for (Entity entity : client.world.getEntities()) {
                if ("*".equals(AutoMoveConfig.INSTANCE.findEntityName)) {
                    EntityHighlighter.highlight(entity);
                    continue;
                }

                String displayName = entity.getDisplayName() != null ? entity.getDisplayName().getString() : "";
                for (String name : names) {
                    if (!name.isEmpty() && displayName.contains(name)) {
                        EntityHighlighter.highlight(entity);
                        break;
                    }
                }
            }
        });
    }

}
