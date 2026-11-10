package com.cloudstoragerei.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
	private static final int CONFIG_VERSION = 3;
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

	public static final class Restock {
		public static final ConfigBoolean ENABLED =
				new ConfigBoolean("restockEnabled", true).apply(CONFIG_KEY);
		public static final ConfigInteger SCAN_INTERVAL =
				new ConfigInteger("restockScanInterval", 10, 1, 100).apply(CONFIG_KEY);
		public static final ConfigBoolean SHOW_PANEL =
				new ConfigBoolean("showRestockPanel", true).apply(CONFIG_KEY);
		public static final ConfigFloat PANEL_SCALE =
				new ConfigFloat("restockPanelScale", 1.0F, 0.5F, 3.0F, true).apply(CONFIG_KEY);
		public static final ConfigInteger PANEL_X =
				new ConfigInteger("restockPanelX", 0, -400, 400).apply(CONFIG_KEY);
		public static final ConfigInteger PANEL_Y =
				new ConfigInteger("restockPanelY", 0, -400, 400).apply(CONFIG_KEY);

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
				ENABLED,
				SCAN_INTERVAL,
				SHOW_PANEL,
				PANEL_SCALE,
				PANEL_X,
				PANEL_Y
		);

		private static final int MAX_AMOUNT = 999_999;
		private static final Map<String, ItemSettings> ITEMS = new LinkedHashMap<>();

		public record ItemSettings(int threshold, int target) {
			public ItemSettings {
				int safeThreshold = Math.max(1, Math.min(threshold, MAX_AMOUNT));
				threshold = safeThreshold;
				target = Math.max(safeThreshold, Math.min(target, MAX_AMOUNT));
			}
		}

		public static synchronized ItemSettings getItem(String itemId) {
			return ITEMS.get(itemId);
		}

		public static synchronized Map<String, ItemSettings> getItems() {
			// Keep the insertion order so the overlay panel and the JSON file stay stable.
			return Collections.unmodifiableMap(new LinkedHashMap<>(ITEMS));
		}

		public static synchronized void putItem(String itemId, ItemSettings settings) {
			ITEMS.put(itemId, settings);
		}

		public static synchronized void removeItem(String itemId) {
			ITEMS.remove(itemId);
		}

		public static ItemSettings defaultSettings(int maxStackSize) {
			int stackSize = Math.max(1, maxStackSize);
			return new ItemSettings(Math.max(1, stackSize / 4), stackSize);
		}

		private static void readItems(JsonObject restock) {
			ITEMS.clear();

			if (restock == null || !restock.has("items") || !restock.get("items").isJsonObject()) {
				return;
			}

			for (Map.Entry<String, JsonElement> entry : restock.getAsJsonObject("items").entrySet()) {
				if (!entry.getValue().isJsonObject()) {
					continue;
				}

				JsonObject settings = entry.getValue().getAsJsonObject();
				int threshold = Math.max(1, JsonUtils.getIntegerOrDefault(settings, "threshold", 1));
				int target = Math.max(1, JsonUtils.getIntegerOrDefault(settings, "target", threshold));
				ITEMS.put(entry.getKey(), new ItemSettings(threshold, target));
			}
		}

		private static void writeItems(JsonObject root) {
			JsonObject restock = root.getAsJsonObject("Restock");

			if (restock == null) {
				return;
			}

			JsonObject items = new JsonObject();
			synchronized (Restock.class) {
				for (Map.Entry<String, ItemSettings> entry : ITEMS.entrySet()) {
					JsonObject settings = new JsonObject();
					settings.addProperty("threshold", entry.getValue().threshold());
					settings.addProperty("target", entry.getValue().target());
					items.add(entry.getKey(), settings);
				}
			}
			restock.add("items", items);
		}
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
			ConfigUtils.readConfigBase(root, "Restock", Restock.OPTIONS);
			ConfigUtils.readHotkeys(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

			JsonElement restockElement = root.get("Restock");
			Restock.readItems(restockElement != null && restockElement.isJsonObject()
					? restockElement.getAsJsonObject()
					: null);

			int version = JsonUtils.getIntegerOrDefault(root, "config_version", 1);

			if (version < CONFIG_VERSION) {
				// v3 added the restock panel layout options; writing once persists the new defaults.
				INSTANCE.save();
			}
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
			ConfigUtils.writeConfigBase(root, "Restock", Restock.OPTIONS);
			Restock.writeItems(root);
			ConfigUtils.writeHotkeys(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
			root.addProperty("config_version", CONFIG_VERSION);
			JsonUtils.writeJsonToFile(root, configFile);
		} catch (Exception exception) {
			CloudStorageModClient.LOGGER.warn("Failed to save cloud storage config to '{}'", configFile, exception);
		}
	}
}
