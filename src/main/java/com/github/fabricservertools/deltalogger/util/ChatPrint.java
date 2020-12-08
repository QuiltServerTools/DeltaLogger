package com.github.fabricservertools.deltalogger.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class ChatPrint {
    public void sendMessage(PlayerEntity player, String message, Formatting format){
        LiteralText textMessage = new LiteralText(message);
        textMessage.formatted(format);
        player.sendSystemMessage(textMessage, Util.NIL_UUID);
    }
}
