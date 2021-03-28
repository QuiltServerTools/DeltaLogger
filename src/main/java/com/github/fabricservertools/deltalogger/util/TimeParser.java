package com.github.fabricservertools.deltalogger.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class TimeParser {
    public static TimeParserSuggestor INSTANCE = new TimeParserSuggestor();

    private static class Pair<K, V> {
        K key;
        V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class TimeParserSuggestor implements SuggestionProvider<ServerCommandSource> {
        private enum time_units {
            S,//Seconds
            M,//Minutes
            H,//Hours
            D,//Days
            Y//Years
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
            String current = builder.getRemaining().toLowerCase();
            for (time_units actions : time_units.values()) {
                String name = actions.name().toLowerCase();
                if (current.endsWith(name)) {
                    return builder.buildFuture();
                }
                builder.suggest(current + name);
            }
            return builder.buildFuture();
        }
    }

    private static Pair<Integer, String> removeAndReturn(String s, char c) throws NumberFormatException {
        int index = s.indexOf(c);
        if (index != -1) {
            String s1 = s.substring(0, index);
            s = s.substring(index + 1);
            index = Integer.parseInt(s1);
            return new Pair(index, s);
        }

        return new Pair(0, s);
    }

    public static Duration parseTime(String formattedText) throws NumberFormatException {
        formattedText = formattedText.trim().replace(" ", "").toLowerCase();

        Duration duration = Duration.ZERO;

        Pair<Integer, String> pair = removeAndReturn(formattedText, 'y');
        duration = duration.plus(Duration.ofDays(pair.key * 365));
        formattedText = pair.value;

        pair = removeAndReturn(formattedText, 'd');
        duration = duration.plus(Duration.ofDays(pair.key));
        formattedText = pair.value;

        pair = removeAndReturn(formattedText, 'h');
        duration = duration.plus(Duration.ofHours(pair.key));
        formattedText = pair.value;

        pair = removeAndReturn(formattedText, 'm');
        duration = duration.plus(Duration.ofMinutes(pair.key));
        formattedText = pair.value;

        pair = removeAndReturn(formattedText, 's');
        duration = duration.plus(Duration.ofSeconds(pair.key));
        formattedText = pair.value;

        if (!formattedText.isEmpty()) {
            throw new IllegalStateException("Did not expect '" + formattedText + "'");
        }

        return duration;
    }
}