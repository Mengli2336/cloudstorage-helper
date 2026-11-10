package com.cloudstoragerei.gui;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock.ItemSettings;
import com.cloudstoragerei.core.CloudStorageRestockManager;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Per-item restock settings: enable flag, threshold, target and a manual trigger.
 * Enabling restock for an item is what puts it on the restock list (and the star badge).
 */
public final class RestockSettingsScreen extends Screen {
	private final Screen parent;
	private final String itemId;
	private final String itemName;
	private final int maxStackSize;
	private NumericEditBox thresholdField;
	private NumericEditBox targetField;
	private Button enabledButton;
	private Button restockNowButton;
	private boolean itemEnabled;
	private Component status = Component.empty();

	public RestockSettingsScreen(Screen parent, String itemId, String itemName) {
		super(Component.translatable("screen.cloudstorage-helper.restock.title"));
		this.parent = parent;
		this.itemId = itemId;
		this.itemName = itemName;
		this.maxStackSize = getMaxStackSize(itemId);
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.width / 2;
		int top = this.height / 2 - 100;
		ItemSettings settings = Restock.getItem(this.itemId);
		this.itemEnabled = settings != null;

		if (settings == null) {
			settings = Restock.defaultSettings(this.maxStackSize);
		}

		this.thresholdField = new NumericEditBox(this.font, centerX - 100, top + 20, 90, 20,
				Component.translatable("screen.cloudstorage-helper.restock.threshold"));
		this.thresholdField.setMaxLength(6);
		this.thresholdField.setValue(Integer.toString(settings.threshold()));
		this.addRenderableWidget(this.thresholdField);

		this.targetField = new NumericEditBox(this.font, centerX + 10, top + 20, 90, 20,
				Component.translatable("screen.cloudstorage-helper.restock.target"));
		this.targetField.setMaxLength(6);
		this.targetField.setValue(Integer.toString(settings.target()));
		this.addRenderableWidget(this.targetField);

		this.enabledButton = Button.builder(this.enabledText(), button -> this.toggleEnabled())
				.bounds(centerX - 105, top + 54, 210, 20)
				.build();
		this.addRenderableWidget(this.enabledButton);

		this.restockNowButton = Button.builder(
						Component.translatable("screen.cloudstorage-helper.restock.now"),
						button -> this.restockNow()
				)
				.bounds(centerX - 105, top + 78, 210, 20)
				.build();
		this.restockNowButton.active = this.itemEnabled;
		this.addRenderableWidget(this.restockNowButton);

		this.addRenderableWidget(Button.builder(
						Component.translatable("gui.done"),
						button -> this.onClose()
				)
				.bounds(centerX - 105, top + 102, 210, 20)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int centerX = this.width / 2;
		int top = this.height / 2 - 100;
		graphics.centeredText(this.font, this.title, centerX, top - 40, 0xFFFFFFFF);
		graphics.centeredText(
				this.font,
				Component.translatable("screen.cloudstorage-helper.restock.item", this.itemName),
				centerX,
				top - 28,
				0xFFAAAAAA
		);
		graphics.text(this.font, Component.translatable("screen.cloudstorage-helper.restock.thresholdLabel"),
				centerX - 100, top + 8, 0xFFAAAAAA, false);
		graphics.text(this.font, Component.translatable("screen.cloudstorage-helper.restock.targetLabel"),
				centerX + 10, top + 8, 0xFFAAAAAA, false);

		if (!this.status.getString().isEmpty()) {
			graphics.centeredText(this.font, this.status, centerX, top + 128, 0xFF55FF55);
		}
	}

	@Override
	public void onClose() {
		this.commit();
		this.minecraft.setScreen(this.parent);
	}

	private void toggleEnabled() {
		this.itemEnabled = !this.itemEnabled;

		if (this.itemEnabled) {
			Restock.putItem(this.itemId, this.currentSettings());
		} else {
			Restock.removeItem(this.itemId);
		}

		CloudStorageConfigs.saveFromClientThread();
		this.enabledButton.setMessage(this.enabledText());
		this.restockNowButton.active = this.itemEnabled;
		this.status = Component.empty();
	}

	private void restockNow() {
		if (!this.itemEnabled) {
			return;
		}

		CloudStorageRestockManager.getInstance().restockNow();
		this.status = Component.translatable("screen.cloudstorage-helper.restock.manualDone");
	}

	private void commit() {
		if (this.itemEnabled) {
			Restock.putItem(this.itemId, this.currentSettings());
		} else {
			Restock.removeItem(this.itemId);
		}

		CloudStorageConfigs.saveFromClientThread();
	}

	private ItemSettings currentSettings() {
		int threshold = this.parseAmount(this.thresholdField, 1);
		int target = this.parseAmount(this.targetField, this.maxStackSize);
		return new ItemSettings(threshold, target);
	}

	private int parseAmount(NumericEditBox field, int fallback) {
		if (field == null || field.getValue().isEmpty()) {
			return fallback;
		}

		try {
			return Integer.parseInt(field.getValue());
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private Component enabledText() {
		return Component.translatable(this.itemEnabled
				? "screen.cloudstorage-helper.restock.enabled"
				: "screen.cloudstorage-helper.restock.disabled");
	}

	private static int getMaxStackSize(String itemId) {
		Identifier identifier = Identifier.tryParse(itemId);

		if (identifier == null) {
			return 64;
		}

		return BuiltInRegistries.ITEM.get(identifier)
				.map(holder -> holder.value().getDefaultMaxStackSize())
				.filter(size -> size > 0)
				.orElse(64);
	}
}
