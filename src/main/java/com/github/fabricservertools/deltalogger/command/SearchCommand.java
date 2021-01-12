package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.BlockDAO;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.ChatPrint;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SearchCommand {
    boolean defaultDim;
    private static ChatPrint chat = new ChatPrint();
    private static HashMap<PlayerEntity, Boolean> toolMap = new HashMap<>();
    public static void register(LiteralCommandNode root) {
        LiteralCommandNode<ServerCommandSource> searchNode =
                literal("search").then(argument("criteria", StringArgumentType.greedyString()).suggests(CriteriumParser.getInstance())
                        .executes(context -> search(context, StringArgumentType.getString(context, "criteria"))))
                        .build();

        root.addChild(searchNode);
    }

    private static int search(CommandContext<ServerCommandSource> context, String criteria) throws CommandSyntaxException{
        HashMap<String, Object> propertyMap;
        propertyMap = CriteriumParser.getInstance().rawProperties(criteria);
        readAdvanced(context.getSource(), propertyMap);
        return 1;
    }
    public static void readAdvanced(ServerCommandSource scs, HashMap<String, Object> propertyMap)
            throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = scs.getPlayer();
        String sqlPlace = "";
        String sqlContainer = "";
        /*"FROM (",
              "WHERE placements.id < ", sqlPlaceUtils.offsetOrZeroLatest("placements", "placements.id", idOffset),
              "AND x = :x AND y = :y AND z = :z AND dimension_id = (SELECT id FROM registry WHERE `name` = :dim)",
              "ORDER BY `id` DESC LIMIT :lim",
            ") as PL"*/ 
        if (propertyMap.containsKey("target")) {
            GameProfileArgumentType.GameProfileArgument targets = (GameProfileArgumentType.GameProfileArgument)propertyMap.get("targets");
            //runner.fillParameter("targets", targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray());
            sqlPlace += " AND player_id = (SELECT  id FROM players WHERE uuid = "+ targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray() + ")";
            sqlContainer += " AND player_id = (SELECT  id FROM players WHERE uuid = "+ targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray()+")";
        }
        if (propertyMap.containsKey("block")) {
            BlockStateArgument block = (BlockStateArgument)propertyMap.get("block");
            //runner.fillParameter("block", Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "%");
            sqlPlace += " AND block = (SELECT id FROM registry WHERE `name` = "+Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + ")";
            sqlContainer += " AND item = (SELECT id FROM registry WHERE `name` = "+Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + ")";
        }
        if (propertyMap.containsKey("range")) {
            int range = (Integer)propertyMap.get("range");
            range *= range;
            BlockPos playerPos = sourcePlayer.getBlockPos();
            int x = playerPos.getX();
            int y = playerPos.getY();
            int z = playerPos.getZ();
            //runner.fillParameter("range", x, x, y, y, z, z, range);
        }
        if (propertyMap.containsKey("action")) {
            String action = (String)propertyMap.get("action");
            if(!action.contains("everything")) {

                if(action.contains("placed")) {
                    sqlPlace += " AND placed = ";
                    sqlPlace += "1";
                    send(scs, sqlPlace);
                } else if (action.contains("broken")) {
                    sqlPlace += " AND placed = ";
                    sqlPlace += "0";
                    send(scs, sqlPlace);
                } else if (action.contains("added")) {
                    sqlContainer += " AND placed = ";
                    sqlContainer += " AND placed = ";
                    sendTransactions(scs, sqlContainer);
                }
                else if (action.contains("removed")) {
                    sqlContainer += " AND placed = ";
                    sqlContainer += " AND placed = ";
                    sendTransactions(scs, sqlContainer);
                }
            }
        }
        else{
            sendTransactions(scs, sqlContainer);
            send(scs, sqlPlace);

        }
    }
    private static void sendTransactions(ServerCommandSource scs, String sqlContainer) throws CommandSyntaxException{
        MutableText transactionMessage = DAO.transaction
            .search(scs.getPlayer().getEntityWorld().getRegistryKey().getValue(), 10, sqlContainer).stream()
            .map(p -> p.getText())
            .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
            .map(txt -> Chat.concat("\n", Chat.text("Transaction history"), txt))
            .orElse(Chat.text("No results found with the terms specified"));
            scs.getPlayer().sendSystemMessage(transactionMessage, Util.NIL_UUID);
    }
    private static void send(ServerCommandSource scs, String sqlPlace) throws CommandSyntaxException{
            MutableText placementMessage = DAO.block
            .search(scs.getPlayer().getEntityWorld().getRegistryKey().getValue(), 0, 10, sqlPlace).stream()
            .map(p -> p.getText())
            .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
            .map(txt -> Chat.concat("\n", Chat.text("Placement history"), txt))
            .orElse(Chat.text("No results found with the terms specified"));
            scs.getPlayer().sendSystemMessage(placementMessage, Util.NIL_UUID);
    }
}
//    private static int search(CommandContext<ServerCommandSource> context, @Nullable BlockState block, int range) throws CommandSyntaxException {
//        ServerCommandSource source = context.getSource();
//        ServerPlayerEntity player = source.getPlayer();
//
//        LoggedEventType eventType;
//        String actionString = StringArgumentType.getString(context, "action");
//        if (actionString.equalsIgnoreCase("everything")) {
//            eventType = null;
//        } else {
//            eventType = LoggedEventType.valueOf(actionString.toLowerCase());
//        }
//
//        String dimension = PlayerUtils.getPlayerDimension(player);
//        Collection<ServerPlayerEntity> targets = EntityArgumentType.getOptionalPlayers(context, "targets");
//
//        DbConn.readAdvanced(source, eventType, dimension, targets, block, range);
//        return 1;
//    }
