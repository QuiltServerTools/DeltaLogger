package com.github.fabricservertools.deltalogger.util;

import com.github.fabricservertools.deltalogger.dao.DAO;

import org.lwjgl.system.CallbackI.D;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerUtils {
    public static String checkPlayer(ServerPlayerEntity player){
        if (player!=null){
            String sql = "AND playerid = "+DAO.player.getPlayerByUUID(player.getUuid());
            return sql;
        }
        return "";
    }
}
