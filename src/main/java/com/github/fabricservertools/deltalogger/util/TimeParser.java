package com.github.fabricservertools.deltalogger.util;

import java.time.Duration;

public class TimeParser {
	private static class Pair<K, V> {
		K key;
		V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
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