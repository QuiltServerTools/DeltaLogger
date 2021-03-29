package com.github.fabricservertools.deltalogger.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * Wrapper class to identify an item including its type {@link Item} and NBT data {@link CompoundTag},
 * but not the number of items in a stack of that item.
 */
public class TaggedItem {
    private final Item item;
    private final CompoundTag tag;

    public TaggedItem(Item item, CompoundTag tag) {
        this.item = Objects.requireNonNull(item);
        this.tag = tag; // Nullable
    }

    public static TaggedItem fromStack(ItemStack is) {
        return new TaggedItem(is.getItem(), is.getTag());
    }

    public Item getItem() {
        return item;
    }

    public CompoundTag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaggedItem that = (TaggedItem) o;

        if (!item.equals(that.item)) return false;
        return Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
