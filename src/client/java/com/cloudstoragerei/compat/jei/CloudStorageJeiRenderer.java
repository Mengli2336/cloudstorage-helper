package com.cloudstoragerei.compat.jei;

import java.util.Optional;
import java.util.stream.Stream;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.render.CloudCountRenderer;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class CloudStorageJeiRenderer {
	private CloudStorageJeiRenderer() {
	}

	public static void renderCounts(
			GuiGraphicsExtractor graphics,
			Stream<IngredientListSlot> visibleSlots
	) {
		if (!CloudStorageConfigs.Display.ENABLE_RENDER.getBooleanValue()
				|| !CloudStorageConfigs.Compatibility.JEI_ENABLED.getBooleanValue()) {
			return;
		}

		visibleSlots.forEach(slot -> {
			Optional<IElement<?>> optionalElement = slot.getOptionalElement();

			if (optionalElement.isEmpty()) {
				return;
			}

			Optional<ItemStack> optionalStack = optionalElement.get()
					.getTypedIngredient()
					.getItemStack();

			if (optionalStack.isEmpty() || optionalStack.get().isEmpty()) {
				return;
			}

			ImmutableRect2i area = slot.getRenderArea();
			CloudCountRenderer.render(
					graphics,
					optionalStack.get(),
					area.x(),
					area.y(),
					area.width(),
					area.height()
			);
		});
	}
}
