package com.cloudstoragerei.config;

import java.util.List;

import com.cloudstoragerei.CloudStorageModClient;
import com.cloudstoragerei.core.CloudStorageManager;
import com.cloudstoragerei.input.CloudStorageAction;
import com.cloudstoragerei.input.CloudStorageInteractionHandler;

import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.KeyAction;

public final class CloudStorageHotkeys {
	private static final IKeybindProvider PROVIDER = new Provider();
	private static final IHotkeyCallback CALLBACK = new Callback();
	private static boolean initialized;

	private CloudStorageHotkeys() {
	}

	public static void init() {
		if (initialized) {
			return;
		}

		initialized = true;
		CloudStorageConfigs.Hotkeys.REFRESH.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.PUT.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.OPEN_CONFIG.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.TAKE_ONE.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.TAKE_STACK.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.OPEN_AMOUNT.getKeybind().setCallback(CALLBACK);
		CloudStorageConfigs.Hotkeys.QUICK_TAKE.getKeybind().setCallback(CALLBACK);
		InputEventHandler.getKeybindManager().registerKeybindProvider(PROVIDER);
	}

	private static final class Provider implements IKeybindProvider {
		@Override
		public void addKeysToMap(IKeybindManager manager) {
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.REFRESH.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.PUT.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.OPEN_CONFIG.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.TAKE_ONE.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.TAKE_STACK.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.OPEN_AMOUNT.getKeybind());
			manager.addKeybindToMap(CloudStorageConfigs.Hotkeys.QUICK_TAKE.getKeybind());
		}

		@Override
		public void addHotkeys(IKeybindManager manager) {
			List<IHotkey> hotkeys = List.of(
					CloudStorageConfigs.Hotkeys.REFRESH,
					CloudStorageConfigs.Hotkeys.PUT,
					CloudStorageConfigs.Hotkeys.OPEN_CONFIG,
					CloudStorageConfigs.Hotkeys.TAKE_ONE,
					CloudStorageConfigs.Hotkeys.TAKE_STACK,
					CloudStorageConfigs.Hotkeys.OPEN_AMOUNT,
					CloudStorageConfigs.Hotkeys.QUICK_TAKE
			);
			manager.addHotkeysForCategory(
					CloudStorageModClient.MOD_NAME,
					"key.category.cloudstorage-helper.cloud_storage",
					hotkeys
			);
		}
	}

	private static final class Callback implements IHotkeyCallback {
		@Override
		public boolean onKeyAction(KeyAction action, IKeybind key) {
			if (key == CloudStorageConfigs.Hotkeys.REFRESH.getKeybind()) {
				CloudStorageManager.getInstance().requestUpdate();
				return true;
			}

			if (key == CloudStorageConfigs.Hotkeys.PUT.getKeybind()) {
				CloudStorageManager.getInstance().requestPut();
				return true;
			}

			if (key == CloudStorageConfigs.Hotkeys.OPEN_CONFIG.getKeybind()) {
				GuiBase.openGui(new CloudStorageConfigGui());
				return true;
			}

			if (key == CloudStorageConfigs.Hotkeys.TAKE_ONE.getKeybind()) {
				return CloudStorageInteractionHandler.performAction(CloudStorageAction.TAKE_ONE);
			}

			if (key == CloudStorageConfigs.Hotkeys.TAKE_STACK.getKeybind()) {
				return CloudStorageInteractionHandler.performAction(CloudStorageAction.TAKE_STACK);
			}

			if (key == CloudStorageConfigs.Hotkeys.OPEN_AMOUNT.getKeybind()) {
				return CloudStorageInteractionHandler.performAction(CloudStorageAction.OPEN_AMOUNT);
			}

			if (key == CloudStorageConfigs.Hotkeys.QUICK_TAKE.getKeybind()) {
				return CloudStorageInteractionHandler.performAction(CloudStorageAction.QUICK_TAKE);
			}

			return false;
		}
	}
}
