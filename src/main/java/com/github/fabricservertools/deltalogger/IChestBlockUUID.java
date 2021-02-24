package com.github.fabricservertools.deltalogger;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public interface IChestBlockUUID {
	Optional<UUID> getNbtUuidAt(final BlockState state, final World world, final BlockPos pos);
}