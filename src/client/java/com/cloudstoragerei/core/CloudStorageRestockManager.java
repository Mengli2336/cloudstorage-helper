package com.cloudstoragerei.core;

import java.util.Map;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.config.CloudStorageConfigs.Restock.ItemSettings;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Scans the player inventory on a configurable interval and asks the cloud storage
 * to top up every starred item whose count dropped below its threshold.
 */
public final class CloudStorageRestockManager {
	private static final CloudStorageRestockManager INSTANCE = new CloudStorageRestockManager();

	private int scanCountdown;
	private boolean bagFullPaused;
	private boolean initialized;

	private CloudStorageRestockManager() {
	}

	public static CloudStorageRestockManager getInstance() {
		return INSTANCE;
	}

	public void initialize() {
		if (this.initialized) {
			return;
		}

		this.initialized = true;
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.reset());
	}

	public boolean isBagFullPaused() {
		return this.bagFullPaused;
	}

	/**
	 * Runs one restock cycle immediately, ignoring the scan interval and the
	 * enable switch. Used by the "restock now" button.
	 */
	public void restockNow() {
		this.scanCountdown = this.scanInterval();
		this.runCycle();
	}

	private void reset() {
		this.scanCountdown = 0;
		this.bagFullPaused = false;
	}

	private void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		// Any open screen pauses the automatic cycle; the manual button still works.
		if (client.screen != null || !CloudStorageConfigs.Restock.ENABLED.getBooleanValue()) {
			return;
		}

		if (this.scanCountdown > 0) {
			this.scanCountdown--;
			return;
		}

		this.scanCountdown = this.scanInterval();
		this.runCycle();
	}

	private int scanInterval() {
		return CloudStorageConfigs.Restock.SCAN_INTERVAL.getIntegerValue();
	}

	private void runCycle() {
		Player player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		Map<String, ItemSettings> entries = CloudStorageConfigs.Restock.getItems();
		CloudStorageManager manager = CloudStorageManager.getInstance();
		boolean bagFull = false;

		for (Map.Entry<String, ItemSettings> entry : entries.entrySet()) {
			String itemId = entry.getKey();
			ItemSettings settings = entry.getValue();
			Item item = getItem(itemId);

			if (item == null) {
				continue;
			}

			int inInventory = countItem(player, item);
			int missing = settings.target() - inInventory;

			if (inInventory >= settings.threshold() || missing <= 0) {
				continue;
			}

			long available = manager.getCount(itemId);

			if (available <= 0) {
				continue;
			}

			int capacity = getFreeCapacity(player, item);

			if (capacity <= 0) {
				bagFull = true;
				continue;
			}

			int amount = (int) Math.min(Math.min(missing, available), capacity);

			if (amount <= 0) {
				continue;
			}

			manager.takeItem(itemId, amount);
		}

		this.bagFullPaused = bagFull;
	}

	public static int countItem(Player player, Item item) {
		Inventory inventory = player.getInventory();
		int total = 0;

		for (ItemStack stack : inventory.getNonEquipmentItems()) {
			if (!stack.isEmpty() && stack.getItem() == item) {
				total += stack.getCount();
			}
		}

		ItemStack offhand = player.getOffhandItem();

		if (!offhand.isEmpty() && offhand.getItem() == item) {
			total += offhand.getCount();
		}

		return total;
	}

	public static int getFreeCapacity(Player player, Item item) {
		Inventory inventory = player.getInventory();
		int maxStackSize = item.getDefaultInstance().getMaxStackSize();
		int capacity = 0;

		for (ItemStack stack : inventory.getNonEquipmentItems()) {
			if (stack.isEmpty()) {
				capacity += maxStackSize;
			} else if (stack.getItem() == item) {
				capacity += Math.max(0, stack.getMaxStackSize() - stack.getCount());
			}
		}

		return capacity;
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
}
