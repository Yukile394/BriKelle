package exloran.bright.mixin;

import exloran.bright.Bright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitboxMixin — hitbox buyutme.
 *
 * Eski surumdeki hatalar:
 *  1) Yukseklik, o anki kutunun yuksekligine gore hesaplaniyordu. Elytra + havai
 *     fisek ile ucarken poz FALL_FLYING olur ve vanilla kutu 0.6 x 0.6'ya
 *     kuculur; genisletme de bu kucuk kutuya uygulandigi icin hitbox
 *     "normale donuyor" gibi gorunuyordu (ayakta 1.8 * 1.2 yerine 0.6 * 1.2).
 *  2) Kutu sadece dikey olarak minY'den yukari buyutuluyordu; yatay ucusta
 *     kutunun gercek merkezi kayiyordu.
 *
 * Duzeltme:
 *  - Ucus/yuzme/egilme gibi pozlarda kutu, ayakta duran STANDING boyutlarindan
 *    turetilen sabit bir yukseklige gore buyutulur.
 *  - Kutu, dikeyde varligin gercek orta noktasi etrafinda buyutulur.
 *  - Genisletme her zaman vanilla kutudan hesaplanir (getBoundingBox sonucunu
 *    tekrar girdi olarak kullanmaz), boylece ust uste buyume olmaz.
 */
@Mixin(Entity.class)
public abstract class HitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void bright_expandBoundingBox(CallbackInfoReturnable<Box> cir) {
        if (Bright.config == null || !Bright.config.hitboxActive) return;

        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && self == mc.player) return;

        Box orig = cir.getReturnValue();

        // Poza bagli olmayan referans boyut: ayakta durma boyutu
        EntityDimensions standing = living.getDimensions(EntityPose.STANDING);
        double baseW = Math.max(orig.maxX - orig.minX, standing.width);
        double baseH = Math.max(orig.maxY - orig.minY, standing.height);

        // Ucus/yatay pozlarda kutu ayakta durma boyutundan kucuk olabilir; buyutmeyi
        // her zaman en az ayakta boyuta gore yap.
        double halfW = baseW / 2.0 * Bright.config.xzExpand;
        double halfH = baseH / 2.0 * Bright.config.yExpand;

        double cx = (orig.minX + orig.maxX) / 2.0;
        double cy = (orig.minY + orig.maxY) / 2.0;
        double cz = (orig.minZ + orig.maxZ) / 2.0;

        cir.setReturnValue(new Box(
                cx - halfW, cy - halfH, cz - halfW,
                cx + halfW, cy + halfH, cz + halfW
        ));
    }
}
