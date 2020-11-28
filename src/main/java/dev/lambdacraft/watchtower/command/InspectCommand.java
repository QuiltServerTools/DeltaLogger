package dev.lambdacraft.watchtower.command;

import java.util.HashMap;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.lambdacraft.watchtower.Chat;
import dev.lambdacraft.watchtower.DatabaseManager;
import dev.lambdacraft.watchtower.util.ChatPrint;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
        DatabaseManager dm = DatabaseManager.getSingleton();
        BlockPos pos = BlockPosArgumentType.getBlockPos(scs, "pos");
        World world = scs.getSource().getWorld();

        // Use player current dim otherwise if null arg
        Identifier dimension = dim == null ? world.getRegistryKey().getValue() : dim.getRegistryKey().getValue();

        dm.getPlacementsAt(dimension, pos, 10)
            .stream()
            .map(p -> p.getText())
            .reduce((p1, p2) -> Chat.concat("\n", p1, p2))
            .ifPresent(msg -> {
                try {
                    // FIXME: fix getting block from server console
                    // FIXME: empty result has no message
                    Chat.send(scs.getSource().getPlayer(), Chat.concat("\n", Chat.text("Placement history"), msg));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            });
        return 1;
      }
}
