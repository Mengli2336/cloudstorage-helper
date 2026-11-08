package com.cloudstoragerei.compat.modmenu;

import com.cloudstoragerei.config.CloudStorageConfigGui;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class CloudStorageModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			CloudStorageConfigGui gui = new CloudStorageConfigGui();
			gui.setParent(screen);
			return gui;
		};
	}
}
