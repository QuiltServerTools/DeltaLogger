package com.github.fabricservertools.deltalogger;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.UUID;

public interface IPlayerLoader {
	List<DefaultedList<ItemStack>> loadPlayerInventory(UUID uuid);
}