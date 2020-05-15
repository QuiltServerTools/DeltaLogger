package dev.lambdacraft.watchtower;

import java.util.List;
import java.util.UUID;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface IPlayerLoader {
  List<DefaultedList<ItemStack>> loadPlayerInventory(UUID uuid);
}