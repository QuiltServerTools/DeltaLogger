package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.world.World;

public interface Bean {
    void rollback(World world);
}
