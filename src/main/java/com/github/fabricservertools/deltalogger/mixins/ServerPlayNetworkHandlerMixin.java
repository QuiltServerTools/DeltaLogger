package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.ItemUtils;
import com.github.fabricservertools.deltalogger.NbtUuid;
import com.github.fabricservertools.deltalogger.dao.TransactionDAO;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hooks to log the transaction events with inventory screens
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    private UUID screenHandlerUUID;
    private List<Pair<Item, Integer>> modified = new ArrayList<>();
    private ItemStack[] tracked;

    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At(value = "HEAD"), method = "onCloseHandledScreen")
    public void close(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
        commit();
    }

    @Inject(at = @At(value = "HEAD"), method = "disconnect")
    public void disconnect(Text reason, CallbackInfo info) {
        commit();
    }

    private void commit() {
        if (this.screenHandlerUUID != null) {
            ItemUtils.compressTransactions(this.modified).forEach((item, count) -> {
                if (count == 0) return;
                Identifier id = Registry.ITEM.getId(item);
                DatabaseManager.getSingleton().queueOp(TransactionDAO.insert(
                        player.getUuid(),
                        screenHandlerUUID,
                        java.time.Instant.now(),
                        id,
                        count,
                        null
                ));
            });

            this.modified.clear();
            this.screenHandlerUUID = null;
        }
    }

    @Inject(at = @At("HEAD"), method = "onClickSlot")
    public void onSlotClickHead(ClickSlotC2SPacket packet, CallbackInfo info) {
        UUID uuid = ((NbtUuid) player.currentScreenHandler).getNbtUuid();

        if (uuid != this.screenHandlerUUID) {
            commit();
            tracked = null;
        }

        this.screenHandlerUUID = uuid;

        if (uuid != null) {
            List<ItemStack> stacks = player.currentScreenHandler.getStacks();
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
    }

    @Inject(at = @At("RETURN"), method = "onClickSlot")
    public void onSlotClickReturn(ClickSlotC2SPacket packet, CallbackInfo info) {
        UUID uuid = ((NbtUuid) player.currentScreenHandler).getNbtUuid();
        if (uuid != null) {
            for (int i = 0; i < this.tracked.length; ++i) {
                ItemStack current = player.currentScreenHandler.getSlot(i).getStack();
                ItemStack prev = tracked[i];
                if (!ItemStack.areEqual(prev, current)) {
                    if (prev.isItemEqual(current) || current.isEmpty() || prev.isEmpty()) {
                        // if same item then subtract and do transaction
                        Item item = prev.isEmpty() ? current.getItem() : prev.getItem();
                        modified.add(new Pair<>(item, current.getCount() - prev.getCount()));
                    } else {
                        // else treat as item swap
                        modified.add(new Pair<>(prev.getItem(), -prev.getCount()));
                        modified.add(new Pair<>(current.getItem(), current.getCount()));
                    }
                    tracked[i] = current.copy();
                }
            }
        }
    }
}