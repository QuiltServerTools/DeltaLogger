package com.github.fabricservertools.deltalogger.command;

import java.util.HashMap;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.ChatPrint;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class InspectCommand {
    boolean defaultDim;
    private static ChatPrint chat = new ChatPrint();
    private static HashMap<PlayerEntity, Boolean> toolMap = new HashMap<>();

    public InspectCommand(boolean defaultDim) {
        this.defaultDim = defaultDim;
    }

    public static void register(LiteralCommandNode root) {
        LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager
                .literal("inspect").executes(context -> toggleTool(context))
                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .executes(context -> inspect(context, null))
                            .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .executes(context -> inspect(
                                    context,
                                    DimensionArgumentType.getDimensionArgument(context, "dimension")))
                                ))
        .build();

        root.addChild(inspectNode);
    }

    private static int toggleTool(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity p;
        try {
          p = ctx.getSource().getPlayer();
          boolean mode = !toolMap.getOrDefault(p, false);
          toolMap.put(p, mode);
          p.sendMessage(new LiteralText("WatchTower inspector mode " + (mode ? "on" : "off")), true);
        } catch (CommandSyntaxException e) {
          e.printStackTrace();
        }
        return 1;
      }

    public static boolean hasToolEnabled(PlayerEntity p) {
        return toolMap.getOrDefault(p, false);
    }

    private static int inspect(CommandContext<ServerCommandSource> scs, ServerWorld dim) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(scs, "pos");
        World world = scs.getSource().getWorld();

        // Use player current dim otherwise if null arg
        Identifier dimension = dim == null ? world.getRegistryKey().getValue() : dim.getRegistryKey().getValue();

        MutableText placementMessage = DAO.block
            .getLatestPlacementsAt(dimension, pos, 0, 10).stream()
            .map(p -> p.getText())
            .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
            .map(txt -> Chat.concat("\n", Chat.text("Placement history"), txt))
            .orElse(Chat.text("No placements found at " + pos.toString()));
        
        // FIXME: fix getting block from server console
        Chat.send(scs.getSource().getPlayer(), placementMessage);
        return 1;
      }
}
