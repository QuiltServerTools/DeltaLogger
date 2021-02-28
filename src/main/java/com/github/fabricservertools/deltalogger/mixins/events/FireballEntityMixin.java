package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.events.FireballExplodeCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Log fireball explosions and the fireballing entity's target
 */
@Mixin(FireballEntity.class)

// onCollision
public class FireballEntityMixin extends AbstractFireballEntity {
    public FireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, LivingEntity livingEntity,
                               double d, double e, double f, World world) {
        super(entityType, livingEntity, d, e, f, world);
    }

    @Inject(
            method = "onCollision",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"),
            cancellable = true
    )
    private void tryLogFireballExplosion(HitResult hit, CallbackInfo info) {
        ActionResult result = FireballExplodeCallback.EVENT.invoker().explode((FireballEntity) (Object) this, hit);

        if (result != ActionResult.PASS) {
            info.cancel();
        }
    }
}
