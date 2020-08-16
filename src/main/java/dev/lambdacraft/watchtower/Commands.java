package dev.lambdacraft.watchtower;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.collection.DefaultedList;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * WatchTower admin commands
 */
public class Commands {
  private Commands() {}
  private static HashMap<PlayerEntity, Boolean> toolMap = new HashMap<>();

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
      literal("wt")
        .requires(source -> source.hasPermissionLevel(4))
        .then(literal("tool").executes(Commands::toggleTool))
        .then(literal("takeitems")
          .then(argument("player", word()).executes(Commands::takeItems)))
    );
  }

  public static boolean hasToolEnabled(PlayerEntity p) {
    return toolMap.getOrDefault(p, false);
  }

  /**
   * Triggers tool inspection mode
   */
  private static int toggleTool(CommandContext<ServerCommandSource> ctx) {
    PlayerEntity p;
    try {
      p = ctx.getSource().getPlayer();
      boolean mode = !toolMap.getOrDefault(p, false);
      toolMap.put(p, mode);
      p.sendMessage(new LiteralText("WatchTower tool " + (mode ? "on" : "off")), true);
    } catch (CommandSyntaxException e) {
      e.printStackTrace();
    }
    return 1;
  }

  /**
   * Take a list of item stacks to drop at a player
   * @param player
   * @param combinedInventory
   */
  public static void dropItemsAtPlayer(PlayerEntity player, List<DefaultedList<ItemStack>> combinedInventory) {
    Iterator<DefaultedList<ItemStack>> var1 = combinedInventory.iterator();

    while(var1.hasNext()) {
       List<ItemStack> list = var1.next();

       for(int i = 0; i < list.size(); ++i) {
          ItemStack itemStack = (ItemStack)list.get(i);
          if (!itemStack.isEmpty()) {
             player.dropItem(itemStack, true, false);
             list.set(i, ItemStack.EMPTY);
          }
       }
    }
  }

  /**
   * Command to duplicate items of player and spit them at the feet of the command
   * source entity. Mainly intended foor getting banned player's inventory.
   * @param ctx
   * @return
   * @throws CommandSyntaxException
   */
  private static int takeItems(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    try {
      PlayerEntity caller = ctx.getSource().getPlayer();
      MinecraftServer server = ctx.getSource().getMinecraftServer();
      String toLookup = StringArgumentType.getString(ctx, "player");
      UUID uuid = null;
      caller.sendMessage(new LiteralText("this command is broken for now :("), true);
      return 1;
      // IPlayerLoader loader = (IPlayerLoader)(Object)server.getWorld(caller.dimension).getSaveHandler();
      // try {
      //   uuid = UUID.fromString(toLookup);
      // } catch (IllegalArgumentException e) {}
      // GameProfile profile = uuid == null
      //   ? server.getUserCache().findByName(toLookup)
      //   : server.getUserCache().getByUuid(uuid);
      // if (profile == null) {
      //   caller.sendMessage(new LiteralText("No player found for that name/id"), true);
      //   return 1;
      // }
  
      // List<DefaultedList<ItemStack>> inventory = loader.loadPlayerInventory(profile.getId());
      // dropItemsAtPlayer(caller, inventory);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 1;
  }

}