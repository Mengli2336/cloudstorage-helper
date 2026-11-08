package com.cloudstoragerei.compat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public interface ItemManagerAdapter {
	String id();

	boolean isEnabled();

	@Nullable
	ItemStack getHoveredItem(Screen screen, double mouseX, double mouseY);
}
