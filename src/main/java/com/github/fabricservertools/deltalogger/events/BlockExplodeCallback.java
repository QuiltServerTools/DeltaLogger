package com.github.fabricservertools.deltalogger.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public interface BlockExplodeCallback {
    Event<BlockExplodeCallback> EVENT = EventFactory.createArrayBacked(BlockExplodeCallback.class,
            (listeners) -> (world, pos, block, explosion) -> {
                for (BlockExplodeCallback listener : listeners) {
                    ActionResult result = listener.explode(world, pos, block, explosion);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult explode(World world, BlockPos pos, Block block, Explosion explosion);
}
