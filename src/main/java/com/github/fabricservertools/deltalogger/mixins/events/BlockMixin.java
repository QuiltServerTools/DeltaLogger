package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.events.BlockExplodeCallback;
import net.minecraft.block.Block;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
	@Inject(method = "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/explosion/Explosion;)V",
			at = @At("HEAD"), cancellable = true)
	private void dlBlockExplodeEventTrigger(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		ActionResult result = BlockExplodeCallback.EVENT.invoker().explode(world, pos, (Block) (Object) this, explosion);

		if (result != ActionResult.PASS) {
			ci.cancel();
		}
	}
}
