package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Save the uuid of shulker entities into their item forms so we retain transaction information
 * even when the shulker box is moved
 */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends LootableContainerBlockEntity implements NbtUuid {
	protected ShulkerBoxBlockEntityMixin(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	@Inject(at = @At("TAIL"), method = "deserializeInventory")
	public void deserializeInventoryMixin(CompoundTag tag, CallbackInfo info) {
		if (tag.containsUuid(ItemUtils.NBT_TAG_KEY)) {
			this.setNbtUuid(tag.getUuid(ItemUtils.NBT_TAG_KEY));
		} else {
			this.setNbtUuid(UUID.randomUUID());
		}
	}

	@Inject(at = @At("TAIL"), method = "serializeInventory")
	public void serializeInventoryMixin(CompoundTag tag, CallbackInfoReturnable<CompoundTag> ret) {
		tag.putUuid(ItemUtils.NBT_TAG_KEY, this.getNbtUuid());
	}
}
