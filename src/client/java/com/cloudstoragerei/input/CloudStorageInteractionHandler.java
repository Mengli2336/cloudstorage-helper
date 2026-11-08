package com.cloudstoragerei.input;

import com.cloudstoragerei.compat.CloudItemTarget;
import com.cloudstoragerei.compat.CloudStorageAdapters;
import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.core.CloudStorageManager;
import com.cloudstoragerei.core.CloudStorageMath;
import com.cloudstoragerei.gui.CustomAmountScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public final class CloudStorageInteractionHandler {
	private static long suppressClicksUntil;

	private CloudStorageInteractionHandler() {
	}

	public static boolean performAction(CloudStorageAction action) {
		Minecraft client = Minecraft.getInstance();
		Screen screen = client.screen;

		if (action == CloudStorageAction.QUICK_TAKE) {
			return performQuickTake(client);
		}

		if (screen == null || screen instanceof CustomAmountScreen) {
			return false;
		}

		if (System.currentTimeMillis() < suppressClicksUntil) {
			return true;
		}

		CloudItemTarget target = findHoveredTarget(client, screen);

		if (target == null) {
			return false;
		}

		ItemStack stack = target.stack();
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		long count = CloudStorageManager.getInstance().getCount(itemId.toString());

		if (count <= 0) {
			return false;
		}

		return switch (action) {
			case TAKE_ONE -> {
				CloudStorageManager.getInstance().takeItem(itemId.toString(), 1);
				yield true;
			}
			case TAKE_STACK -> {
				int stackAmount = (int) Math.min(count, (long) stack.getMaxStackSize());
				CloudStorageManager.getInstance().takeItem(itemId.toString(), stackAmount);
				yield true;
			}
			case OPEN_AMOUNT -> {
				openCustomAmountScreen(screen, stack, itemId, count);
				yield true;
			}
			case QUICK_TAKE -> false;
		};
	}

	private static boolean performQuickTake(Minecraft client) {
		Screen screen = client.screen;

		if (screen != null) {
			return false;
		}

		if (System.currentTimeMillis() < suppressClicksUntil) {
			return true;
		}

		ItemStack targetedItem = findCrosshairTarget(client);

		if (targetedItem == null || targetedItem.isEmpty()) {
			return false;
		}

		if (!takeMaxStack(targetedItem)) {
			return false;
		}

		return true;
	}

	private static ItemStack findCrosshairTarget(Minecraft client) {
		if (client.hitResult instanceof EntityHitResult entityHit) {
			if (entityHit.getEntity() instanceof ItemEntity itemEntity) {
				return itemEntity.getItem();
			}

			return null;
		}

		if (CloudStorageConfigs.Compatibility.LITEMATICA_ENABLED.getBooleanValue()) {
			ItemStack schematicItem = CloudStorageAdapters.findTargetedWorldItem();

			if (schematicItem != null && !schematicItem.isEmpty()) {
				return schematicItem;
			}
		}

		if (client.hitResult instanceof BlockHitResult blockHit && client.level != null) {
			BlockState state = client.level.getBlockState(blockHit.getBlockPos());
			ItemStack stack = state.getBlock().asItem().getDefaultInstance();
			return stack.isEmpty() ? null : stack;
		}

		return null;
	}

	private static CloudItemTarget findHoveredTarget(Minecraft client, Screen screen) {
		double mouseX = client.mouseHandler.xpos()
				* (double) client.getWindow().getGuiScaledWidth()
				/ (double) client.getWindow().getScreenWidth();
		double mouseY = client.mouseHandler.ypos()
				* (double) client.getWindow().getGuiScaledHeight()
				/ (double) client.getWindow().getScreenHeight();
		return CloudStorageAdapters.findHoveredItem(screen, mouseX, mouseY);
	}

	private static boolean takeMaxStack(ItemStack stack) {
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		long available = CloudStorageManager.getInstance().getCount(itemId.toString());
		int amount = CloudStorageMath.clampTakeAmount(stack.getMaxStackSize(), available);

		if (amount <= 0) {
			return false;
		}

		CloudStorageManager.getInstance().takeItem(itemId.toString(), amount);
		return true;
	}

	private static void openCustomAmountScreen(
			Screen parent,
			ItemStack stack,
			Identifier itemId,
			long count
	) {
		Minecraft client = Minecraft.getInstance();
		String itemName = stack.getHoverName().getString();

		client.execute(() -> client.setScreen(
				new CustomAmountScreen(parent, itemId.toString(), itemName, count)
		));
	}

	public static void suppressClicksAfterClosing() {
		suppressClicksUntil = System.currentTimeMillis() + 250L;
	}
}
