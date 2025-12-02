package fun.cyclesn.automove.client;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class HighlightVault {
    public static void init() {
        LOGGER.info("HighlightVault init");
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!AutoMoveConfig.INSTANCE.highlightTreasure) return;
            if (world.isClient && MinecraftClient.getInstance().player != null) {
                render(chunk, MinecraftClient.getInstance().player);
            }

        });
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            if (!AutoMoveConfig.INSTANCE.highlightTreasure) return;
            ChunkPos cp = chunk.getPos();
            BlockHighlighter.removeChunk(cp);
        });
    }

    public static void render(Chunk chunk, ClientPlayerEntity player) {
        int minY = -40;
        int maxY = 40;
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    String blockName = state.getBlock().getName().toString();
                    if (blockName.contains("vault")) {
                        BlockHighlighter.highlight(pos);
                    }
                }
            }
        }
    }
}
