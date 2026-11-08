package com.cloudstoragerei.compat.rei;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.render.CloudCountRenderer;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.entry.renderer.ForwardingEntryRenderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import net.minecraft.world.item.ItemStack;

public final class CloudStorageReiPlugin implements REIClientPlugin {
	@Override
	public void registerEntryRenderers(EntryRendererRegistry registry) {
		registry.register(VanillaEntryTypes.ITEM, (entry, last) -> new ForwardingEntryRenderer<>(last) {
			@Override
			public void render(
					EntryStack<ItemStack> entry,
					GuiGraphics graphics,
					Rectangle bounds,
					int mouseX,
					int mouseY,
					float delta
			) {
				super.render(entry, graphics, bounds, mouseX, mouseY, delta);

				if (entry.isEmpty()
						|| !CloudStorageConfigs.Compatibility.REI_ENABLED.getBooleanValue()
						|| !isRenderedInVisibleList(bounds)) {
					return;
				}

				CloudCountRenderer.render(
						graphics,
						entry.getValue(),
						bounds.x,
						bounds.y,
						bounds.width,
						bounds.height
				);
			}
		});
	}

	private static boolean isRenderedInVisibleList(Rectangle bounds) {
		Point center = new Point(bounds.getCenterX(), bounds.getCenterY());

		return REIRuntime.getInstance().getOverlay()
				.map(overlay -> overlay.getEntryList().containsMouse(center)
						|| overlay.getFavoritesList()
						.map(favorites -> favorites.containsMouse(center))
						.orElse(false))
				.orElse(false);
	}
}
