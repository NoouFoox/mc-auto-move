package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

/**
 * 寻找试炼大厅
 */
public class TrialChamber {
    // 铜阈值
    private static final int COPPER_THRESHOLD = 50;
    // 定义最小通知距离（米），100米
    private static final double MIN_NOTIFICATION_DISTANCE = 100.0;

    // 使用 Set 存储已发送通知的区块中心坐标，避免重复通知同一个结构
    private static final Set<Vec3d> notifiedChambers = new HashSet<>();

    public static void resetNotifiedChambers() {
        notifiedChambers.clear();
    }

    public static void init() {
        LOGGER.info("TrialChamber find init");
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!AutoMoveConfig.INSTANCE.findChamber) return;
            if (world.isClient && MinecraftClient.getInstance().player != null) {
                checkAndRenderInfo(chunk, MinecraftClient.getInstance().player);
            }
        });
    }

    public static void checkAndRenderInfo(Chunk chunk, ClientPlayerEntity player) {
        int copperCount = 0;
        // 限制Y轴范围
        int minY = -40;
        int maxY = 40;
        ChunkPos chunkPos = chunk.getPos();


        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();

        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // 修正坐标计算，使用 startX/Z
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    String blockName = state.getBlock().getName().toString();
                    if (AutoMoveConfig.INSTANCE.findChamber && blockName.contains("copper")) {
                        copperCount++;
                    }
                    if (blockName.contains("vault")) {
                        BlockHighlighter.highlight(pos);
                    }
                }
            }
        }

        if (copperCount >= COPPER_THRESHOLD) {
            // 计算当前检测到的区块中心的世界坐标
            Vec3d chamberCenter = new Vec3d(startX + 8.0, 0.0, startZ + 8.0);

            // --- 优化逻辑：检查是否距离已知的地牢太近 ---
            boolean isNewChamber = true;
            for (Vec3d notifiedPos : notifiedChambers) {
                // 检查水平距离是否小于阈值
                if (chamberCenter.distanceTo(notifiedPos) < MIN_NOTIFICATION_DISTANCE) {
                    isNewChamber = false;
                    break;
                }
            }

            if (isNewChamber) {
                // 如果是新的，添加进列表，并发送通知
                notifiedChambers.add(chamberCenter);

                // --- 添加距离计算逻辑 ---
                Vec3d playerPos = player.getPos();
                double chunkCenterX = startX + 8.0;
                double chunkCenterZ = startZ + 8.0;

                double distance = MathHelper.sqrt((float) ((playerPos.x - chunkCenterX) * (playerPos.x - chunkCenterX) +
                        (playerPos.z - chunkCenterZ) * (playerPos.z - chunkCenterZ)));

                // 4. 发送包含距离的提示信息
                player.sendMessage(Text.literal(
                        String.format("[TCD] 检测到附近有试炼地牢！距离约为 %.1f 米。方块坐标: X=%d, Z=%d, 铜块数量: %d",
                                distance, startX, startZ, copperCount)), false);
            }
        }
    }
}
