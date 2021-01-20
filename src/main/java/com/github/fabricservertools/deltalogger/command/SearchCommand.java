package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SearchCommand {
    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> searchNode = literal("search")
                .then(argument("criteria", StringArgumentType.greedyString()).suggests(CriteriumParser.getInstance())
                        .executes(context -> search(context, StringArgumentType.getString(context, "criteria"))))
                .build();

        root.addChild(searchNode);
    }
    /*
    *   Prepares the reading by collecting the custom search statement
    */
    private static int search(CommandContext<ServerCommandSource> context, String criteria)
            throws CommandSyntaxException {
        HashMap<String, Object> propertyMap;
        propertyMap = CriteriumParser.getInstance().rawProperties(criteria);
        readAdvanced(context.getSource(), propertyMap);
        return 1;
    }
    /*
    *   Monstrosity of a method for building the WHERE section of a query
    *   Should probably split into smaller methods at some point
    */
    public static void readAdvanced(ServerCommandSource scs, HashMap<String, Object> propertyMap)
            throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = scs.getPlayer();
        String sqlPlace = "";
        String sqlContainer = "";
        if (propertyMap.containsKey("target")) {
            GameProfileArgumentType.GameProfileArgument targets = (GameProfileArgumentType.GameProfileArgument) propertyMap
                    .get("targets");
            sqlPlace += "AND player_id = (SELECT  id FROM players WHERE uuid = "
                    + targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray() + ") ";
            sqlContainer += "AND player_id = (SELECT  id FROM players WHERE uuid = "
                    + targets.getNames(scs).stream().map(gp -> gp.getId().toString()).toArray() + ") ";
        }
        if (propertyMap.containsKey("block")) {
            BlockStateArgument block = (BlockStateArgument) propertyMap.get("block");
            sqlPlace += "AND type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "\") ";
            sqlContainer += "AND CT.item_type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "\") ";
        }
        if (propertyMap.containsKey("range")) {
            int range = (Integer) propertyMap.get("range");
            BlockPos playerPos = sourcePlayer.getBlockPos();
            // runner.fillParameter("range", x, x, y, y, z, z, range);
            sqlPlace += rangeStatementBuilder(playerPos, range);
        }
        Identifier dimension;
        if(propertyMap.containsKey("dimension")) {
            dimension = (Identifier) (propertyMap.get("dimension"));
        }
        else {
            dimension = sourcePlayer.getEntityWorld().getRegistryKey().getValue();
        }
        // Add to query searching in only one dimension
        sqlPlace += "AND dimension_id = (SELECT id FROM registry WHERE `name` = \""+dimension+"\") ";
        sqlContainer += "AND dimension_id = (SELECT id FROM registry WHERE `name` = \""+dimension+"\") ";

        // Check for an action and only query the relevant tables
        if (propertyMap.containsKey("action")) {
            String action = (String) propertyMap.get("action");
            if (!action.contains("everything")) {

                if (action.contains("placed")) {
                    sqlPlace += "AND placed = 1 ";
                    sendPlacements(scs, sqlPlace);
                } else if (action.contains("broken")) {
                    sqlPlace += "AND placed = 0 ";
                    sendPlacements(scs, sqlPlace);
                } else if (action.contains("added")) {
                    sqlContainer += "AND item_count > 0 ";
                    sendTransactions(scs, sqlContainer);
                } else if (action.contains("removed")) {
                    sqlContainer += "AND item_count < 0 ";
                    sendTransactions(scs, sqlContainer);
                }
            }
        } else {
            sendTransactions(scs, sqlContainer);
            sendPlacements(scs, sqlPlace);

        }
    }

    /*
    *   Takes the custom WHERE statement and queries the database for transactions, prints results to chat
    */
    private static void sendTransactions(ServerCommandSource scs, String sqlContainer) throws CommandSyntaxException {
        MutableText transactionMessage = DAO.transaction
                .search(scs.getPlayer().getEntityWorld().getRegistryKey().getValue(), 10, sqlContainer).stream()
                .map(p -> p.getText()).reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                .map(txt -> Chat.concat("\n", Chat.text("Transaction history"), txt))
                .orElse(Chat.text("No transactions found with the terms specified"));
        scs.getPlayer().sendSystemMessage(transactionMessage, Util.NIL_UUID);
    }

    /*
    *   Takes the custom WHERE statement and queries the database for placements, prints results to chat
    */
    private static void sendPlacements(ServerCommandSource scs, String sqlPlace) throws CommandSyntaxException {
        MutableText placementMessage = DAO.block
                .search(0, 10, sqlPlace).stream()
                .map(p -> p.getText()).reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                .map(txt -> Chat.concat("\n", Chat.text("Placement history"), txt))
                .orElse(Chat.text("No placements found with the terms specified"));
        scs.getPlayer().sendSystemMessage(placementMessage, Util.NIL_UUID);
    }

    /*
    *   Does the maths for calculating ranges, returns the prepared String
    */
    private static String rangeStatementBuilder(BlockPos pos, int range) {
        return "";
    }
}