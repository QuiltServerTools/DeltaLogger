package com.github.fabricservertools.deltalogger.command.rollback;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RollbackUtils {
    public static Item getItem(Identifier id) {
        return Registry.ITEM.get(id);
    }

    public static Block getBlock(Identifier id) {
        return Registry.BLOCK.get(id);
    }
    public static Identifier createIdentifier(String identifier) {
        String[] identifierSplit = identifier.split(":");
        return new Identifier(identifierSplit[0], identifierSplit[1]);
    }
}
