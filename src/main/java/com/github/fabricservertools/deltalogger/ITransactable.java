package com.github.fabricservertools.deltalogger;

import net.minecraft.item.Item;
import net.minecraft.util.Pair;

import java.util.List;

public interface ITransactable {
	List<Pair<Item, Integer>> getTransactions();
}