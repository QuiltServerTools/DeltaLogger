package com.github.fabricservertools.deltalogger.network.client;

import com.github.fabricservertools.deltalogger.DeltaLogger;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CheckClientStatusPacket {
	public static final Identifier PACKET_IDENTIFIER = new Identifier("deltalogger", "check_client_status");
	public static void sendToClient(ServerPlayerEntity player) {
		if(player.getEntityWorld().isClient) return;
		ServerPlayNetworking.send(player, PACKET_IDENTIFIER, PacketByteBufs.empty());
	}
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(PACKET_IDENTIFIER, (client, handler, buf, responseSender) -> {
			sendToServer(responseSender);
		});
	}
	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET_IDENTIFIER, ((server, player, handler, buf, responseSender) -> {
			DeltaLogger.LOG.info(player.getName().asString() + " joined with DeltaLogger client");
		}));
	}
	protected static void sendToServer(PacketSender responseSender) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true);
		responseSender.sendPacket(PACKET_IDENTIFIER, buf);
	}
}
