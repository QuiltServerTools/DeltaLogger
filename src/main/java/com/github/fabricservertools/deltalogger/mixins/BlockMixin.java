package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.ModInit;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(Block.class)
public abstract class BlockMixin implements ItemConvertible {
  // @Shadow
  // private MinecraftClient client;

  /**
   * Log block breaking hook
   */
  @Inject(at = @At("HEAD"), method = "onBreak")
  public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
    Identifier id = null;
    Identifier dimension = null;
    try {
      id = Registry.BLOCK.getId(state.getBlock());
      dimension = world.getRegistryKey().getValue();
  
      DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
        player.getUuid(),
        id,
        false,
        pos,
        dimension,
        java.time.Instant.now()
      ));
    } catch (Exception e) {
      ModInit.LOG.warn(String.join("\n",
        "Problem detected id:" + (id == null ? "null" : id.toString()) + " dimension:" + (dimension == null ? "null" : dimension.toString()),
        "player: " + (player == null ? "null" : player.toString()),
        e.toString()
      ));
    }
  }
}