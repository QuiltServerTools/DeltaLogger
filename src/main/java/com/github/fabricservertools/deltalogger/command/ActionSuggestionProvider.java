package com.github.fabricservertools.deltalogger.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class ActionSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        String current = builder.getRemaining().toLowerCase();

        builder.suggest("everything");
        
        for (EventTypes actions : EventTypes.values()) {
            if (actions.name().contains(current)) {
                builder.suggest(actions.name().toLowerCase());
            }
        }
        return builder.buildFuture();
    }
}
