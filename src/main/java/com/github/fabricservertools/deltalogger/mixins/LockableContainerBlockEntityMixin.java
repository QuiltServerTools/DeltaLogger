package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Give all LocakableContainerBlockEntity objects an identifier to associate events with
 */
@Mixin(LockableContainerBlockEntity.class)
public abstract class LockableContainerBlockEntityMixin extends BlockEntity implements NbtUuid {
	public LockableContainerBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	private UUID uuid = UUID.randomUUID();

	@Inject(at = @At("TAIL"), method = "fromTag")
	public void fromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
		if (tag.containsUuid(ItemUtils.NBT_TAG_KEY)) {
			this.uuid = tag.getUuid(ItemUtils.NBT_TAG_KEY);
		} else {
			this.uuid = UUID.randomUUID();
		}
	}

	@Inject(at = @At("TAIL"), method = "toTag")
	public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> ret) {
		tag.putUuid(ItemUtils.NBT_TAG_KEY, this.uuid);
	}

	public UUID getNbtUuid() {
		return uuid;
	}

	@Override
	public void setNbtUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
