package dev.lambdacraft.watchtower.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.util.ChatPrint;

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

    private static int search(CommandContext<ServerCommandSource> context, String criteria) throws CommandSyntaxException {
        HashMap<String, Object> propertyMap = CriteriumParser.getInstance().rawProperties(criteria);
        /*DbConn.readAdvanced(context.getSource(), propertyMap);
        TODO Happy implementing :p
        */
        return 1;
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
}
