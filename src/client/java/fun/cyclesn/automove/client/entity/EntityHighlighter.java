package fun.cyclesn.automove.client.entity;

import fun.cyclesn.automove.client.config.AutoMoveConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class EntityHighlighter {

    private static final Set<Entity> HIGHLIGHTED = new HashSet<>();

    static {
        WorldRenderEvents.AFTER_ENTITIES.register(EntityHighlighter::onRender);
    }

    /**
     * 添加一个要高亮的实体
     */
    public static void highlight(Entity e) {
        if (e != null) {
            HIGHLIGHTED.add(e);
        }
    }

    /**
     * 清除所有高亮
     */
    public static void clear() {
        HIGHLIGHTED.clear();
    }

    /**
     * 移除一个实体
     */
    public static void remove(Entity e) {
        HIGHLIGHTED.remove(e);
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

        for (Entity e : HIGHLIGHTED) {
            if (e == null || !e.isAlive()) continue;

            drawEntityBox(matrices, buf, camPos, e);
        }
    }

    private static void drawEntityBox(MatrixStack matrices, VertexConsumer buf, Vec3d cam, Entity e) {
        matrices.push();

        // AABB
        Box box = e.getBoundingBox();
        matrices.translate(
                box.minX - cam.x,
                box.minY - cam.y,
                box.minZ - cam.z
        );

        double w = box.getLengthX();
        double h = box.getLengthY();
        double d = box.getLengthZ();
        float y0 = (float) (h - AutoMoveConfig.INSTANCE.findStrength);
        float y1 = (float) (h + AutoMoveConfig.INSTANCE.findStrength);
        // 绿色实体框
        drawBoxLines(
                matrices, buf,
                0, 0, 0,
                w, y1, d,
                1f, 0.843f, 0f, 1f
        );
        drawBoxLines(
                matrices, buf,
                0, y0, 0,
                w, h, d,
                1f, 0f, 1f, 1f
        );

        matrices.pop();
    }

    private static void drawBoxLines(MatrixStack matrices, VertexConsumer buf,
                                     double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     float r, float g, float b, float a) {

        float xMin = (float) x1;
        float yMin = (float) y1;
        float zMin = (float) z1;
        float xMax = (float) x2;
        float yMax = (float) y2;
        float zMax = (float) z2;

        // bottom
        line(buf, matrices, xMin, yMin, zMin, xMax, yMin, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMin, xMax, yMin, zMax, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMax, xMin, yMin, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMin, zMax, xMin, yMin, zMin, r, g, b, a);

        // top
        line(buf, matrices, xMin, yMax, zMin, xMax, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMax, zMin, xMax, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMax, yMax, zMax, xMin, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMax, zMax, xMin, yMax, zMin, r, g, b, a);

        // sides
        line(buf, matrices, xMin, yMin, zMin, xMin, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMin, xMax, yMax, zMin, r, g, b, a);
        line(buf, matrices, xMax, yMin, zMax, xMax, yMax, zMax, r, g, b, a);
        line(buf, matrices, xMin, yMin, zMax, xMin, yMax, zMax, r, g, b, a);
    }

    private static void line(VertexConsumer buf, MatrixStack matrices,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {

        var entry = matrices.peek().getPositionMatrix();
        buf.vertex(entry, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0);
        buf.vertex(entry, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0);
    }
}
