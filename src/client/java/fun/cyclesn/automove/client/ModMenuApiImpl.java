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

        private String getLabel(String label, Boolean is) {
            return label + (is ? "：开启" : "：关闭");
        }

        private void addToggleButton(
                int y,
                String label,
                BooleanSupplier getter,
                Consumer<Boolean> setter
        ) {
            String text = getLabel(label, getter.getAsBoolean());

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

        private void addToggleButton(
                int y,
                int x,
                int width,
                String label,
                BooleanSupplier getter,
                Consumer<Boolean> setter
        ) {
            String text = getLabel(label, getter.getAsBoolean());

            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal(text), button -> {
                                boolean newVal = !getter.getAsBoolean();
                                setter.accept(newVal);
                                AutoMoveConfig.INSTANCE.save();
                                button.setMessage(Text.literal(label + (newVal ? "：开启" : "：关闭")));
                            })
                            .dimensions(x, y, width, 20)
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
                    "是否自动吃食",
                    () -> AutoMoveConfig.INSTANCE.autoEat,
                    v -> AutoMoveConfig.INSTANCE.autoEat = v
            );
            addToggleButton(100,
                    "自动挂机攻击 X-Sword",
                    () -> AutoMoveConfig.INSTANCE.autoSword,
                    v -> AutoMoveConfig.INSTANCE.autoSword = v
            );
            addToggleButton(130,
                    "寻找试炼大厅",
                    () -> AutoMoveConfig.INSTANCE.findChamber,
                    v -> AutoMoveConfig.INSTANCE.findChamber = v
            );
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("清空试炼hash"), b -> {
                                TrialChamber.resetNotifiedChambers();
                            })
                            .dimensions(270, 130, 80, 20)
                            .build()
            );
            addToggleButton(160,
                    "高亮宝库",
                    () -> AutoMoveConfig.INSTANCE.highlightTreasure,
                    v -> {
                        AutoMoveConfig.INSTANCE.highlightTreasure = v;
                        if (!v) {
                            BlockHighlighter.clear();
                        }
                    }
            );
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("超级高亮:"+(AutoMoveConfig.INSTANCE.showHighLight ? "开启" : "关闭")), b -> {
                                AutoMoveConfig.INSTANCE.showHighLight = !AutoMoveConfig.INSTANCE.showHighLight;
                                AutoMoveConfig.INSTANCE.save();
                                b.setMessage(Text.literal("超级高亮:" + (AutoMoveConfig.INSTANCE.showHighLight ? "开启" : "关闭")));
                            })
                            .dimensions(270, 160, 120, 20)
                            .build()
            );
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("返回"), b -> MinecraftClient.getInstance().setScreen(parent))
                            .dimensions(60, 190, 80, 20)
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