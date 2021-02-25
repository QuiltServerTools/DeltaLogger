package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.ITransactable;
import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import com.github.fabricservertools.deltalogger.dao.TransactionDAO;

import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hooks to log the transaction events with inventory screens
 */
@Mixin(ScreenHandler.class)
public abstract class ContainerMixin implements NbtUuid, ITransactable {
  private ItemStack[] tracked;
  private List<Pair<Item, Integer>> transactions = new ArrayList<>();

  private UUID uuid;
  public void setNbtUuid(UUID uuid) { this.uuid = uuid; }
  public UUID getNbtUuid() { return uuid; }

	@Inject(at = @At(value = "HEAD"), method = "close")
	public void close(PlayerEntity player, CallbackInfo info) {
		UUID containerId = ((NbtUuid) this).getNbtUuid();
		if (containerId == null) return;

    Map<Item, Integer> transactions = ItemUtils.compressTransactions(this.getTransactions());
    transactions.forEach((item, count) -> {
      if (count == 0) return;
      Identifier id = Registry.ITEM.getId(item);
      // if (containerId == null) {
      //   LOG.warn(String.join("", "Unable to add container transaction for ", id.toString(), " by player ", player.getName().asString(), " skipping.."));
      //   return;
      // }
      DatabaseManager.getSingleton().queueOp(TransactionDAO.insert(
        player.getUuid(),
        containerId,
        java.time.Instant.now(),
        id,
        count,
        null
      ));
    });
  }

  @Inject(at = @At("HEAD"), method = "onSlotClick")
  public void onSlotClickHead(int ignored1, int ignored2, SlotActionType ignored3, PlayerEntity ignored4, CallbackInfoReturnable<ItemStack> cir) {
    List<ItemStack> stacks = ((ScreenHandler) (Object) this).getStacks();
    if (tracked == null) {
      final int playerSlotStartIndex = stacks.size() - 9 * 4;
      tracked = new ItemStack[playerSlotStartIndex]; // Only record slots that are not in the player inventory
    }
    for (int i = 0; i < tracked.length; i++) {
      if (tracked[i] == null || !ItemStack.areEqual(tracked[i], stacks.get(i))) {
        tracked[i] = stacks.get(i).copy();
      }
    }
  }

  @Inject(at = @At("RETURN"), method = "onSlotClick")
  public void onSlotClickReturn(int ignored1, int ignored2, SlotActionType ignored3, PlayerEntity ignored4, CallbackInfoReturnable<ItemStack> cir) {
    for(int i = 0; i < this.tracked.length; ++i) {
      ItemStack current = ((ScreenHandler) (Object) this).getSlot(i).getStack();
      ItemStack prev = tracked[i];
      if (!ItemStack.areEqual(prev, current)) {
        if (prev.isItemEqual(current) || current.isEmpty() || prev.isEmpty()) {
          // if same item then subtract and do transaction
          Item item = prev.isEmpty() ? current.getItem() : prev.getItem();
          transactions.add(new Pair<>(item, current.getCount() - prev.getCount()));
        } else {
          // else treat as item swap
          transactions.add(new Pair<>(prev.getItem(), -prev.getCount()));
          transactions.add(new Pair<>(current.getItem(), current.getCount()));
        }
        tracked[i] = current.copy();
      }
    }
  }

  @Override
  public List<Pair<Item, Integer>> getTransactions() {
    if (transactions == null) transactions = new ArrayList<>();
    return transactions;
  }
}