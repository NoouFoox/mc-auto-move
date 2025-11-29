package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoEatAndRod {
    public static boolean wasUsingItem = false;
    private static int previousSlot = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (!AutoMoveConfig.INSTANCE.autoEat) {
//                client.options.useKey.setPressed(false);
                wasUsingItem = false;
                previousSlot = -1;
            } else {
                autoEat(client);
                checkEatFinish(client);
            }
        });
    }

    //    自动吃食
    public static void autoEat(MinecraftClient client) {
        var player = client.player;
        if (player == null) return;
        if (player.getHungerManager().getFoodLevel() >= 18) return;
        var food = findFoodInHotbar(player.getInventory());
        if (food != -1) {
            int preSlot = player.getInventory().getSelectedSlot();
            if (food != preSlot) {
                previousSlot = player.getInventory().getSelectedSlot();
            }
            player.getInventory().setSelectedSlot(food);
            client.options.useKey.setPressed(true);
        }
    }

    //    吃没吃完食
    private static void checkEatFinish(MinecraftClient client) {
        var player = client.player;
        if (player == null) return;
        boolean isUsing = player.isUsingItem();

        // 上一 tick 在吃，现在不吃 = 吃完了
        if (wasUsingItem && !isUsing) {
            switchBackToRod(client);
        }

        wasUsingItem = isUsing;
    }

    //    吃完食钓鱼
    private static void switchBackToRod(MinecraftClient client) {
        if (client.player == null) return;
        PlayerInventory inv = client.player.getInventory();
        if (previousSlot != -1) {
            inv.setSelectedSlot(previousSlot);
            previousSlot = -1;
        }
        // 停止“右键”
        client.options.useKey.setPressed(false);
    }

    //    找食在哪
    private static int findFoodInHotbar(PlayerInventory inv) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && isFood(stack)) return i;
        }
        return -1;
    }

    //    看看是不是食
    public static boolean isFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return switch (stack.getItem().toString()) {
            case "minecraft:apple",
                 "minecraft:bread",
                 "minecraft:cooked_beef",
                 "minecraft:cooked_chicken",
                 "minecraft:cooked_porkchop",
                 "minecraft:cooked_mutton",
                 "minecraft:cooked_rabbit",
                 "minecraft:cooked_cod",
                 "minecraft:cooked_salmon",
                 "minecraft:carrot",
                 "minecraft:potato",
                 "minecraft:baked_potato",
                 "minecraft:beetroot",
                 "minecraft:sweet_berries",
                 "minecraft:mushroom_stew",
                 "minecraft:beetroot_soup",
                 "minecraft:rabbit_stew",
                 "minecraft:suspicious_stew",
                 "minecraft:golden_apple",
                 "minecraft:enchanted_golden_apple",
                 "minecraft:golden_carrot" -> true;
            default -> false;
        };
    }
}
