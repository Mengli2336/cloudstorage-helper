package com.cloudstoragerei.compat.litematica;

import org.jetbrains.annotations.Nullable;

import com.cloudstoragerei.compat.WorldItemResolver;
import com.cloudstoragerei.config.CloudStorageConfigs;

import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class LitematicaSchematicResolver implements WorldItemResolver {
	@Nullable
	@Override
	public ItemStack getTargetedItem() {
		if (!CloudStorageConfigs.Compatibility.LITEMATICA_ENABLED.getBooleanValue()) {
			return null;
		}

		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.level == null) {
			return null;
		}

		WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();

		if (schematicWorld == null) {
			return null;
		}

		double reach = WorldUtils.getValidBlockRange(client);
		RayTraceUtils.RayTraceWrapper trace = RayTraceUtils.getGenericTrace(
				client.level,
				client.player,
				reach
		);

		if (trace == null
				|| (trace.getHitType() != RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK
				&& trace.getHitType() != RayTraceUtils.RayTraceWrapper.HitType.MISMATCH_OVERLAY)) {
			trace = RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(
					client.level,
					client.player,
					reach
			);
		}

		BlockHitResult hitResult = trace != null ? trace.getBlockHitResult() : null;

		if (hitResult == null) {
			if (client.hitResult instanceof BlockHitResult vanillaHit) {
				BlockPos vanillaPos = vanillaHit.getBlockPos();

				if (!schematicWorld.getBlockState(vanillaPos).isAir()) {
					hitResult = vanillaHit;
				}
			}
		}

		if (hitResult == null) {
			return null;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = schematicWorld.getBlockState(pos);

		if (state.isAir()) {
			return null;
		}

		ItemStack stack = MaterialCache.getInstance()
				.getRequiredBuildItemForState(state, schematicWorld, pos);
		return stack.isEmpty() ? null : stack.copy();
	}
}
