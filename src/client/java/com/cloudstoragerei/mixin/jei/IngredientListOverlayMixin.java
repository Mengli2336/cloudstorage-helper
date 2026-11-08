package com.cloudstoragerei.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cloudstoragerei.compat.jei.CloudStorageJeiRenderer;

import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(IngredientListOverlay.class)
public abstract class IngredientListOverlayMixin {
	@Shadow
	private IIngredientListOverlayContents contents;

	@Inject(
			method = "drawForeground",
			at = @At(
					value = "INVOKE",
					target = "Lmezz/jei/gui/overlay/ingredients/IIngredientListOverlayContents;drawForeground(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
					shift = At.Shift.AFTER
			),
			require = 1
	)
	private void cloudstorage$renderCounts(
			Minecraft minecraft,
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTicks,
			CallbackInfo callbackInfo
	) {
		if (this.contents instanceof IngredientGridWithNavigation grid) {
			CloudStorageJeiRenderer.renderCounts(graphics, grid.getSlots());
		}
	}
}
