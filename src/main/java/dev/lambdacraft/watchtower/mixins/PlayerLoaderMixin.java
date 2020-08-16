package dev.lambdacraft.watchtower.mixins;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import dev.lambdacraft.watchtower.subclass.IterablePlayerInventory;
import com.mojang.datafixers.DataFixer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.lambdacraft.watchtower.IPlayerLoader;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.WorldSaveHandler;

@Mixin(WorldSaveHandler.class)
public abstract class PlayerLoaderMixin implements IPlayerLoader {
  private static final Logger LOG = LogManager.getLogger();

  @Shadow
  DataFixer dataFixer;
  @Shadow
  private File playerDataDir;

  /**
   * Method to get a listing all of a player's inventory from their save state,
   * including their enderchest.
   */
  @Override
  public List<DefaultedList<ItemStack>> loadPlayerInventory(UUID uuid) {
    CompoundTag compoundTag = null;

    try {
      File file = new File(this.playerDataDir, uuid.toString() + ".dat");
      if (file.exists() && file.isFile()) {
        compoundTag = NbtIo.readCompressed(new FileInputStream(file));
      }
    } catch (Exception var4) {
      LOG.warn("Failed to load player data for {}", uuid.toString());
    }

    IterablePlayerInventory inventory = new IterablePlayerInventory(null);
    ListTag listTag = compoundTag.getList("Inventory", 10);
    inventory.deserialize(listTag);
    inventory.selectedSlot = compoundTag.getInt("SelectedItemSlot");

    EnderChestInventory enderChestInventory = new EnderChestInventory();
    if (compoundTag.contains("EnderItems", 9)) {
      enderChestInventory.readTags(compoundTag.getList("EnderItems", 10));
    }
    DefaultedList<ItemStack> enderChestStacks = DefaultedList.ofSize(enderChestInventory.size(), ItemStack.EMPTY);
    for (int i = 0; i < enderChestInventory.size(); i++) {
      ItemStack itemStack = enderChestInventory.getStack(i);
      if (!itemStack.isEmpty()) {
        enderChestStacks.set(i, itemStack);
      }
    }

    List<DefaultedList<ItemStack>> ret = inventory.getCombinedInventoryList();
    ret.add(enderChestStacks);

    return ret;
  }
}