package com.github.fabricservertools.deltalogger.network;

import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.beans.TransactionPos;
import com.github.fabricservertools.deltalogger.command.search.CriteriumParser;
import com.github.fabricservertools.deltalogger.command.search.SearchCommand;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;

/*
* Should be registered on the client using the S2C identifier, parsing as per the wiki docs
 */

public class SearchPacket {
	public static final Identifier PACKET_IDENTIFIER_C2S = new Identifier("deltalogger", "search_c2s_packet");
	public static final Identifier PACKET_IDENTIFIER_S2C = new Identifier("deltalogger", "search_s2c_packet");

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET_IDENTIFIER_C2S, ((server, player, handler, buf, responseSender) -> {
			if (!player.hasPermissionLevel(3) && !Permissions.check(player, "deltalogger.search")) return;
			HashMap<String, Object> propertyMap;
			try {
				propertyMap = CriteriumParser.getInstance().rawProperties(buf.readString());
			} catch (CommandSyntaxException e) {
				player.sendSystemMessage(new TranslatableText("deltalogger.network.failed_to_parse"), Util.NIL_UUID);
				return;
			}
			try {
				SearchCommand.readAdvanced(server.getCommandSource().withEntity(player), propertyMap, true);
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
		}));
	}

	public static void sendToClient(Placement placement, ServerPlayerEntity player) {
		PacketByteBuf buf = NetworkUtils.setPlacement(placement);
		ServerPlayNetworking.send(player, PACKET_IDENTIFIER_S2C, buf);
	}
	public static void sendToClient(TransactionPos transactionPos, ServerPlayerEntity player) {
		PacketByteBuf buf = NetworkUtils.setTransaction(transactionPos);
		ServerPlayNetworking.send(player, PACKET_IDENTIFIER_S2C, buf);
	}

}
