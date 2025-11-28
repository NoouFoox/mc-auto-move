package fun.cyclesn.automove.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AuthScreen::new;
    }

    public static class AuthScreen extends Screen {
        private final Screen parent;

        protected AuthScreen(Screen parent) {
            super(Text.literal("AutoMove 配置"));
            this.parent = parent;
        }
        private void addToggleButton(
                int y,
                String label,
                BooleanSupplier getter,
                Consumer<Boolean> setter
        ) {
            String text = label + (getter.getAsBoolean() ? "：开启" : "：关闭");

            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal(text), button -> {
                                boolean newVal = !getter.getAsBoolean();
                                setter.accept(newVal);
                                AutoMoveConfig.INSTANCE.save();
                                button.setMessage(Text.literal(label + (newVal ? "：开启" : "：关闭")));
                            })
                            .dimensions(60, y, 200, 20)
                            .build()
            );
        }

        @Override
        protected void init() {
            // 一个通用的按钮创建方法（减少重复代码）
            addToggleButton(10,
                    "自动挂机",
                    () -> AutoMoveConfig.INSTANCE.enabled,
                    v -> AutoMoveConfig.INSTANCE.enabled = v
            );
            addToggleButton(40,
                    "挂机是否跳跃",
                    () -> AutoMoveConfig.INSTANCE.jumpEnabled,
                    v -> AutoMoveConfig.INSTANCE.jumpEnabled = v
            );
            addToggleButton(70,
                    "切换鱼竿功能",
                    () -> AutoMoveConfig.INSTANCE.fishEnabled,
                    v -> AutoMoveConfig.INSTANCE.fishEnabled = v
            );
            addToggleButton(100,
                    "是否自动吃食",
                    () -> AutoMoveConfig.INSTANCE.autoEat,
                    v -> AutoMoveConfig.INSTANCE.autoEat = v
            );
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("返回"), b -> MinecraftClient.getInstance().setScreen(parent))
                            .dimensions(60, 150, 80, 20)
                            .build()
            );
        }

        @Override
        public void close() {
            if (client != null) {
                client.setScreen(parent);
            }
        }
    }
}