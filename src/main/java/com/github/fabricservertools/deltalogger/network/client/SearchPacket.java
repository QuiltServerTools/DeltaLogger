package com.github.fabricservertools.deltalogger.network.client;

import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.dao.DAO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/*
* Should be registered on the client using the S2C identifier, parsing as per the wiki docs
*
 */

public class SearchPacket {
	public static final Identifier PACKET_IDENTIFIER_C2S = new Identifier("deltalogger", "search_c2s_packet");
	public static final Identifier PACKET_IDENTIFIER_S2C = new Identifier("deltalogger", "search_s2c_packet");

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET_IDENTIFIER_C2S, ((server, player, handler, buf, responseSender) -> {
			if (!player.hasPermissionLevel(2)) return;
			String where = buf.readString(1);
			if (where.contains(";") || where.contains("DROP") || where.contains("TRUNCATE"))
				throw new IllegalStateException("Attempted SQL injection");
			DAO.block.search(0, 100, where).forEach(placement -> {
				sendToClient(placement, player);
			});
		}));
	}

	public static void sendToServer(String where) {
		ClientPlayNetworking.send(PACKET_IDENTIFIER_C2S, PacketByteBufs.create().writeString(where));
	}


	public static void sendToClient(Placement placement, ServerPlayerEntity player) {
		String placementString = placement.getX() + "," + placement.getY() + "," + placement.getZ() + "," + placement.getBlockType() + "," + placement.getPlayerName() + "," + placement.getPlaced();
		PacketByteBuf buf = PacketByteBufs.create().writeString(placementString);
		ServerPlayNetworking.send(player, PACKET_IDENTIFIER_S2C, buf);
	}

}
