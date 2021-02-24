package com.github.fabricservertools.deltalogger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.util.HashMap;

import static net.minecraft.util.Formatting.*;

/**
 * Utility class to make sending chat messages easier
 */
public class Chat {
	private static final HashMap<Class, Formatting> textColorMap;

	static {
		textColorMap = new HashMap<>();
		textColorMap.put(Instant.class, GRAY);
		textColorMap.put(Identifier.class, YELLOW);
		textColorMap.put(Integer.class, GOLD);
	}

	public static void send(PlayerEntity p, String s) {
		((ServerPlayerEntity) p).sendMessage(new LiteralText(s), false);
	}

	public static void send(PlayerEntity p, Text text) {
		((ServerPlayerEntity) p).sendMessage(text, false);
	}

	public static void send(PlayerEntity p, MutableText... texts) {
		((ServerPlayerEntity) p).sendMessage(joinText(texts), false);
	}

	public static TranslatableText text(String s) {
		return new TranslatableText(s);
	}

	public static MutableText format(MutableText t, Formatting f) {
		return t.setStyle(Style.EMPTY.withFormatting(f));
	}

	public static MutableText format(String s, Formatting f) {
		return format(text(s), f);
	}

	public static MutableText format(Object o) {
		if (textColorMap.containsKey(o.getClass())) {
			return format(stringFrom(o), textColorMap.get(o.getClass()));
		}
		return text(stringFrom(o));
	}

	public static MutableText joinText(MutableText... texts) {
		MutableText out = new LiteralText("");
		for (int i = 0; i < texts.length; i++) {
			out.append(" ").append(texts[i]);
		}
		return out;
	}

	public static MutableText concat(String sep, MutableText t1, MutableText t2) {
		t1.append(text(sep)).append(t2);
		return t1;
	}

	public static String stringFrom(Object o) {
		if (o.getClass() == BlockPos.class) {
			BlockPos pos = (BlockPos) o;
			return "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
		}
		if (o.getClass() == Instant.class) {
			return SQLUtils.instantToUTCString((Instant) o);
		}
		if (o.getClass() == Identifier.class) {
			Identifier id = (Identifier) o;
			return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
		}
		return o.toString();
	}
}