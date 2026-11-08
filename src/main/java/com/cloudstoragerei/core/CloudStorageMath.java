package com.cloudstoragerei.core;

public final class CloudStorageMath {
	private CloudStorageMath() {
	}

	public static long getBoxItemCount(int boxes, int maxStackSize) {
		if (boxes <= 0 || maxStackSize <= 0) {
			return 0L;
		}

		try {
			return Math.multiplyExact(Math.multiplyExact((long) boxes, 27L), maxStackSize);
		} catch (ArithmeticException ignored) {
			return Long.MAX_VALUE;
		}
	}

	public static int clampTakeAmount(int configuredAmount, long availableCount) {
		if (configuredAmount <= 0 || availableCount <= 0) {
			return 0;
		}

		return (int) Math.min((long) configuredAmount, availableCount);
	}
}
