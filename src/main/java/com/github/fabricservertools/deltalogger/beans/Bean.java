package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public abstract class Bean {
    abstract void rollback(World world);
    public Identifier createIdentifier(String identifier) {
        String[] identifierSplit = identifier.split(":");
        return new Identifier(identifierSplit[0], identifierSplit[1]);
    }
    public Item getItem(Identifier id) {
        return Registry.ITEM.get(id);
    }

    public Block getBlock(Identifier id) {
        return Registry.BLOCK.get(id);
    }
}
