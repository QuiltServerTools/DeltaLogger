package dev.lambdacraft.watchtower.mixins;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.ITransactable;
import dev.lambdacraft.watchtower.IWatchTowerId;
import dev.lambdacraft.watchtower.ItemUtils;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

/**
 * Hooks to log the transaction events with inventory screens
 */
@Mixin(ScreenHandler.class)
public abstract class ContainerMixin implements IWatchTowerId, ITransactable {
	private static final Logger LOG = LogManager.getLogger();

  private List<Pair<Item, Integer>> watchTowerTransactions = new ArrayList<>();

  public List<Pair<Item, Integer>> getTransactions() {
    if (watchTowerTransactions == null) watchTowerTransactions = new ArrayList<>();
    return watchTowerTransactions;
  }

  private UUID watchTowerId;
  public void setWatchTowerId(UUID uuid) { watchTowerId = uuid; }
  public UUID getWatchTowerId() { return watchTowerId; }

  @Shadow
  private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
  @Shadow
  public final List<Slot> slots = Lists.newArrayList();

  @Inject(at = @At(value = "HEAD"), method = "close")
  public void close(PlayerEntity player, CallbackInfo info) {
    UUID containerId = ((IWatchTowerId)this).getWatchTowerId();
    if (containerId == null) return;

    ITransactable transactable = (ITransactable)this;
    Map<Item, Integer> transactions = ItemUtils.compressTransactions(transactable.getTransactions());
    transactions.forEach((item, count) -> {
      Identifier id = Registry.ITEM.getId(item);
      // System.out.println("QUEUE " + id + " " + count);
      // if (containerId == null) {
      //   LOG.warn(String.join("", "Unable to add container transaction for ", id.toString(), " by player ", player.getName().asString(), " skipping.."));
      //   return;
      // }
      DatabaseManager.getSingleton().queueOp(new DatabaseManager.ContainerTransaction(
        player.getUuid(), containerId, DatabaseManager.getTime(), id, count, null
      ));
    });
  }

  public void trackLatestTransactions() {
    List<Pair<Item, Integer>> transactions = new ArrayList<>();
    int playerSlotStartIndex = this.slots.size() - 9 * 4;
    for(int j = 0; j < playerSlotStartIndex; ++j) {
       ItemStack afterStack = this.slots.get(j).getStack();
       ItemStack beforeStack = this.trackedStacks.get(j);
       if (!ItemStack.areItemsEqualIgnoreDamage(beforeStack, afterStack)) {
          // System.out.println(j + " beforeStack " + beforeStack + " afterStack " + afterStack);
          if (beforeStack.isItemEqual(afterStack) || beforeStack.isEmpty() || afterStack.isEmpty()) {
            Item item = beforeStack.isEmpty() ? afterStack.getItem() : beforeStack.getItem();
            transactions.add(new Pair<>(item, afterStack.getCount() - beforeStack.getCount()));
          } else {
            transactions.add(new Pair<>(beforeStack.getItem(), -beforeStack.getCount()));
            transactions.add(new Pair<>(afterStack.getItem(), afterStack.getCount()));
          }
       }
    }

    getTransactions().addAll(transactions);
  }
}