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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.CompoundTag;

/** 
 * Log most container entity access events which have LockableContainerBlockEntity
 * as a super class.
 */
@Mixin(LockableContainerBlockEntity.class)
public abstract class LockableContainerBlockEntityMixin extends BlockEntity implements IWatchTowerId {
  public LockableContainerBlockEntityMixin(BlockEntityType<?> type) {
    super(type);
  }

  private UUID watchtowerid = UUID.randomUUID();

  @Inject(at = @At("TAIL"), method = "fromTag")
  public void fromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
    if (tag.containsUuid(ItemUtils.NBT_TAG_KEY)) {
      this.watchtowerid = tag.getUuid(ItemUtils.NBT_TAG_KEY);
      // System.out.println("HAS UUID " + this.watchtowerid);
    } else {
      this.watchtowerid = UUID.randomUUID();
      // System.out.println("ASSIGNED UUID " + this.watchtowerid);
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