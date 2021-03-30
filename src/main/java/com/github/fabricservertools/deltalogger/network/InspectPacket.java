package com.github.fabricservertools.deltalogger.network;

import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.beans.Transaction;
import com.github.fabricservertools.deltalogger.dao.DAO;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class InspectPacket {
    public static final Identifier INSPECT_PACKET = new Identifier("deltalogger", "inspect");
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(INSPECT_PACKET, ((server, player, handler, buf, responseSender) -> {
            if (!player.hasPermissionLevel(3) && !Permissions.check(player, "deltalogger.inspect") && Permissions.check(player, "deltalogger.all")) return;
            BlockPos pos = new BlockPos(buf.getInt(0), buf.getInt(1), buf.getInt(2));
            if(player.getEntityWorld().getBlockEntity(pos) != null) {
                DAO.transaction.getTransactionsAt(player.getEntityWorld().getRegistryKey().getValue(), pos, buf.getShort(3)).forEach(transaction -> sendToClient(transaction, player, pos));
                DAO.block.getLatestPlacementsAt(player.getEntityWorld().getRegistryKey().getValue(), pos, 0, buf.getShort(3)).forEach(placement -> sendToClient(placement, player));
            }
        }));
    }
    public static void sendToClient(Transaction transaction, ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, NetworkUtils.TRANSACTION_PACKET, NetworkUtils.setTransaction(transaction, pos));
    }
    public static void sendToClient(Placement placement, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, NetworkUtils.PLACEMENT_PACKET, NetworkUtils.setPlacement(placement));
    }
}
