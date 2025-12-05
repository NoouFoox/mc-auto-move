package fun.cyclesn.automove.client;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fun.cyclesn.automove.client.config.AutoMoveConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class ModMenuApiImplNew implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::getConfigScreen;
    }

    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("AutoMove 配置"));
        builder.setSavingRunnable(() -> {
            LOGGER.info("正在保存 Cloth Config 配置...");
            AutoMoveConfig.INSTANCE.save();
        });
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("通用设置"));
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("自动挂机"), AutoMoveConfig.INSTANCE.enabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("启用/禁用自动移动功能"))
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.enabled = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("挂机是否跳跃"), AutoMoveConfig.INSTANCE.jumpEnabled)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.jumpEnabled = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("是否自动吃食"), AutoMoveConfig.INSTANCE.autoEat)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.autoEat = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("自动挂机攻击 X-Sword"), AutoMoveConfig.INSTANCE.autoSword)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.autoSword = newValue)
                .build());
//        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("寻找试炼大厅"), AutoMoveConfig.INSTANCE.findChamber)
//                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.findChamber = newValue)
//                .build());
        // 高亮宝库 (highlightTreasure)
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("高亮宝库"), AutoMoveConfig.INSTANCE.highlightTreasure)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.highlightTreasure = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("宝库超级高亮"), AutoMoveConfig.INSTANCE.showHighLight)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.showHighLight = newValue)
                .setTooltip(Text.literal("开启后会有一个长度为100格的大柱子显示在宝库上放"))
                .build());
        general.addEntry(entryBuilder.startTextField(Text.literal("API Key"), AutoMoveConfig.INSTANCE.apiKey)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.apiKey = newValue)
                .build());
        general.addEntry(entryBuilder.startTextField(Text.literal("API URL"), AutoMoveConfig.INSTANCE.apiUrl)
                .setDefaultValue("https://api.example.com/")
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.apiUrl = newValue)
                .build());
        general.addEntry(entryBuilder.startTextField(Text.literal("模型"), AutoMoveConfig.INSTANCE.model)
                .setDefaultValue("deepseek-chat")
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.model = newValue)
                .build());
        general.addEntry(entryBuilder.startIntField(Text.literal("AI 历史记录条数"), AutoMoveConfig.INSTANCE.AI_MAX_HISTORY)
                .setDefaultValue(6)
                .setSaveConsumer(newValue -> AutoMoveConfig.INSTANCE.AI_MAX_HISTORY = newValue)
                .build());

        return builder.build();
    }
}