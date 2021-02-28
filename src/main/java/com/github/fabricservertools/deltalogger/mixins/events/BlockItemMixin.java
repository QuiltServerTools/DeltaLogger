package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.DeltaLogger;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;
import com.github.fabricservertools.deltalogger.events.BlockPlaceCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
			method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
			cancellable = true
	)
	public void dlPlaceEventTrigger(ItemPlacementContext context, CallbackInfoReturnable<Boolean> info) {
		ActionResult result = BlockPlaceCallback.EVENT.invoker().place(context.getPlayer(), context);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}
}