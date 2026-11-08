package com.cloudstoragerei.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cloudstoragerei.compat.jei.CloudStorageJeiRenderer;

import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(BookmarkOverlay.class)
public abstract class BookmarkOverlayMixin {
	@Shadow
	private IngredientGridWithNavigation contents;

	@Inject(
			method = "drawForeground",
			at = @At(
					value = "INVOKE",
					target = "Lmezz/jei/gui/overlay/ingredients/IngredientGridWithNavigation;drawForeground(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
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
		CloudStorageJeiRenderer.renderCounts(graphics, this.contents.getSlots());
	}
}
