package com.cloudstoragerei.mixin.rei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cloudstoragerei.gui.CustomAmountScreen;

import net.minecraft.client.gui.screens.Screen;

@Mixin(targets = "me.shedaniel.rei.impl.client.REIRuntimeImpl")
public abstract class REIRuntimeImplMixin {
	@Inject(method = "setPreviousScreen", at = @At("HEAD"), cancellable = true)
	private void cloudstorage$keepRecipeReturnScreen(Screen previousScreen, CallbackInfo callbackInfo) {
		if (previousScreen instanceof CustomAmountScreen) {
			callbackInfo.cancel();
		}
	}
}
