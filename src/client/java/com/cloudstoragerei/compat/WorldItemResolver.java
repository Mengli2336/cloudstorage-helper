package com.cloudstoragerei.compat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public interface WorldItemResolver {
	@Nullable
	ItemStack getTargetedItem();
}
