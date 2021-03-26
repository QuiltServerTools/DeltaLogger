package com.github.fabricservertools.deltalogger.command.search;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.beans.MobGrief;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.beans.TransactionPos;
import com.github.fabricservertools.deltalogger.command.DlPermissions;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.network.client.SearchPacket;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
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
                .requires(scs -> DlPermissions.checkPerms(scs, "deltalogger.search"))
                .then(argument("criteria", StringArgumentType.greedyString()).suggests(CriteriumParser.getInstance())
                        .executes(context -> search(context, StringArgumentType.getString(context, "criteria"))))
                .build();

        root.addChild(searchNode);
    }

    /*
     * Prepares the reading by collecting the custom search statement
     */
    private static int search(CommandContext<ServerCommandSource> context, String criteria)
            throws CommandSyntaxException {
        HashMap<String, Object> propertyMap;
        propertyMap = CriteriumParser.getInstance().rawProperties(criteria);
        readAdvanced(context.getSource(), propertyMap, false);
        return 1;
    }

    /*
     * Monstrosity of a method for building the WHERE section of a query
     * Should probably split into smaller methods at some point
     */
    public static void readAdvanced(ServerCommandSource scs, HashMap<String, Object> propertyMap, boolean returnPackets)
            throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = scs.getPlayer();

        String sqlPlace = "";
        String sqlContainer = "";
        String sqlGrief = "";
        if (propertyMap.containsKey("target")) {
            GameProfileArgumentType.GameProfileArgument targets = (GameProfileArgumentType.GameProfileArgument) propertyMap
                    .get("target");
            sqlPlace += "AND player_id = (SELECT id FROM players WHERE uuid = \""
                    + getUuid(targets, scs) + "\") ";
            sqlContainer += "AND player_id = (SELECT id FROM players WHERE uuid = \""
                    + getUuid(targets, scs) + "\") ";
            sqlGrief += "AND player_id = (SELECT  id FROM players WHERE uuid = \""
                    + getUuid(targets, scs) + "\") ";
        }
        if (propertyMap.containsKey("block")) {
            BlockStateArgument block = (BlockStateArgument) propertyMap.get("block");
            sqlPlace += "AND type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "\") ";
            sqlContainer += "AND CT.item_type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.BLOCK.getId(block.getBlockState().getBlock()).toString() + "\") ";
        }

        if (propertyMap.containsKey("item")) {
            ItemStackArgument item = (ItemStackArgument) propertyMap.get("item");
            Block block = Block.getBlockFromItem(item.getItem());
            sqlPlace += "AND CT.item_type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.BLOCK.getId(block).toString() + "\") ";
            sqlContainer += "AND CT.item_type = (SELECT id FROM registry WHERE `name` = \""
                    + Registry.ITEM.getId(item.getItem()).toString() + "\") ";
        }

        //range or pos
        if (propertyMap.containsKey("range")) {
            int range = (Integer) propertyMap.get("range");
            BlockPos playerPos = sourcePlayer.getBlockPos();
            sqlPlace += rangeStatementBuilder(playerPos, range);
            sqlContainer += rangeStatementBuilderTransaction(playerPos, range);
            sqlGrief += rangeStatementBuilder(playerPos, range);
        }

        Identifier dimension;
        if (propertyMap.containsKey("dimension")) {
            dimension = (Identifier) (propertyMap.get("dimension"));
        } else {
            dimension = sourcePlayer.getEntityWorld().getRegistryKey().getValue();
        }

        // Add to query searching in only one dimension
        sqlPlace += "AND dimension_id = (SELECT id FROM registry WHERE `name` = \"" + dimension + "\") ";
        sqlContainer += "AND dimension_id = (SELECT id FROM registry WHERE `name` = \"" + dimension + "\") ";
        sqlGrief += "AND dimension_id = (SELECT id FROM registry WHERE `name` = \"" + dimension + "\") ";

        // Get optional limit value
        int limit = 10;
        if (propertyMap.containsKey("limit")) {
            limit = (int) propertyMap.get("limit");
        }

        // Check for an action and only query the relevant tables
        if (propertyMap.containsKey("action")) {
            String action = (String) propertyMap.get("action");
            switch (action) {
                case "placed":
                    sqlPlace += "AND placed = 1 ";
                    sendPlacements(sourcePlayer, sqlPlace, limit, returnPackets);
                    break;
                case "broken":
                    sqlPlace += "AND placed = 0 ";
                    sendPlacements(sourcePlayer, sqlPlace, limit, returnPackets);
                    break;
                case "added":
                    sqlContainer += "AND item_count > 0 ";
                    sendTransactions(sourcePlayer, sqlContainer, limit, returnPackets);
                    break;
                case "taken":
                    sqlContainer += "AND item_count < 0 ";
                    sendTransactions(sourcePlayer, sqlContainer, limit, returnPackets);
                    break;
                case "grief":
                    sendGrief(sourcePlayer, sqlGrief, limit);
                    break;
                case "everything":
                    sendGrief(sourcePlayer, sqlGrief, limit);
                    sendTransactions(sourcePlayer, sqlContainer, limit, returnPackets);
                    sendPlacements(sourcePlayer, sqlPlace, limit, returnPackets);
                    break;
                default:
                    throw new SimpleCommandExceptionType(new LiteralMessage("Invalid action: " + action))
                            .create();
            }
        } else {
            sendTransactions(sourcePlayer, sqlContainer, limit, returnPackets);
            sendPlacements(sourcePlayer, sqlPlace, limit, returnPackets);

        }
    }

    /*
     * Takes the custom WHERE statement and queries the database for transactions,
     * prints results to chat
     */
    private static void sendTransactions(ServerPlayerEntity player, String sqlContainer, int limit, boolean returnPackets) {
        if (returnPackets) {
            DAO.transaction.search(limit, sqlContainer).forEach(transactionPos -> SearchPacket.sendToClient(transactionPos, player));
        } else {
            MutableText transactionMessage = DAO.transaction.search(limit, sqlContainer).stream()
                    .map(TransactionPos::getText).reduce((t1, t2) -> Chat.concat("\n", t1, t2))
                    .map(txt -> Chat.concat("\n", Chat.text("deltalogger.history.transaction"), txt))
                    .orElse(Chat.text("deltalogger.none_found.no_pos.transaction"));
            Chat.send(player, transactionMessage);
        }
    }

    /*
     * Takes the custom WHERE statement and queries the database for placements,
     * prints results to chat
     */
    private static void sendPlacements(ServerPlayerEntity player, String sqlPlace, int limit, boolean returnPackets) {
        if (returnPackets) {
            DAO.block.search(0, limit, sqlPlace).forEach(placement -> SearchPacket.sendToClient(placement, player));
        } else {
            MutableText placementMessage = DAO.block.search(0, limit, sqlPlace).stream().map(Placement::getTextWithPos)
                    .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                    .map(txt -> Chat.concat("\n", Chat.text("deltalogger.history.placement"), txt))
                    .orElse(Chat.text("deltalogger.none_found.no_pos.placement"));
            Chat.send(player, placementMessage);
        }
    }


    private static void sendGrief(ServerPlayerEntity player, String sqlPlace, int limit) {
        MutableText griefMessage = DAO.entity.searchGrief(0, limit, sqlPlace).stream().map(MobGrief::getTextWithPos)
                .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
                .map(txt -> Chat.concat("\n", Chat.text("deltalogger.history.grief"), txt))
                .orElse(Chat.text("deltalogger.none_found.no_pos.grief"));
        player.sendSystemMessage(griefMessage, Util.NIL_UUID);
    }

    /*
     * Does the maths for calculating ranges, returns the prepared String
     */
    private static String rangeStatementBuilder(BlockPos pos, int range) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return "AND x BETWEEN " + (x - range) + " AND " + (x + range) + " AND y BETWEEN " + (y - range) + " AND "
                + (y + range) + " AND z BETWEEN " + (z - range) + " AND " + (z + range) + " ";
    }

    private static String rangeStatementBuilderTransaction(BlockPos pos, int range) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return "AND C.x BETWEEN " + (x - range) + " AND " + (x + range) + " AND C.y BETWEEN " + (y - range) + " AND "
                + (y + range) + " AND C.z BETWEEN " + (z - range) + " AND " + (z + range) + " ";
    }

    public static String getUuid(GameProfileArgumentType.GameProfileArgument player, ServerCommandSource scs) throws CommandSyntaxException {
        return player.getNames(scs).stream().findFirst().isPresent() ? player.getNames(scs).stream().findFirst().get().getId().toString() : "";
    }
}