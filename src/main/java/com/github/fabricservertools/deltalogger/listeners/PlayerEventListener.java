package com.github.fabricservertools.deltalogger.listeners;

import com.github.fabricservertools.deltalogger.*;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.command.InspectCommand;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;
import com.github.fabricservertools.deltalogger.dao.ContainerDAO;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.dao.PlayerDAO;
import com.github.fabricservertools.deltalogger.events.BlockExplodeCallback;
import com.github.fabricservertools.deltalogger.events.BlockPlaceCallback;
import com.github.fabricservertools.deltalogger.events.PlayerOpenScreenCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PlayerEventListener {
    public PlayerEventListener() {
        ServerPlayConnectionEvents.JOIN.register(this::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onQuit);

        PlayerBlockBreakEvents.AFTER.register(this::onBreakFinished);
        UseBlockCallback.EVENT.register(this::onUseBlock);
        BlockPlaceCallback.EVENT.register(this::onBlockPlace);

        PlayerOpenScreenCallback.EVENT.register(this::onOpenScreen);

        BlockExplodeCallback.EVENT.register(this::onBlockExplode);
    }

    private ActionResult onOpenScreen(ServerPlayerEntity player, NamedScreenHandlerFactory screenHandlerFactory) {
        if (player.currentScreenHandler.slots.isEmpty()) return ActionResult.PASS;
        Inventory inventory = player.currentScreenHandler.getSlot(0).inventory;
        if (inventory instanceof LockableContainerBlockEntity) {
            BlockEntity be = (BlockEntity) inventory;
            UUID uuid = ((NbtUuid) be).getNbtUuid();
            ((NbtUuid) player.currentScreenHandler).setNbtUuid(uuid);

            Identifier dimId = player.getEntityWorld().getRegistryKey().getValue();

            Identifier blockId = Registry.BLOCK.getId(be.getCachedState().getBlock());
            DatabaseManager.getSingleton().queueOp(ContainerDAO.insert(
                    uuid, blockId, be.getPos(), player.getUuid(), java.time.Instant.now(), dimId
            ));
        }

        return ActionResult.PASS;
    }

    private ActionResult onBlockPlace(PlayerEntity playerEntity, ItemPlacementContext context) {
        try {
            BlockPos pos = context.getBlockPos();
            PlayerEntity player = context.getPlayer();
            World world = context.getWorld();
            BlockState bs = world.getBlockState(pos);
            Identifier id = Registry.BLOCK.getId(bs.getBlock());
            Identifier dimension = world.getRegistryKey().getValue();

            if (player == null) return ActionResult.PASS;

            DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
                    player.getUuid(),
                    id,
                    true,
                    pos,
                    bs,
                    dimension,
                    java.time.Instant.now()
            ));
        } catch (Exception e) {
            DeltaLogger.LOG.warn("Problem in placement");
            e.printStackTrace();
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;
        ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        if (!InspectCommand.hasToolEnabled(player)) {
            return ActionResult.PASS;
        }

        Block targetBlock = state.getBlock();
        BlockEntity be = world.getBlockEntity(pos);
        Identifier dimension = world.getRegistryKey().getValue();

        if (be instanceof LockableContainerBlockEntity) {
            Optional<UUID> opt;
            if (targetBlock instanceof ChestBlock) {
                opt = ((IChestBlockUUID) targetBlock).getNbtUuidAt(state, world, pos);
            } else {
                opt = Optional.of(((NbtUuid) be).getNbtUuid());
            }
            opt.ifPresent(uuid -> {
                MutableText transactionMessage = DAO.transaction.getTransactionsFromUUID(uuid, 10).stream()
                        .map(t -> t.getText()).reduce((t1, t2) -> Chat.concat("\n", t1, t2))
                        .map(txt -> Chat.concat("\n", Chat.text("Transaction History"), txt))
                        .orElse(Chat.text("No transactions found in container"));

                Chat.send(player, transactionMessage);
            });
        }

        MutableText placementMessage = DAO.block.getLatestPlacementsAt(dimension, pos, 0, 10).stream().map(Placement::getText)
                .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                .map(txt -> Chat.concat("\n", Chat.text("Placement history"), txt))
                .orElse(Chat.text("No placements found at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));

        Chat.send(player, placementMessage);


        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                -2,
                player.inventory.selectedSlot,
                player.inventory.getStack(player.inventory.selectedSlot)));
        return ActionResult.SUCCESS;
    }

    private ActionResult onBlockExplode(World world, BlockPos pos, Block block, Explosion explosion) {
        if (explosion.getCausingEntity() instanceof PlayerEntity) {
            DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
                    explosion.getCausingEntity().getUuid(), Registry.BLOCK.getId(block),
                    false, pos, world.getBlockState(pos), world.getRegistryKey().getValue(), Instant.now()
            ));
        }
        return ActionResult.PASS;
    }

    private void onJoin(ServerPlayNetworkHandler networkHandler, PacketSender sender, MinecraftServer server) {
        DatabaseManager.getSingleton()
                .queueOp(PlayerDAO.insert(
                        networkHandler.player.getUuid(),
                        networkHandler.player.getName().asString(),
                        java.time.Instant.now()
                ));
    }

    private void onQuit(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
        DatabaseManager.getSingleton()
                .queueOp(PlayerDAO.insert(
                        networkHandler.player.getUuid(),
                        networkHandler.player.getName().asString(),
                        java.time.Instant.now()
                ));
    }

    private void onBreakFinished(World world, PlayerEntity player, BlockPos pos,
                                 BlockState state, /* Nullable */ BlockEntity blockEntity) {
        Identifier id = Registry.BLOCK.getId(state.getBlock());
        Identifier dimension = world.getRegistryKey().getValue();

        DatabaseManager.getSingleton().queueOp(BlockDAO.insertPlacement(
                player.getUuid(),
                id,
                false,
                pos,
                state,
                dimension,
                java.time.Instant.now()
        ));
    }
}
