package com.cloudstoragerei.compat;

import net.minecraft.world.item.ItemStack;

public record CloudItemTarget(
		String managerId,
		ItemManagerAdapter adapter,
		ItemStack stack
) {
}
