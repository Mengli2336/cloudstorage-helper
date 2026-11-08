package com.cloudstoragerei.config;

import java.util.List;

import com.cloudstoragerei.CloudStorageModClient;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.StringUtils;

public final class CloudStorageConfigGui extends GuiConfigsBase {
	private static ConfigTab tab = ConfigTab.DISPLAY;

	public CloudStorageConfigGui() {
		super(
				10,
				50,
				CloudStorageModClient.MOD_ID,
				null,
				"config.cloudstorage-helper.title",
				CloudStorageModClient.VERSION
		);
		this.setHoverInfoProvider(CloudStorageConfigGui::getHoverInfo);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.clearOptions();
		int x = 10;
		int y = 26;

		for (ConfigTab configTab : ConfigTab.values()) {
			x += this.createButton(x, y, configTab) + 2;
		}
	}

	@Override
	protected boolean useKeybindSearch() {
		return tab == ConfigTab.HOTKEYS;
	}

	@Override
	protected int getConfigWidth() {
		return tab == ConfigTab.DISPLAY || tab == ConfigTab.INTERACTION ? 180 : 220;
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		List<? extends IConfigBase> configs = switch (tab) {
			case DISPLAY -> CloudStorageConfigs.Display.OPTIONS;
			case INTERACTION -> CloudStorageConfigs.Interaction.OPTIONS;
			case HOTKEYS -> CloudStorageConfigs.Hotkeys.HOTKEY_LIST;
			case COMPATIBILITY -> CloudStorageConfigs.Compatibility.OPTIONS;
		};

		return ConfigOptionWrapper.createFor(configs);
	}

	private int createButton(int x, int y, ConfigTab configTab) {
		ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, configTab.getDisplayName());
		button.setEnabled(tab != configTab);
		this.addButton(button, new ButtonListener(configTab, this));
		return button.getWidth();
	}

	private static String getHoverInfo(IConfigBase config) {
		String comment = config.getComment();

		if (!(config instanceof IHotkey hotkey)) {
			return comment;
		}

		String keyLine = StringUtils.translate(
				"config.cloudstorage-helper.hover.currentKeys",
				formatHotkey(hotkey.getKeybind().getStringValue())
		);
		return comment == null || comment.isBlank() ? keyLine : comment + "\n" + keyLine;
	}

	private static String formatHotkey(String hotkey) {
		StringBuilder result = new StringBuilder();

		for (String key : hotkey.split(",")) {
			if (!result.isEmpty()) {
				result.append(" + ");
			}

			String translationKey = "key.cloudstorage-helper." + key;
			String translated = StringUtils.translate(translationKey);
			result.append(translated.equals(translationKey) ? key : translated);
		}

		return result.toString();
	}

	private record ButtonListener(ConfigTab configTab, CloudStorageConfigGui parent)
			implements IButtonActionListener {
		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			tab = this.configTab;
			this.parent.reCreateListWidget();

			if (this.parent.getListWidget() != null) {
				this.parent.getListWidget().resetScrollbarPosition();
			}

			this.parent.initGui();
		}
	}

	private enum ConfigTab {
		DISPLAY("config.cloudstorage-helper.category.display"),
		INTERACTION("config.cloudstorage-helper.category.interaction"),
		HOTKEYS("config.cloudstorage-helper.category.hotkeys"),
		COMPATIBILITY("config.cloudstorage-helper.category.compatibility");

		private final String translationKey;

		ConfigTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}
	}
}
