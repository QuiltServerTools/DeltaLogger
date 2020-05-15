package dev.lambdacraft.watchtower.subclass;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.collection.DefaultedList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class IterablePlayerInventory extends PlayerInventory {
  public IterablePlayerInventory(PlayerEntity player) { super(player); }
  public List<DefaultedList<ItemStack>> getCombinedInventoryList() {
    ArrayList<DefaultedList<ItemStack>> ret = new ArrayList<>();
    ret.add(this.main);
    ret.add(this.armor);
    ret.add(this.offHand);
    return ret;
  }
}