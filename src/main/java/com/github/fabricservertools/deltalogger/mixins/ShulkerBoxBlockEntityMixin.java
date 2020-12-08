package com.github.fabricservertools.deltalogger.mixins;

import java.util.UUID;

import com.github.fabricservertools.deltalogger.NbtUuid;
import com.github.fabricservertools.deltalogger.ItemUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Assign UUID to shulker entities
 */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends LootableContainerBlockEntity implements NbtUuid {
  protected ShulkerBoxBlockEntityMixin(BlockEntityType<?> blockEntityType) {
    super(blockEntityType);
  }

  private UUID watchtowerid = UUID.randomUUID();

  @Inject(at = @At("TAIL"), method = "fromTag")
  public void fromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
    if (tag.containsUuid(ItemUtils.NBT_TAG_KEY)) {
      this.watchtowerid = tag.getUuid(ItemUtils.NBT_TAG_KEY);
    } else {
      this.watchtowerid = UUID.randomUUID();
    }
  }

  @Inject(at = @At("TAIL"), method = "toTag")
  public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> ret) {
    tag.putUuid(ItemUtils.NBT_TAG_KEY, this.watchtowerid);
  }

  public UUID getNbtUuid() {
    return watchtowerid;
  }
}