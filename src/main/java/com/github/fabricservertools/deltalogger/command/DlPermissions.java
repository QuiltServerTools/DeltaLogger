package com.github.fabricservertools.deltalogger.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

public class DlPermissions {
    public static boolean checkPerms(ServerCommandSource scs, String permNode) {
        if(scs.hasPermissionLevel(3)) return true;
        if(Permissions.check(scs, permNode)) return true;
        if(Permissions.check(scs, "deltalogger.all")) return true;
        return false;
    }
}
