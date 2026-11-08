package com.cloudstoragerei.compat;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.cloudstoragerei.CloudStorageModClient;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public final class CloudStorageAdapters {
	private static final List<ItemManagerAdapter> ADAPTERS = new ArrayList<>();
	@Nullable
	private static WorldItemResolver worldItemResolver;
	private static boolean initialized;

	private CloudStorageAdapters() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;
		FabricLoader loader = FabricLoader.getInstance();

		if (loader.isModLoaded("roughlyenoughitems")) {
			ReiAdapterBootstrap.register();
		}

		if (loader.isModLoaded("jei")) {
			JeiAdapterBootstrap.register();
		}

		if (loader.isModLoaded("litematica")) {
			LitematicaTargetBootstrap.register();
		}
	}

	public static void register(ItemManagerAdapter adapter) {
		ADAPTERS.removeIf(existing -> existing.id().equals(adapter.id()));
		ADAPTERS.add(adapter);
		CloudStorageModClient.LOGGER.info("Registered item manager adapter: {}", adapter.id());
	}

	public static void registerWorldItemResolver(WorldItemResolver resolver) {
		worldItemResolver = resolver;
		CloudStorageModClient.LOGGER.info("Registered world item resolver: litematica");
	}

	@Nullable
	public static CloudItemTarget findHoveredItem(Screen screen, double mouseX, double mouseY) {
		for (ItemManagerAdapter adapter : List.copyOf(ADAPTERS)) {
			if (!adapter.isEnabled()) {
				continue;
			}

			ItemStack stack = adapter.getHoveredItem(screen, mouseX, mouseY);

			if (stack != null && !stack.isEmpty()) {
				return new CloudItemTarget(adapter.id(), adapter, stack);
			}
		}

		return null;
	}

	@Nullable
	public static ItemStack findTargetedWorldItem() {
		if (worldItemResolver == null) {
			return null;
		}

		return worldItemResolver.getTargetedItem();
	}
}
