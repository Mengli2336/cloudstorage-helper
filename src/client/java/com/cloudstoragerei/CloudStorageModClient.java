package com.cloudstoragerei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import fi.dy.masa.malilib.event.InitializationHandler;

public final class CloudStorageModClient implements ClientModInitializer {
	public static final String MOD_ID = "cloudstorage-helper";
	public static final String MOD_NAME = "Cloud Storage Helper";
	public static final String VERSION = FabricLoader.getInstance()
			.getModContainer(MOD_ID)
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("dev");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitializeClient() {
		InitializationHandler.getInstance()
				.registerInitializationHandler(new CloudStorageInitHandler());
	}
}
