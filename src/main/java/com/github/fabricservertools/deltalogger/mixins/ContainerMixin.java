package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.ITransactable;
import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import com.github.fabricservertools.deltalogger.dao.TransactionDAO;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hooks to log the transaction events with inventory screens
 */
@Mixin(ScreenHandler.class)
public abstract class ContainerMixin implements NbtUuid, ITransactable {
	private static final Logger LOG = LogManager.getLogger();

	private List<Pair<Item, Integer>> watchTowerTransactions = new ArrayList<>();

	public List<Pair<Item, Integer>> getTransactions() {
		if (watchTowerTransactions == null) {
			watchTowerTransactions = new ArrayList<>();
		}
		return watchTowerTransactions;
	}

	private UUID watchTowerId;

	public void setNbtUuid(UUID uuid) {
		watchTowerId = uuid;
	}

	public UUID getNbtUuid() {
		return watchTowerId;
	}

	@Shadow
	private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
	@Shadow
	public final List<Slot> slots = Lists.newArrayList();

	@Inject(at = @At(value = "HEAD"), method = "close")
	public void close(PlayerEntity player, CallbackInfo info) {
		UUID containerId = ((NbtUuid) this).getNbtUuid();
		if (containerId == null) return;

		ITransactable transactable = (ITransactable) this;
		Map<Item, Integer> transactions = ItemUtils.compressTransactions(transactable.getTransactions());
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
}