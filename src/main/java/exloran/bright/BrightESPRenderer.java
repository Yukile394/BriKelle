package exloran.bright;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * BrightESPRenderer — ESP acikken canli varliklarin etrafina tel kafes kutu cizer.
 *
 * Neden eski surum calismiyordu (1.20.1):
 *  - WorldRenderEvents.LAST icinde el ile Tessellator + POSITION_COLOR + LINES
 *    kullaniliyordu. 1.20.1'de LINES cizimi normal vektoru ve lines shader'i
 *    ister; POSITION_COLOR ile cizgiler ekranda hic gorunmez.
 *  - LAST olayi sis/derinlik durumu sifirlandiktan sonra calistigi icin
 *    matris ve kamera donusumu guvenilir degildi.
 *
 * Simdiki yaklasim:
 *  - Vanilla'nin kendi WorldRenderer.drawBox(...) metodu ve RenderLayer.getLines()
 *    kullanilir; format/normal/shader islerini oyun kendisi yapar.
 *  - Cizim AFTER_TRANSLUCENT asamasinda yapilir, kendi VertexConsumerProvider
 *    ile hemen flush edilir.
 *  - "Duvar Icinden Gor" acikken derinlik testi kapatilir.
 *  - Kutu tick interpolasyonu ile cizilir, hareket eden hedefte titremez.
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

        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer lines = provider.getBuffer(RenderLayer.getLines());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(lw);
        if (walls) RenderSystem.disableDepthTest();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;

            // Interpolasyonlu konum farki
            double dx = MathHelper.lerp(tickDelta, le.lastRenderX, le.getX()) - le.getX();
            double dy = MathHelper.lerp(tickDelta, le.lastRenderY, le.getY()) - le.getY();
            double dz = MathHelper.lerp(tickDelta, le.lastRenderZ, le.getZ()) - le.getZ();

            Box box = le.getBoundingBox().offset(dx - cam.x, dy - cam.y, dz - cam.z);
            WorldRenderer.drawBox(ms, lines, box, r, g, b, 0.95f);
        }

        // Cizgileri hemen flush et; derinlik testi durumu bu noktada gecerli
        provider.draw(RenderLayer.getLines());

        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }
}
