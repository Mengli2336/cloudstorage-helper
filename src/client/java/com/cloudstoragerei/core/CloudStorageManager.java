package com.cloudstoragerei.core;

import java.util.HashMap;
import java.util.Map;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.core.CloudStorageMessageParser.ItemEntry;
import com.cloudstoragerei.core.CloudStorageMessageParser.ParsedMessage;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class CloudStorageManager {
	private static final CloudStorageManager INSTANCE = new CloudStorageManager();
	private static final int INITIAL_UPDATE_DELAY_TICKS = 50;

	private final Map<String, Long> items = new HashMap<>();
	private int initialUpdateCountdown = -1;
	private boolean initialized;

	private CloudStorageManager() {
	}

	public static CloudStorageManager getInstance() {
		return INSTANCE;
	}

	public void initialize() {
		if (this.initialized) {
			return;
		}

		this.initialized = true;
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> this.handleGameMessage(message.getString()));
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> this.initialUpdateCountdown = INITIAL_UPDATE_DELAY_TICKS
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			this.initialUpdateCountdown = -1;
			this.clear();
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::tickInitialUpdate);
	}

	private boolean handleGameMessage(String messageText) {
		ParsedMessage parsedMessage = CloudStorageMessageParser.parse(messageText);

		if (parsedMessage.cloudStorageHeader()) {
			this.clear();
		}

		if (parsedMessage.hasParsedEntries()) {
			synchronized (this.items) {
				for (ItemEntry entry : parsedMessage.entries()) {
					this.items.put(entry.itemId(), entry.count());
				}
			}
		}

		boolean hideMessage = !CloudStorageConfigs.Display.SHOW_CI_MESSAGES.getBooleanValue()
				&& (parsedMessage.dimensionSwitch()
				|| parsedMessage.cloudStorageHeader()
				|| parsedMessage.hasParsedEntries());
		return !hideMessage;
	}

	private void tickInitialUpdate(Minecraft client) {
		if (this.initialUpdateCountdown < 0) {
			return;
		}

		if (this.initialUpdateCountdown > 0) {
			this.initialUpdateCountdown--;
			return;
		}

		ClientPacketListener connection = client.getConnection();

		if (client.player == null || connection == null || connection.getCommands() == null) {
			return;
		}

		this.initialUpdateCountdown = -1;
		this.requestUpdate();
	}

	public Map<String, Long> getItems() {
		synchronized (this.items) {
			return Map.copyOf(this.items);
		}
	}

	public long getCount(String itemId) {
		synchronized (this.items) {
			return this.items.getOrDefault(itemId, 0L);
		}
	}

	public void clear() {
		synchronized (this.items) {
			this.items.clear();
		}
	}

	public void takeItem(String itemId, int amount) {
		if (amount <= 0) {
			return;
		}

		Identifier identifier = Identifier.tryParse(itemId);
		Minecraft client = Minecraft.getInstance();
		ClientPacketListener connection = readyConnection(client);

		if (identifier == null || connection == null || getMaxStackSize(identifier) <= 0) {
			return;
		}

		this.applyOptimisticTake(itemId, amount);
		sendTakeCommand(client, "ci get", identifier, amount);
	}

	public void takeBox(String itemId, int amount) {
		if (amount <= 0) {
			return;
		}

		Identifier identifier = Identifier.tryParse(itemId);
		Minecraft client = Minecraft.getInstance();
		ClientPacketListener connection = readyConnection(client);

		if (identifier == null || connection == null) {
			return;
		}

		int maxStackSize = getMaxStackSize(identifier);

		if (maxStackSize <= 0) {
			return;
		}

		long requiredAmount = CloudStorageMath.getBoxItemCount(amount, maxStackSize);
		synchronized (this.items) {
			long currentCount = this.items.getOrDefault(itemId, 0L);

			if (currentCount >= requiredAmount) {
				this.updateCountLocked(itemId, currentCount - requiredAmount);
			}
		}

		sendTakeCommand(client, "ci getbox", identifier, amount);
	}

	private void applyOptimisticTake(String itemId, int amount) {
		synchronized (this.items) {
			long currentCount = this.items.getOrDefault(itemId, 0L);

			if (amount <= currentCount) {
				this.updateCountLocked(itemId, currentCount - amount);
			}
		}
	}

	private void updateCountLocked(String itemId, long remaining) {
		if (remaining <= 0) {
			this.items.remove(itemId);
		} else {
			this.items.put(itemId, remaining);
		}
	}

	private static ClientPacketListener readyConnection(Minecraft client) {
		ClientPacketListener connection = client.getConnection();
		return connection != null && connection.getCommands() != null ? connection : null;
	}

	private static int getMaxStackSize(Identifier itemId) {
		return BuiltInRegistries.ITEM.get(itemId)
				.map(item -> item.value().getDefaultMaxStackSize())
				.orElse(-1);
	}

	private static void sendTakeCommand(
			Minecraft client,
			String command,
			Identifier itemId,
			int amount
	) {
		client.execute(() -> {
			ClientPacketListener activeConnection = readyConnection(client);

			if (activeConnection != null) {
				activeConnection.sendCommand(command + " " + itemId.getPath() + " " + amount);
			}
		});
	}

	public void requestUpdate() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();

		if (connection != null && connection.getCommands() != null) {
			connection.sendCommand("ci textlist");
		}
	}

	public void requestPut() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();

		if (connection != null && connection.getCommands() != null) {
			connection.sendCommand("ci put");
		}
	}
}
