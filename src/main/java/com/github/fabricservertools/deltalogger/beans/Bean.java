package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.world.World;

public interface Bean {
    abstract void rollback(World world);
}
