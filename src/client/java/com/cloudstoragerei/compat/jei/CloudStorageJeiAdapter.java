package com.cloudstoragerei.compat.jei;

import org.jetbrains.annotations.Nullable;

import com.cloudstoragerei.compat.ItemManagerAdapter;
import com.cloudstoragerei.config.CloudStorageConfigs;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public final class CloudStorageJeiAdapter implements ItemManagerAdapter {
	@Override
	public String id() {
		return "jei";
	}

	@Override
	public boolean isEnabled() {
		return CloudStorageConfigs.Compatibility.JEI_ENABLED.getBooleanValue();
	}

	@Nullable
	@Override
	public ItemStack getHoveredItem(Screen screen, double mouseX, double mouseY) {
		IJeiRuntime runtime = CloudStorageJeiPlugin.getRuntime();

		if (runtime == null || !runtime.getIngredientListOverlay().isListDisplayed()) {
			return null;
		}

		ItemStack stack = runtime.getIngredientListOverlay()
				.getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
		return stack != null
				? stack
				: runtime.getBookmarkOverlay().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
	}
}
