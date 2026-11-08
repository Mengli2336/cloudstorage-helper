package com.cloudstoragerei.compat.rei;

import org.jetbrains.annotations.Nullable;

import com.cloudstoragerei.compat.ItemManagerAdapter;
import com.cloudstoragerei.config.CloudStorageConfigs;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public final class CloudStorageReiAdapter implements ItemManagerAdapter {
	@Override
	public String id() {
		return "rei";
	}

	@Override
	public boolean isEnabled() {
		return CloudStorageConfigs.Compatibility.REI_ENABLED.getBooleanValue();
	}

	@Nullable
	@Override
	public ItemStack getHoveredItem(Screen screen, double mouseX, double mouseY) {
		Point mouse = new Point(mouseX, mouseY);

		return REIRuntime.getInstance().getOverlay()
				.map(overlay -> getFocusedItem(overlay, mouse))
				.filter(stack -> stack.getType() == VanillaEntryTypes.ITEM)
				.filter(stack -> !stack.isEmpty())
				.map(EntryStack::getValue)
				.filter(ItemStack.class::isInstance)
				.map(ItemStack.class::cast)
				.orElse(null);
	}

	private static EntryStack<?> getFocusedItem(ScreenOverlay overlay, Point mouse) {
		if (overlay.getEntryList().containsMouse(mouse)) {
			return overlay.getEntryList().getFocusedStack();
		}

		return overlay.getFavoritesList()
				.filter(favorites -> favorites.containsMouse(mouse))
				.map(OverlayListWidget::getFocusedStack)
				.orElse(EntryStack.empty());
	}
}
