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
import com.cloudstoragerei.core.CloudStorageLegacyHotkeys;

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
	private static final String PREVIOUS_CONFIG_FILE_NAME = "cloudstorage.json";
	private static final String LEGACY_CONFIG_FILE_NAME = "cloudstorage-rei.json";
	private static final int CONFIG_VERSION = 10;
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
		Path configDirectory = FileUtils.getConfigDirectory();
		Path configFile = configDirectory.resolve(CONFIG_FILE_NAME);

		try {
			if (Files.isRegularFile(configFile) && Files.isReadable(configFile)) {
				readConfig(configFile, false);
			} else {
				Path previousConfigFile = configDirectory.resolve(PREVIOUS_CONFIG_FILE_NAME);
				boolean migrated;

				if (Files.isRegularFile(previousConfigFile) && Files.isReadable(previousConfigFile)) {
					readConfig(previousConfigFile, true);
					migrated = true;
				} else {
					migrated = migrateLegacyConfig(configDirectory.resolve(LEGACY_CONFIG_FILE_NAME));
				}

				if (migrated && Files.notExists(configFile)) {
					this.save();
				}
			}
		} catch (Exception exception) {
			CloudStorageModClient.LOGGER.warn("Failed to load cloud storage config from '{}'", configFile, exception);
		}
	}

	private static void readConfig(Path configFile, boolean migrating) throws IOException {
		try (Reader reader = Files.newBufferedReader(configFile)) {
			JsonElement element = JsonParser.parseReader(reader);

			if (!element.isJsonObject()) {
				return;
			}

			JsonObject root = element.getAsJsonObject();
			int version = getInt(root, "config_version", 0);
			ConfigUtils.readConfigBase(root, "Display", Display.OPTIONS);
			ConfigUtils.readConfigBase(root, "Interaction", Interaction.OPTIONS);
			ConfigUtils.readConfigBase(root, "Compatibility", Compatibility.OPTIONS);
			ConfigUtils.readHotkeys(root, "Hotkeys", Hotkeys.HOTKEY_LIST);

			if (version < 2) {
				migrateVersionOneMouseHotkeys(root);
			}

			if (version < 4) {
				migrateVersionThreeQuickTakeHotkeys(root);
			}

			if (version < 9) {
				migrateVersionEightDefaultHotkeys(root);
			}

			if (version < 10) {
				migrateVersionNineDefaultHotkeys(root);
			}

			boolean resolvedHotkeyConflict = resolveOpenAmountConflict();

			if (version < CONFIG_VERSION || migrating || resolvedHotkeyConflict) {
				INSTANCE.save();
			}
		}
	}

	private static boolean migrateLegacyConfig(Path legacyFile) {
		if (!Files.isRegularFile(legacyFile) || !Files.isReadable(legacyFile)) {
			return false;
		}

		try (Reader reader = Files.newBufferedReader(legacyFile)) {
			JsonElement element = JsonParser.parseReader(reader);

			if (!element.isJsonObject()) {
				return false;
			}

			JsonObject root = element.getAsJsonObject();
			copyLegacyValue(root, "enableRender", Display.ENABLE_RENDER);
			copyLegacyValue(root, "fontScale", Display.FONT_SCALE);
			copyLegacyValue(root, "xOffset", Display.X_OFFSET);
			copyLegacyValue(root, "yOffset", Display.Y_OFFSET);
			copyLegacyValue(root, "showCiMessages", Display.SHOW_CI_MESSAGES);
			copyLegacyValue(root, "lastInputAmount", Interaction.LAST_INPUT_AMOUNT);
			migrateMouseHotkeys(
					new JsonObject(),
					getString(root, "modifierKey", "SHIFT"),
					getString(root, "takeOneMouse", "LEFT"),
					getString(root, "takeStackMouse", "RIGHT"),
					getString(root, "openGuiMouse", "MIDDLE")
			);
			CloudStorageModClient.LOGGER.info("Migrated legacy cloud storage config from '{}'", legacyFile);
			return true;
		} catch (Exception exception) {
			CloudStorageModClient.LOGGER.warn("Failed to migrate legacy cloud storage config from '{}'", legacyFile, exception);
			return false;
		}
	}

	private static void migrateVersionOneMouseHotkeys(JsonObject root) {
		JsonElement interactionElement = root.get("Interaction");

		if (interactionElement == null || !interactionElement.isJsonObject()) {
			return;
		}

		JsonObject interaction = interactionElement.getAsJsonObject();
		migrateMouseHotkeys(
				root,
				getString(interaction, "modifierKey", "SHIFT"),
				getString(interaction, "takeOneMouse", "LEFT"),
				getString(interaction, "takeStackMouse", "RIGHT"),
				getString(interaction, "openGuiMouse", "MIDDLE")
		);
	}

	private static void migrateMouseHotkeys(
			JsonObject root,
			String modifier,
			String takeOneMouse,
			String takeStackMouse,
			String openAmountMouse
	) {
		setHotkeyIfMissing(
				root,
				Hotkeys.TAKE_ONE,
				CloudStorageLegacyHotkeys.create(modifier, takeOneMouse)
		);
		setHotkeyIfMissing(
				root,
				Hotkeys.TAKE_STACK,
				CloudStorageLegacyHotkeys.create(modifier, takeStackMouse)
		);
		setHotkeyIfMissing(
				root,
				Hotkeys.OPEN_AMOUNT,
				CloudStorageLegacyHotkeys.openAmount(openAmountMouse)
		);
	}

	private static void migrateVersionThreeQuickTakeHotkeys(JsonObject root) {
		JsonElement hotkeysElement = root.get("Hotkeys");

		if (hotkeysElement == null || !hotkeysElement.isJsonObject()) {
			return;
		}

		JsonElement openAmountElement = hotkeysElement.getAsJsonObject().get("openAmount");

		if (openAmountElement == null) {
			return;
		}

		String storedValue;

		if (openAmountElement.isJsonObject()) {
			JsonElement keysElement = openAmountElement.getAsJsonObject().get("keys");
			storedValue = keysElement != null && keysElement.isJsonPrimitive()
					? keysElement.getAsString()
					: "";
		} else {
			storedValue = openAmountElement.isJsonPrimitive() ? openAmountElement.getAsString() : "";
		}

		if ("BUTTON_3".equals(storedValue)) {
			Hotkeys.OPEN_AMOUNT.getKeybind().setValueFromString("BUTTON_3");
		}
	}

	private static void migrateVersionEightDefaultHotkeys(JsonObject root) {
		migrateHotkeyIfMatching(
				root,
				Hotkeys.QUICK_TAKE,
				"BUTTON_3",
				"LEFT_SHIFT,BUTTON_3"
		);
		migrateHotkeyIfMatching(
				root,
				Hotkeys.OPEN_AMOUNT,
				"LEFT_CONTROL,BUTTON_3",
				"BUTTON_3"
		);
	}

	private static void migrateVersionNineDefaultHotkeys(JsonObject root) {
		migrateHotkeyIfMatching(
				root,
				Hotkeys.TAKE_STACK,
				"BUTTON_3",
				"LEFT_SHIFT,BUTTON_2"
		);
		migrateHotkeyIfMatching(
				root,
				Hotkeys.OPEN_AMOUNT,
				"LEFT_CONTROL,BUTTON_3",
				"BUTTON_3"
		);
	}

	private static void migrateHotkeyIfMatching(
			JsonObject root,
			ConfigHotkey hotkey,
			String oldValue,
			String newValue
	) {
		JsonElement hotkeysElement = root.get("Hotkeys");

		if (hotkeysElement == null || !hotkeysElement.isJsonObject()) {
			return;
		}

		JsonElement hotkeyElement = hotkeysElement.getAsJsonObject().get(hotkey.getName());

		if (hotkeyElement == null) {
			return;
		}

		String storedValue;

		if (hotkeyElement.isJsonObject()) {
			JsonElement keysElement = hotkeyElement.getAsJsonObject().get("keys");
			storedValue = keysElement != null && keysElement.isJsonPrimitive()
					? keysElement.getAsString()
					: "";
		} else {
			storedValue = hotkeyElement.isJsonPrimitive() ? hotkeyElement.getAsString() : "";
		}

		if (oldValue.equals(storedValue)) {
			hotkey.getKeybind().setValueFromString(newValue);
		}
	}

	private static boolean resolveOpenAmountConflict() {
		if (Hotkeys.OPEN_AMOUNT.getKeybind().getStringValue()
				.equals(Hotkeys.QUICK_TAKE.getKeybind().getStringValue())) {
			Hotkeys.QUICK_TAKE.getKeybind().setValueFromString("LEFT_SHIFT,BUTTON_3");
			return true;
		}

		return false;
	}

	private static void setHotkeyIfMissing(JsonObject root, ConfigHotkey hotkey, String value) {
		JsonElement hotkeysElement = root.get("Hotkeys");
		boolean alreadyConfigured = hotkeysElement != null
				&& hotkeysElement.isJsonObject()
				&& hotkeysElement.getAsJsonObject().has(hotkey.getName());

		if (value != null && !alreadyConfigured) {
			hotkey.getKeybind().setValueFromString(value);
		}
	}

	private static void copyLegacyValue(JsonObject root, String name, IConfigBase option) {
		if (root.has(name)) {
			option.setValueFromJsonElement(root.get(name));
			option.checkIfClean();
		}
	}

	private static String getString(JsonObject root, String name, String fallback) {
		JsonElement element = root.get(name);
		return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
	}

	private static int getInt(JsonObject root, String name, int fallback) {
		JsonElement element = root.get(name);

		try {
			return element != null && element.isJsonPrimitive() ? element.getAsInt() : fallback;
		} catch (NumberFormatException ignored) {
			return fallback;
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
