package dev.lambdacraft.watchtower.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.ExampleMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

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
  
      DatabaseManager.getSingleton().queueOp(new DatabaseManager.BlockUpdate(player.getUuid(), id, true, pos, dimension));
    } catch (Exception e) {
      ExampleMod.LOG.warn("Problem in WatchTower placement");
      e.printStackTrace();
    }
  }
}