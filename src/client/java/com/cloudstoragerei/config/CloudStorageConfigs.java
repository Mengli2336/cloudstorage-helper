package com.cloudstoragerei.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.cloudstoragerei.CloudStorageModClient;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigFloat;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;

public final class CloudStorageConfigs implements IConfigHandler {
	private static final String CONFIG_KEY = "config.cloudstorage-helper";
	private static final String CONFIG_FILE_NAME = "cloudstorage-helper.json";
	private static final int CONFIG_VERSION = 1;
	private static final CloudStorageConfigs INSTANCE = new CloudStorageConfigs();

	private CloudStorageConfigs() {
	}

	public static CloudStorageConfigs get() {
		return INSTANCE;
	}

	public static void saveFromClientThread() {
		INSTANCE.save();
	}

	public static final class Display {
		public static final ConfigBoolean ENABLE_RENDER =
				new ConfigBoolean("enableRender", true).apply(CONFIG_KEY);
		public static final ConfigFloat FONT_SCALE =
				new ConfigFloat("fontScale", 0.6F, 0.5F, 3.0F, true).apply(CONFIG_KEY);
		public static final ConfigInteger X_OFFSET =
				new ConfigInteger("xOffset", 2, -16, 16, true).apply(CONFIG_KEY);
		public static final ConfigInteger Y_OFFSET =
				new ConfigInteger("yOffset", 2, -16, 16, true).apply(CONFIG_KEY);
		public static final ConfigBoolean SHOW_CI_MESSAGES =
				new ConfigBoolean("showCiMessages", false).apply(CONFIG_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				ENABLE_RENDER,
				FONT_SCALE,
				X_OFFSET,
				Y_OFFSET,
				SHOW_CI_MESSAGES
		);
	}

	public static final class Interaction {
		public static final ConfigInteger LAST_INPUT_AMOUNT =
				new ConfigInteger("lastInputAmount", 64, 1, 999_999_999).apply(CONFIG_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				LAST_INPUT_AMOUNT
		);
	}

	public static final class Compatibility {
		public static final ConfigBoolean REI_ENABLED =
				new ConfigBoolean("reiEnabled", true).apply(CONFIG_KEY);
		public static final ConfigBoolean JEI_ENABLED =
				new ConfigBoolean("jeiEnabled", true).apply(CONFIG_KEY);
		public static final ConfigBoolean LITEMATICA_ENABLED =
				new ConfigBoolean("litematicaEnabled", true).apply(CONFIG_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				REI_ENABLED,
				JEI_ENABLED,
				LITEMATICA_ENABLED
		);
	}

	public static final class Hotkeys {
		private static final KeybindSettings GUI_ACTION_SETTINGS = KeybindSettings.create(
				KeybindSettings.Context.GUI,
				KeyAction.PRESS,
				false,
				true,
				true,
				true
		);
		private static final KeybindSettings ANY_ACTION_SETTINGS = KeybindSettings.create(
				KeybindSettings.Context.ANY,
				KeyAction.PRESS,
				false,
				true,
				true,
				true
		);
		public static final ConfigHotkey REFRESH = new ConfigHotkey(
				"refresh",
				"V",
				KeybindSettings.DEFAULT
		).apply(CONFIG_KEY);
		public static final ConfigHotkey PUT = new ConfigHotkey(
				"put",
				"B",
				KeybindSettings.DEFAULT
		).apply(CONFIG_KEY);
		public static final ConfigHotkey OPEN_CONFIG = new ConfigHotkey(
				"openConfig",
				"A,Y",
				KeybindSettings.DEFAULT
		).apply(CONFIG_KEY);
		public static final ConfigHotkey TAKE_ONE = new ConfigHotkey(
				"takeOne",
				"LEFT_SHIFT,BUTTON_1",
				GUI_ACTION_SETTINGS
		).apply(CONFIG_KEY);
		public static final ConfigHotkey TAKE_STACK = new ConfigHotkey(
				"takeStack",
				"LEFT_SHIFT,BUTTON_2",
				GUI_ACTION_SETTINGS
		).apply(CONFIG_KEY);
		public static final ConfigHotkey OPEN_AMOUNT = new ConfigHotkey(
				"openAmount",
				"BUTTON_3",
				GUI_ACTION_SETTINGS
		).apply(CONFIG_KEY);
		public static final ConfigHotkey QUICK_TAKE = new ConfigHotkey(
				"quickTake",
				"LEFT_SHIFT,BUTTON_3",
				ANY_ACTION_SETTINGS
		).apply(CONFIG_KEY);

		public static final ImmutableList<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
				REFRESH,
				PUT,
				OPEN_CONFIG,
				TAKE_ONE,
				TAKE_STACK,
				OPEN_AMOUNT,
				QUICK_TAKE
		);
	}

	@Override
	public void load() {
		Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);

		if (!Files.isRegularFile(configFile) || !Files.isReadable(configFile)) {
			return;
		}

		try {
			readConfig(configFile);
		} catch (Exception exception) {
			CloudStorageModClient.LOGGER.warn("Failed to load cloud storage config from '{}'", configFile, exception);
		}
	}

	private static void readConfig(Path configFile) throws IOException {
		try (Reader reader = Files.newBufferedReader(configFile)) {
			JsonElement element = JsonParser.parseReader(reader);

			if (!element.isJsonObject()) {
				return;
			}

			JsonObject root = element.getAsJsonObject();
			ConfigUtils.readConfigBase(root, "Display", Display.OPTIONS);
			ConfigUtils.readConfigBase(root, "Interaction", Interaction.OPTIONS);
			ConfigUtils.readConfigBase(root, "Compatibility", Compatibility.OPTIONS);
			ConfigUtils.readHotkeys(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
		}
	}

	@Override
	public void save() {
		Path configDirectory = FileUtils.getConfigDirectory();
		Path configFile = configDirectory.resolve(CONFIG_FILE_NAME);

		try {
			Files.createDirectories(configDirectory);
			JsonObject root = new JsonObject();
			ConfigUtils.writeConfigBase(root, "Display", Display.OPTIONS);
			ConfigUtils.writeConfigBase(root, "Interaction", Interaction.OPTIONS);
			ConfigUtils.writeConfigBase(root, "Compatibility", Compatibility.OPTIONS);
			ConfigUtils.writeHotkeys(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
			root.addProperty("config_version", CONFIG_VERSION);
			JsonUtils.writeJsonToFile(root, configFile);
		} catch (Exception exception) {
			CloudStorageModClient.LOGGER.warn("Failed to save cloud storage config to '{}'", configFile, exception);
		}
	}
}
