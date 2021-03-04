package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Objects;

@Mixin(Block.class)
public class BlockMixin {
	@Inject(method = "onDestroyedByExplosion(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/explosion/Explosion;)V", at = @At("HEAD"))
	private void logExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
		DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
			Objects.requireNonNull(explosion.getCausingEntity()).getUuid(), Registry.BLOCK.getId(((Block)(Object)this)) , false, pos, world.getBlockState(pos), world.getRegistryKey().getValue(), Instant.now()
		));
	}
}
