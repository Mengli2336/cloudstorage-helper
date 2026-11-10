package com.cloudstoragerei.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CloudStorageCoreTest {
	@Test
	void parsesCloudStorageHeaderAndItems() {
		CloudStorageMessageParser.ParsedMessage parsed = CloudStorageMessageParser.parse(
				"你的云仓物品如下:\n- (minecraft:stone)x1.6\n- (minecraft:oak_log)x64"
		);

		assertTrue(parsed.cloudStorageHeader());
		assertTrue(parsed.hasParsedEntries());
		assertEquals(2, parsed.entries().size());
		assertEquals(new CloudStorageMessageParser.ItemEntry("minecraft:stone", 2L), parsed.entries().get(0));
		assertEquals(new CloudStorageMessageParser.ItemEntry("minecraft:oak_log", 64L), parsed.entries().get(1));
	}

	@Test
	void identifiesDimensionSwitchAndIgnoresMalformedMessages() {
		CloudStorageMessageParser.ParsedMessage switched =
				CloudStorageMessageParser.parse("你的存档已切换至 minecraft:overworld");
		CloudStorageMessageParser.ParsedMessage malformed =
				CloudStorageMessageParser.parse("cloud item (minecraft:stone)xnot-a-number");

		assertTrue(switched.dimensionSwitch());
		assertFalse(switched.hasParsedEntries());
		assertFalse(malformed.hasParsedEntries());
	}

	@Test
	void formatsCountsAtAllAbbreviationBoundaries() {
		assertEquals("999", CloudStorageCountFormatter.format(999));
		assertEquals("1k", CloudStorageCountFormatter.format(1_000));
		assertEquals("10k", CloudStorageCountFormatter.format(9_999));
		assertEquals("1w", CloudStorageCountFormatter.format(10_000));
		assertEquals("10w", CloudStorageCountFormatter.format(99_999));
		assertEquals("10w", CloudStorageCountFormatter.format(100_000));
		assertEquals("12w", CloudStorageCountFormatter.format(123_456));
	}

	@Test
	void calculatesShulkerBoxContentsWithSaturation() {
		assertEquals(1_728L, CloudStorageMath.getBoxItemCount(1, 64));
		assertEquals(3_456L, CloudStorageMath.getBoxItemCount(2, 64));
		assertEquals(0L, CloudStorageMath.getBoxItemCount(0, 64));
		assertEquals(Long.MAX_VALUE, CloudStorageMath.getBoxItemCount(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Test
	void clampsQuickTakeAmountToKnownCloudCount() {
		assertEquals(64, CloudStorageMath.clampTakeAmount(64, 100));
		assertEquals(23, CloudStorageMath.clampTakeAmount(64, 23));
		assertEquals(0, CloudStorageMath.clampTakeAmount(64, 0));
		assertEquals(0, CloudStorageMath.clampTakeAmount(0, 100));
	}
}
