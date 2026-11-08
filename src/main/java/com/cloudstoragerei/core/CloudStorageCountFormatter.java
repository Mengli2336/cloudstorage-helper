package com.cloudstoragerei.core;

import java.util.Locale;

public final class CloudStorageCountFormatter {
	private CloudStorageCountFormatter() {
	}

	public static String format(long count) {
		if (count >= 100_000) {
			return count / 10_000 + "w";
		}
		if (count >= 10_000) {
			return formatScaled(count, 10_000, "w");
		}
		if (count >= 1_000) {
			return formatScaled(count, 1_000, "k");
		}
		return Long.toString(count);
	}

	private static String formatScaled(long count, long divisor, String suffix) {
		String value = String.format(Locale.ROOT, "%.1f", (double) count / divisor);

		if (value.endsWith(".0")) {
			value = value.substring(0, value.length() - 2);
		}

		return value + suffix;
	}
}
