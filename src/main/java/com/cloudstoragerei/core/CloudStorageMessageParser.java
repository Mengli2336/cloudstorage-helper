package com.cloudstoragerei.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CloudStorageMessageParser {
	private static final Pattern ITEM_ENTRY_PATTERN = Pattern.compile(
			"\\(([a-z0-9_.-]+:[a-z0-9_./-]+)\\)x([0-9.]+)"
	);
	private static final String DIMENSION_SWITCH_TEXT = "你的存档已切换至";
	private static final String CLOUD_STORAGE_HEADER_TEXT = "你的云仓物品如下:";

	private CloudStorageMessageParser() {
	}

	public static ParsedMessage parse(String message) {
		boolean cloudStorageHeader = message.contains(CLOUD_STORAGE_HEADER_TEXT);
		boolean dimensionSwitch = message.contains(DIMENSION_SWITCH_TEXT);
		List<ItemEntry> entries = new ArrayList<>();

		if (message.contains("-") && message.contains("(") && message.contains(")")) {
			Matcher matcher = ITEM_ENTRY_PATTERN.matcher(message);

			while (matcher.find()) {
				try {
					long count = Math.round(Double.parseDouble(matcher.group(2)));
					entries.add(new ItemEntry(matcher.group(1), count));
				} catch (NumberFormatException ignored) {
					// Ignore malformed quantities and keep parsing other entries.
				}
			}
		}

		return new ParsedMessage(cloudStorageHeader, dimensionSwitch, List.copyOf(entries));
	}

	public record ItemEntry(String itemId, long count) {
	}

	public record ParsedMessage(
			boolean cloudStorageHeader,
			boolean dimensionSwitch,
			List<ItemEntry> entries
	) {
		public boolean hasParsedEntries() {
			return !this.entries.isEmpty();
		}
	}
}
