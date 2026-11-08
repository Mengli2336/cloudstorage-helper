package com.cloudstoragerei;

import com.cloudstoragerei.compat.CloudStorageAdapters;
import com.cloudstoragerei.config.CloudStorageConfigGui;
import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.config.CloudStorageHotkeys;
import com.cloudstoragerei.core.CloudStorageManager;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;

public final class CloudStorageInitHandler implements IInitializationHandler {
	@Override
	public void registerModHandlers() {
		ConfigManager.getInstance().registerConfigHandler(CloudStorageModClient.MOD_ID, CloudStorageConfigs.get());
		Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(
				CloudStorageModClient.MOD_ID,
				CloudStorageModClient.MOD_NAME,
				CloudStorageConfigGui::new
		));

		CloudStorageHotkeys.init();
		CloudStorageAdapters.initialize();
		CloudStorageManager.getInstance().initialize();
	}
}
