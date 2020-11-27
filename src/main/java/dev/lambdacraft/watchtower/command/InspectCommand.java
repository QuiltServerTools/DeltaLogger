package dev.lambdacraft.watchtower.command;

import java.util.HashMap;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.util.ChatPrint;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
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
                .literal("inspect").executes(context -> toggleTool(context)).then(
                        CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(context -> inspect(context, null))
                                /*.then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(context -> inspect(context,
                                                DimensionArgumentType.getDimensionArgument(context, "dimension")))*/)
                .build();

        root.addChild(inspectNode);
    }

    private static int toggleTool(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity p;
        try {
          p = ctx.getSource().getPlayer();
          boolean mode = !toolMap.getOrDefault(p, false);
          toolMap.put(p, mode);
          chat.sendMessage(p, "WatchTower tool " + (mode ? "on" : "off"), Formatting.GREEN);
        } catch (CommandSyntaxException e) {
          e.printStackTrace();
        }
        return 1;
      }

    public static boolean hasToolEnabled(PlayerEntity p) {
        return toolMap.getOrDefault(p, false);
    }

    private static int inspect(CommandContext<ServerCommandSource> scs, BlockPos pos){
        DatabaseManager dm = DatabaseManager.getSingleton();
        World world = scs.getSource().getWorld();
        Identifier dimension = world.getRegistryKey().getValue();
        dm.getPlacementsAt(dimension, pos, 10);
        return 1;
      }
}
