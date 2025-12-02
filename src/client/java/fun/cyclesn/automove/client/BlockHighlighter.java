package fun.cyclesn.automove.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockHighlighter {
    // 每个区块有哪些高亮方块
    private static final Map<ChunkPos, Set<BlockPos>> CHUNK_MAP = new HashMap<>();
    private static final Set<BlockPos> HIGHLIGHTED = new HashSet<>();

    static {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlighter::onRender);
    }
    public static void removeChunk(ChunkPos cp) {
        CHUNK_MAP.remove(cp);
    }
    public static void highlight(BlockPos pos) {
        HIGHLIGHTED.add(pos.toImmutable());
        ChunkPos cp = new ChunkPos(pos);
        CHUNK_MAP.computeIfAbsent(cp, k -> new HashSet<>()).add(pos.toImmutable());
    }

    public static void remove(BlockPos pos) {
        HIGHLIGHTED.remove(pos);
    }

    public static void clear() {
        HIGHLIGHTED.clear();
    }

    private static void onRender(WorldRenderContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || HIGHLIGHTED.isEmpty()) return;

        MatrixStack matrices = ctx.matrixStack();
        Camera camera = ctx.camera();
        Vec3d camPos = camera.getPos();

        VertexConsumer buf = client.getBufferBuilders()
                .getEntityVertexConsumers()
                .getBuffer(RenderLayer.getLines());

        for (BlockPos pos : HIGHLIGHTED) {
            if (matrices != null) {
                drawBox(matrices, buf, camPos, pos);
            }
        }
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer buf, Vec3d cam, BlockPos pos) {
        matrices.push();

        matrices.translate(
                pos.getX() - cam.x,
                pos.getY() - cam.y,
                pos.getZ() - cam.z
        );

        drawBoxLines(
                matrices,
                buf,
                0, 0, 0,
                1, 1, 1,
                0f, 1f, 1f, 1f // 青色线框
        );

        matrices.pop();
    }

    private static void drawBoxLines(MatrixStack matrices, VertexConsumer buf,
                                     double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     float r, float g, float b, float a) {

        // 4 个底部点
        float xMin = (float) x1;
        float yMin = (float) y1;
        float zMin = (float) z1;
        float xMax = (float) x2;
        float yMax = (float) y2;
        float zMax = (float) z2;

        // 12 条边（线框）
        // bottom square
        line(buf, matrices, xMin, yMin, zMin, xMax, yMin, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMin, xMax, yMin, zMax, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMax, xMin, yMin, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMin, zMax, xMin, yMin, zMin, r, g, b, a);

        // top square
        line(buf, matrices, xMin, yMax, zMin, xMax, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMax, zMin, xMax, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMax, yMax, zMax, xMin, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMax, zMax, xMin, yMax, zMin, r, g, b, a);

        // pillars
        line(buf, matrices, xMin, yMin, zMin, xMin, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMin, xMax, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMax, xMax, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMin, zMax, xMin, yMax, zMax, r, g, b, a);
    }

    // 单条线段
    private static void line(VertexConsumer buf, MatrixStack matrices,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {

        var entry = matrices.peek().getPositionMatrix();

        buf.vertex(entry, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0);
        buf.vertex(entry, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0);
    }
}