package com.cloudstoragerei.core;

import java.util.Locale;

public final class CloudStorageLegacyHotkeys {
	private CloudStorageLegacyHotkeys() {
	}

	public static String create(String modifier, String mouseButton) {
		String mouseKey = mouseKey(mouseButton);

		if (mouseKey == null) {
			return null;
		}

		String modifierKey = modifierKey(modifier);
		return modifierKey.isEmpty() ? mouseKey : modifierKey + "," + mouseKey;
	}

	public static String modifierKey(String modifier) {
		if (modifier == null) {
			return "LEFT_SHIFT";
		}

		return switch (modifier.toUpperCase(Locale.ROOT)) {
			case "CTRL" -> "LEFT_CONTROL";
			case "ALT" -> "LEFT_ALT";
			case "NONE" -> "";
			default -> "LEFT_SHIFT";
		};
	}

	public static String mouseKey(String mouseButton) {
		if (mouseButton == null) {
			return null;
		}

		return switch (mouseButton.toUpperCase(Locale.ROOT)) {
			case "LEFT" -> "BUTTON_1";
			case "RIGHT" -> "BUTTON_2";
			case "MIDDLE" -> "BUTTON_3";
			default -> null;
		};
	}

	public static String openAmount(String mouseButton) {
		String mouseKey = mouseKey(mouseButton);

		if ("BUTTON_3".equals(mouseKey)) {
			return "LEFT_CONTROL,BUTTON_3";
		}

		return mouseKey;
	}
}
