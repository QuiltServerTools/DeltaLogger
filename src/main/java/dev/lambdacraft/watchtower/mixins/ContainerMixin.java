package dev.lambdacraft.watchtower.mixins;

import java.util.ArrayList;
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
    if (watchTowerTransactions == null) {
      watchTowerTransactions = new ArrayList<>();
    }
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
      if (count == 0) return;
      Identifier id = Registry.ITEM.getId(item);
      // if (containerId == null) {
      //   LOG.warn(String.join("", "Unable to add container transaction for ", id.toString(), " by player ", player.getName().asString(), " skipping.."));
      //   return;
      // }
      DatabaseManager.getSingleton().queueOp(new DatabaseManager.ContainerTransaction(
        player.getUuid(), containerId, DatabaseManager.getTime(), id, count, null
      ));
    });
  }
}