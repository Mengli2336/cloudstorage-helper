package com.cloudstoragerei.compat.jei;

import org.jetbrains.annotations.Nullable;

import com.cloudstoragerei.CloudStorageModClient;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.Identifier;

@JeiPlugin
public final class CloudStorageJeiPlugin implements IModPlugin {
	private static final Identifier PLUGIN_UID =
			Identifier.fromNamespaceAndPath("cloudstorage-helper", "jei_plugin");
	@Nullable
	private static IJeiRuntime runtime;

	@Override
	public Identifier getPluginUid() {
		return PLUGIN_UID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
		CloudStorageModClient.LOGGER.info("JEI integration runtime available");
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
		CloudStorageModClient.LOGGER.info("JEI integration runtime unavailable");
	}

	@Nullable
	static IJeiRuntime getRuntime() {
		return runtime;
	}
}
