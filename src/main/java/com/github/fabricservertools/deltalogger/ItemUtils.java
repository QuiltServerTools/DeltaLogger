package com.github.fabricservertools.deltalogger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ItemUtils {
  public static final String NBT_TAG_KEY = "watchtowerid";
  public static HashMap<PlayerEntity, HashMap<Item, Integer>> openContainers;
  static {
    openContainers = new HashMap<>();
  }

  public static HashMap<Item, Integer> countItemsInContainer(ScreenHandler container) {
      // Get real container inventory size by subtracting player inventory size
      int invSize = container.getStacks().size() - 9 * 4;
      List<ItemStack> stacks = container.getStacks().subList(0, invSize);

      // Collect item counts on opening container
      HashMap<Item, Integer> itemCounts = new HashMap<>();
      stacks.forEach(stack -> {
        Item item = stack.getItem();
        int count = stack.getCount();
        if (Registry.ITEM.getRawId(item) == 0) return;
        int beforeCount = itemCounts.get(item) != null ? itemCounts.get(item) : 0;
        itemCounts.put(item, beforeCount + count);
      });

      return itemCounts;
  }

  /**
   * Get a - b
   */
  public static <T> Map<T, Integer> diff(Map<T, Integer> a, Map<T, Integer> b) {
    Map<T, Integer> diff = new HashMap<>();

    a.forEach((key, i) -> {
      Integer j = b.get(key);
      if (j == null) diff.put(key, i);
      else if (i != j) diff.put(key, i - j);
    });

    b.forEach((key, j) -> {
      if (!a.containsKey(key)) diff.put(key, -j);
    });

    return diff;
  }

  public static <T> Map<T, Integer> compressTransactions(
    List<Pair<T, Integer>> transactions
  ) {
    Map<T, Integer> compressed = new HashMap<>();

    transactions.forEach(pair -> {
      compressed.put(
        pair.getLeft(),
        compressed.getOrDefault(pair.getLeft(), 0) + pair.getRight()
      );
    });

    compressed.values().removeIf(v -> v == 0);

    return compressed;
  }

  public static Map<Item, Integer> itemStacksToTransactions(List<ItemStack> stacks) {
    Map<Item, Integer> transactions = new HashMap<>();

    stacks.forEach(stack -> {
      transactions.put(
        stack.getItem(),
        transactions.getOrDefault(stack.getItem(), 0) + stack.getCount()
      );
    });

    return transactions;
  }

  /**
   * Take a list of item stacks to drop at a player
   * @param player
   * @param combinedInventory
   */
  public static void dropItemsAtPlayer(PlayerEntity player, List<DefaultedList<ItemStack>> combinedInventory) {
    Iterator<DefaultedList<ItemStack>> var1 = combinedInventory.iterator();

    while(var1.hasNext()) {
       List<ItemStack> list = var1.next();

       for(int i = 0; i < list.size(); ++i) {
          ItemStack itemStack = (ItemStack)list.get(i);
          if (!itemStack.isEmpty()) {
             player.dropItem(itemStack, true, false);
             list.set(i, ItemStack.EMPTY);
          }
       }
    }
  }
}