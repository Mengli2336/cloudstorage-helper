package com.cloudstoragerei.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock.ItemSettings;
import com.cloudstoragerei.core.CloudStorageManager;
import com.cloudstoragerei.core.CloudStorageRestockManager;
import com.cloudstoragerei.gui.CustomAmountScreen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the enabled restock items on container screens and handles clicks on it.
 * The panel is anchored to the bottom-left corner; scale and offsets come from the config.
 */
public final class CloudStorageRestockOverlay {
	private static final int MARGIN = 4;
	private static final int PANEL_WIDTH = 132;
	private static final int ROW_HEIGHT = 18;
	private static final int HEADER_HEIGHT = 16;
	private static final int FOOTER_HEIGHT = 12;
	private static final int MAX_ROWS = 8;
	// Screen.init() runs again on resize; only register the per-screen hooks once.
	private static final Set<Screen> REGISTERED_SCREENS = Collections.newSetFromMap(new WeakHashMap<>());

	private CloudStorageRestockOverlay() {
	}

	public static void initialize() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?>) || !REGISTERED_SCREENS.add(screen)) {
				return;
			}

			ScreenEvents.afterExtract(screen).register(CloudStorageRestockOverlay::render);
			ScreenMouseEvents.allowMouseClick(screen).register(CloudStorageRestockOverlay::onClick);
		});
	}

	private static void render(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (!isVisible()) {
			return;
		}

		Layout layout = layout(screen);

		if (layout.rows().isEmpty()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		graphics.pose().pushMatrix();
		graphics.pose().translate(layout.x(), layout.y());
		graphics.pose().scale(layout.scale(), layout.scale());

		graphics.fill(0, 0, layout.width(), layout.height(), 0xC0080810);
		graphics.fill(0, 0, 1, layout.height(), 0xFF4A4A6A);
		graphics.text(client.font, Component.translatable("screen.cloudstorage-helper.restock.header"),
				4, 4, 0xFFCCCCFF, false);

		double localMouseX = (mouseX - layout.x()) / layout.scale();
		double localMouseY = (mouseY - layout.y()) / layout.scale();

		for (int i = 0; i < layout.rows().size(); i++) {
			Row row = layout.rows().get(i);
			int rowY = HEADER_HEIGHT + i * ROW_HEIGHT;

			if (localMouseX >= 0 && localMouseX < layout.width()
					&& localMouseY >= rowY && localMouseY < rowY + ROW_HEIGHT) {
				graphics.fill(1, rowY, layout.width(), rowY + ROW_HEIGHT, 0x30FFFFFF);
			}

			graphics.item(row.stack(), 2, rowY + 1);
			graphics.text(client.font, row.name(), 22, rowY + 5, 0xFFE0E0E0, false);
			graphics.text(client.font, row.progress(), layout.width() - 2 - client.font.width(row.progress()),
					rowY + 5, row.enough() ? 0xFF55FF55 : 0xFFFFAA55, false);
		}

		String footer = CloudStorageRestockManager.getInstance().isBagFullPaused()
				? Component.translatable("screen.cloudstorage-helper.restock.bagFull").getString()
				: Component.translatable("screen.cloudstorage-helper.restock.hint").getString();
		int footerColor = CloudStorageRestockManager.getInstance().isBagFullPaused() ? 0xFFFF5555 : 0xFF888888;
		graphics.text(client.font, footer, 4, layout.height() - FOOTER_HEIGHT + 2, footerColor, false);

		if (layout.total() > layout.rows().size()) {
			String more = Component.translatable("screen.cloudstorage-helper.restock.more",
					layout.total() - layout.rows().size()).getString();
			graphics.text(client.font, more, 4, layout.height() - FOOTER_HEIGHT - 4, 0xFF888888, false);
		}

		graphics.pose().popMatrix();
	}

	private static boolean onClick(Screen screen, MouseButtonEvent event) {
		if (!isVisible()) {
			return true;
		}

		Layout layout = layout(screen);

		if (layout.rows().isEmpty() || !layout.contains(event.x(), event.y())) {
			return true;
		}

		double localY = (event.y() - layout.y()) / layout.scale();
		int index = (int) ((localY - HEADER_HEIGHT) / ROW_HEIGHT);

		if (index < 0 || index >= layout.rows().size()) {
			return true;
		}

		Row row = layout.rows().get(index);

		if (event.button() == 1) {
			Restock.removeItem(row.itemId());
			CloudStorageConfigs.saveFromClientThread();
			return false;
		}

		if (event.button() == 0) {
			Minecraft client = Minecraft.getInstance();
			long count = CloudStorageManager.getInstance().getCount(row.itemId());
			client.setScreen(new CustomAmountScreen(screen, row.itemId(), row.name(), count));
			return false;
		}

		return true;
	}

	private static boolean isVisible() {
		return Restock.SHOW_PANEL.getBooleanValue() && Restock.ENABLED.getBooleanValue();
	}

	private static Layout layout(Screen screen) {
		List<Row> rows = new ArrayList<>();
		Player player = Minecraft.getInstance().player;
		Map<String, ItemSettings> entries = Restock.getItems();
		int total = 0;

		for (Map.Entry<String, ItemSettings> entry : entries.entrySet()) {
			Item item = getItem(entry.getKey());

			if (item == null) {
				continue;
			}

			total++;

			if (rows.size() >= MAX_ROWS) {
				continue;
			}

			int inInventory = player == null ? 0 : CloudStorageRestockManager.countItem(player, item);
			rows.add(new Row(
					entry.getKey(),
					item.getDefaultInstance(),
					item.getDefaultInstance().getHoverName().getString(),
					Math.min(inInventory, 99999) + "/" + entry.getValue().target(),
					inInventory >= entry.getValue().threshold()
			));
		}

		int height = HEADER_HEIGHT + rows.size() * ROW_HEIGHT + FOOTER_HEIGHT + 8;
		float scale = Math.max(0.25F, Restock.PANEL_SCALE.getFloatValue());
		int scaledWidth = Math.round(PANEL_WIDTH * scale);
		int scaledHeight = Math.round(height * scale);
		int x = Math.max(0, Math.min(MARGIN + Restock.PANEL_X.getIntegerValue(),
				screen.width - scaledWidth));
		int y = Math.max(0, Math.min(screen.height - MARGIN + Restock.PANEL_Y.getIntegerValue() - scaledHeight,
				screen.height - scaledHeight));
		return new Layout(x, y, PANEL_WIDTH, height, scale, rows, total);
	}

	private static Item getItem(String itemId) {
		Identifier identifier = Identifier.tryParse(itemId);

		if (identifier == null) {
			return null;
		}

		return BuiltInRegistries.ITEM.get(identifier)
				.map(holder -> holder.value())
				.orElse(null);
	}

	private record Row(String itemId, ItemStack stack, String name, String progress, boolean enough) {
	}

	private record Layout(int x, int y, int width, int height, float scale, List<Row> rows, int total) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= this.x && mouseX < this.x + this.width * this.scale
					&& mouseY >= this.y && mouseY < this.y + this.height * this.scale;
		}
	}
}
