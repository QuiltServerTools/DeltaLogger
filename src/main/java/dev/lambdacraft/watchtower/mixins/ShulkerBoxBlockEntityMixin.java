package dev.lambdacraft.watchtower.mixins;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lambdacraft.watchtower.IWatchTowerId;
import dev.lambdacraft.watchtower.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Assign UUID to shulker entities
 */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends LootableContainerBlockEntity implements IWatchTowerId {
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

  public UUID getWatchTowerId() {
    return watchtowerid;
  }
}