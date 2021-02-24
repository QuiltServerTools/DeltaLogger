package com.github.fabricservertools.deltalogger.util;

import com.github.fabricservertools.deltalogger.dao.DAO;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerUtils {
	public static String checkPlayerId(ServerPlayerEntity player) {
		if (player != null) {
			String sql = "AND playerid = " + DAO.player.getPlayerByUUID(player.getUuid());
			return sql;
		}
		return "";
	}

	public static boolean isPlayer(ServerPlayerEntity player) {
		if (player != null) return true;
		return false;
	}
}
