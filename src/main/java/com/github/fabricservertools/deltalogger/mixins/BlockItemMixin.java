package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.DeltaLogger;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
  public BlockItemMixin(Settings settings) {
    super(settings);
  }

  /**
   * Log item placement hook
   */
  @Inject(
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/item/ItemPlacementContext;getBlockPos()Lnet/minecraft/util/math/BlockPos;"
    ),
    method = "place"
  )
  public void place(ItemPlacementContext context, CallbackInfoReturnable<Boolean> info) {
    try {
      BlockPos pos = context.getBlockPos();
      PlayerEntity player = context.getPlayer();
      World world = context.getWorld();
      BlockState bs = world.getBlockState(pos);
      Identifier id = Registry.BLOCK.getId(bs.getBlock());
      Identifier dimension = world.getRegistryKey().getValue();
  
      if (player == null) return;
      // if (
      //   bs.getBlock() == Blocks.D &&
      //   world.getServer().getPlayerManager().isOperator(player.getGameProfile())
      // ) return;

      DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
        player.getUuid(),
        id,
        true,
        pos,
        bs,
        dimension,
        java.time.Instant.now()
      ));
    } catch (Exception e) {
      DeltaLogger.LOG.warn("Problem in placement");
      e.printStackTrace();
    }
  }
}