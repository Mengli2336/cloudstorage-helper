package com.cloudstoragerei.gui;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.core.CloudStorageManager;
import com.cloudstoragerei.input.CloudStorageInteractionHandler;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public final class CustomAmountScreen extends Screen {
	private final Screen parent;
	private final String itemId;
	private final String itemName;
	private final long totalCount;
	private EditBox amountField;
	private Button modeButton;
	private boolean takeBoxMode;
	private boolean closing;

	public CustomAmountScreen(Screen parent, String itemId, String itemName, long totalCount) {
		super(Component.translatable("screen.cloudstorage-helper.amount.title"));
		this.parent = parent;
		this.itemId = itemId;
		this.itemName = itemName;
		this.totalCount = totalCount;
	}

	@Override
	protected void init() {
		super.init();

		int fieldWidth = 180;
		int fieldX = this.width / 2 - fieldWidth / 2;
		int fieldY = this.height / 2 - 10;

		this.amountField = new NumericEditBox(
				this.font,
				fieldX,
				fieldY,
				fieldWidth,
				20,
				Component.translatable("screen.cloudstorage-helper.amount.input")
		);
		this.amountField.setMaxLength(9);
		String initialAmount = Integer.toString(
				CloudStorageConfigs.Interaction.LAST_INPUT_AMOUNT.getIntegerValue()
		);
		this.amountField.setValue(initialAmount);
		this.addRenderableWidget(this.amountField);
		this.setInitialFocus(this.amountField);
		this.amountField.setFocused(true);
		this.amountField.setCursorPosition(0);
		this.amountField.setHighlightPos(initialAmount.length());

		this.modeButton = Button.builder(this.modeButtonText(), button -> {
					this.takeBoxMode = !this.takeBoxMode;
					button.setMessage(this.modeButtonText());
				})
				.bounds(this.width / 2 - 90, fieldY - 30, 180, 20)
				.build();
		this.addRenderableWidget(this.modeButton);

		int buttonY = fieldY + 30;
		this.addRenderableWidget(Button.builder(
						Component.translatable("screen.cloudstorage-helper.amount.take"),
						button -> this.submit()
				)
				.bounds(this.width / 2 - 105, buttonY, 100, 20)
				.build());
		this.addRenderableWidget(Button.builder(
						Component.translatable("gui.back"),
						button -> this.onClose()
				)
				.bounds(this.width / 2 + 5, buttonY, 100, 20)
				.build());
		this.addRenderableWidget(Button.builder(
						Component.translatable("screen.cloudstorage-helper.amount.restock"),
						button -> this.openRestockSettings()
				)
				.bounds(this.width / 2 - 105, buttonY + 24, 210, 20)
				.build());
	}

	private void openRestockSettings() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(new RestockSettingsScreen(this, this.itemId, this.itemName));
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int centerX = this.width / 2;
		int textY = this.height / 2 - 92;
		graphics.centeredText(this.font, this.title, centerX, textY, 0xFFFFFFFF);
		graphics.centeredText(
				this.font,
				Component.translatable("screen.cloudstorage-helper.amount.item", this.itemName),
				centerX,
				textY + 20,
				0xFFAAAAAA
		);
		graphics.centeredText(
				this.font,
				Component.translatable("screen.cloudstorage-helper.amount.total", this.totalCount),
				centerX,
				textY + 34,
				0xFFAAAAAA
		);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
			this.submit();
			return true;
		}

		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		if (this.closing) {
			return;
		}

		this.closing = true;
		this.saveLastInput();
		CloudStorageInteractionHandler.suppressClicksAfterClosing();
		Screen returnScreen = this.parent;
		this.minecraft.execute(() -> {
			if (this.minecraft.screen == this) {
				this.minecraft.setScreen(returnScreen);
			}
		});
	}

	private void submit() {
		int amount = this.parseAmount();

		if (amount <= 0) {
			return;
		}

		if (this.takeBoxMode) {
			CloudStorageManager.getInstance().takeBox(this.itemId, amount);
		} else {
			CloudStorageManager.getInstance().takeItem(this.itemId, amount);
		}

		this.onClose();
	}

	private void saveLastInput() {
		int amount = this.parseAmount();

		if (amount > 0) {
			CloudStorageConfigs.Interaction.LAST_INPUT_AMOUNT.setIntegerValue(amount);
			CloudStorageConfigs.saveFromClientThread();
		}
	}

	private int parseAmount() {
		if (this.amountField == null || this.amountField.getValue().isEmpty()) {
			return -1;
		}

		try {
			return Integer.parseInt(this.amountField.getValue());
		} catch (NumberFormatException ignored) {
			return -1;
		}
	}

	private Component modeButtonText() {
		return Component.translatable(this.takeBoxMode
				? "screen.cloudstorage-helper.amount.mode_box"
				: "screen.cloudstorage-helper.amount.mode_item");
	}

}
