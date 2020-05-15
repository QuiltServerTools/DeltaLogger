package dev.lambdacraft.watchtower;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IChestBlockUUID {
  Optional<UUID> getWatchTowerIdAt(final BlockState state, final World world, final BlockPos pos);
}