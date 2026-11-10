package com.cloudstoragerei.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

/**
 * Edit box that only accepts digits, used by the amount and restock screens.
 */
public final class NumericEditBox extends EditBox {
	public NumericEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return Character.isDigit(event.codepoint()) && super.charTyped(event);
	}

	@Override
	public void insertText(String text) {
		String digits = text.replaceAll("\\D", "");

		if (!digits.isEmpty()) {
			super.insertText(digits);
		}
	}
}
