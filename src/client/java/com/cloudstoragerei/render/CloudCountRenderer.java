package com.cloudstoragerei.render;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock;
import com.cloudstoragerei.core.CloudStorageCountFormatter;
import com.cloudstoragerei.core.CloudStorageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class CloudCountRenderer {
	private CloudCountRenderer() {
	}

	public static void render(
			GuiGraphicsExtractor graphics,
			ItemStack stack,
			int x,
			int y,
			int width,
			int height
	) {
		if (!CloudStorageConfigs.Display.ENABLE_RENDER.getBooleanValue() || stack.isEmpty()) {
			return;
		}

		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		long count = CloudStorageManager.getInstance().getCount(itemId.toString());

		if (count <= 0) {
			return;
		}

		String countText = CloudStorageCountFormatter.format(count);
		Font font = Minecraft.getInstance().font;
		float scale = Math.max(0.1F, CloudStorageConfigs.Display.FONT_SCALE.getFloatValue());
		int textX = x + width - Math.round(font.width(countText) * scale) - 1
				+ CloudStorageConfigs.Display.X_OFFSET.getIntegerValue();
		int textY = y + height - Math.round(font.lineHeight * scale) - 1
				+ CloudStorageConfigs.Display.Y_OFFSET.getIntegerValue();

		graphics.pose().pushMatrix();
		graphics.pose().translate(textX, textY);
		graphics.pose().scale(scale, scale);
		graphics.text(font, countText, 0, 0, 0xFFFFFFFF, true);
		graphics.pose().popMatrix();

		if (Restock.getItem(itemId.toString()) != null) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(x + 1, y + 1);
			graphics.pose().scale(0.6F, 0.6F);
			graphics.text(font, "\u2605", 0, 0, 0xFFFFD24A, true);
			graphics.pose().popMatrix();
		}
	}
}
