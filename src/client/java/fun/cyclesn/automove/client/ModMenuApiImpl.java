package fun.cyclesn.automove.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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

        @Override
        protected void init() {
            this.addDrawableChild(
                    ButtonWidget.builder(
                            Text.literal("自动挂机:" + (AutoMoveConfig.INSTANCE.enabled ? "开启" : "关闭")),
                            btn -> {
                                AutoMoveConfig.INSTANCE.enabled = !AutoMoveConfig.INSTANCE.enabled;
                                AutoMoveConfig.INSTANCE.save();
                                btn.setMessage(Text.literal("自动挂机:" + (AutoMoveConfig.INSTANCE.enabled ? "开启" : "关闭")));
                            }
                    ).dimensions(this.width / 2 - 100, this.height / 2 - 30, 200, 20).build()
            );
            this.addDrawableChild(
                    ButtonWidget.builder(
                            Text.literal("切换鱼竿功能:" + (AutoMoveConfig.INSTANCE.fishEnabled ? "开启" : "关闭")),
                            btn -> {
                                AutoMoveConfig.INSTANCE.fishEnabled = !AutoMoveConfig.INSTANCE.fishEnabled;
                                AutoMoveConfig.INSTANCE.save();
                                btn.setMessage(Text.literal("切换鱼竿功能:" + (AutoMoveConfig.INSTANCE.fishEnabled ? "开启" : "关闭")));
                            }
                    ).dimensions(this.width / 2 - 100, this.height / 2 - 60, 200, 20).build()
            );
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("返回"), (button) -> {
                        MinecraftClient.getInstance().setScreen(parent);
                    }).dimensions(this.width / 2 - 40, this.height / 2, 80, 20).build()
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