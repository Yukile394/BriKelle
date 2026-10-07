package exloran.bright;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * BrightESPRenderer (1.21) — canli varliklarin etrafina tel kafes kutu cizer.
 * Kendi Tessellator'u + vanilla lines shader'i kullanir; boylece "Duvar Icinden Gor"
 * (derinlik testi kapali) ve cizgi kalinligi gercekten calisir.
 */
public class BrightESPRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BrightESPRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        if (Bright.config == null || !Bright.config.espActive) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;

        Vec3d cam = ctx.camera().getPos();
        float tickDelta = ctx.tickDelta();

        float r = Bright.config.espR;
        float g = Bright.config.espG;
        float b = Bright.config.espB;
        float lw = Bright.config.espLineWidth;
        boolean walls = Bright.config.espThroughWalls;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lw);
        if (walls) RenderSystem.disableDepthTest();

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        boolean any = false;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;

            double dx = MathHelper.lerp(tickDelta, le.lastRenderX, le.getX()) - le.getX();
            double dy = MathHelper.lerp(tickDelta, le.lastRenderY, le.getY()) - le.getY();
            double dz = MathHelper.lerp(tickDelta, le.lastRenderZ, le.getZ()) - le.getZ();

            Box box = le.getBoundingBox().offset(dx - cam.x, dy - cam.y, dz - cam.z);
            WorldRenderer.drawBox(ms, buf, box, r, g, b, 0.95f);
            any = true;
        }

        if (any) BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }
}
