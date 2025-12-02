package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.tool.VaultRecordManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.VaultBlock;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class Records {
    public static void init() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((old, newWorld) -> {
            if (newWorld != null) {
                VaultRecordManager.loadRecords(newWorld);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            VaultRecordManager.saveRecords();
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();

            if (world.getBlockState(pos).getBlock() == Blocks.VAULT) {
                if (player.getStackInHand(hand).getItem() == Items.TRIAL_KEY) {
                    VaultRecordManager.markAsOpened(pos);
                }
            }

            return ActionResult.PASS;
        });
    }
}
