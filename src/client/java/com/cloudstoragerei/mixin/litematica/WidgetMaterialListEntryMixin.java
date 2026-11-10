package com.cloudstoragerei.mixin.litematica;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;

import com.cloudstoragerei.config.CloudStorageConfigs;
import com.cloudstoragerei.core.CloudStorageManager;

/**
 * Adds the cloud storage column and the missing column to the Litematica material list.
 */
@Mixin(WidgetMaterialListEntry.class)
public abstract class WidgetMaterialListEntryMixin {
	private static final int MISSING_COLUMN_OFFSET = 64;
	private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF;

	@Shadow private MaterialListEntry entry;
	@Shadow private MaterialListBase materialList;
	@Shadow private WidgetListMaterialList listWidget;
	@Shadow protected abstract int getColumnPosX(int column);

	@Inject(method = "render", at = @At("TAIL"))
	private void cloudstorage$renderCloudColumns(
			GuiContext ctx,
			int mouseX,
			int mouseY,
			boolean selected,
			CallbackInfo ci
	) {
		if (!CloudStorageConfigs.Compatibility.MATERIAL_LIST_CLOUD.getBooleanValue()) {
			return;
		}

		// Shadows only resolve members declared in the target class, so the inherited widget
		// accessors are reached through the base class: this mixin instance is the target widget.
		WidgetBase widget = (WidgetBase) (Object) this;
		int cloudX = this.getColumnPosX(4);
		int missingX = cloudX + MISSING_COLUMN_OFFSET;
		int y = widget.getY() + 7;

		if (cloudX >= widget.getX() + widget.getWidth()) {
			return;
		}

		if (this.entry == null) {
			if (this.listWidget.getSearchBarWidget().isSearchOpen()) {
				return;
			}

			String bold = GuiBase.TXT_BOLD;
			String reset = GuiBase.TXT_RST;

			widget.drawString(ctx, cloudX, y, HIGHLIGHT_COLOR,
					bold + StringUtils.translate("cloudstorage-helper.material_list.cloud") + reset);
			widget.drawString(ctx, missingX, y, HIGHLIGHT_COLOR,
					bold + StringUtils.translate("cloudstorage-helper.material_list.missing") + reset);
			return;
		}

		int total = this.entry.getCountTotal() * this.materialList.getMultiplier();
		ItemStack stack = this.entry.getStack();
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		long cloud = itemId == null ? 0L : CloudStorageManager.getInstance().getCount(itemId.toString());
		long missing = Math.max(0L, (long) total - cloud);
		String green = GuiBase.TXT_GREEN;
		String red = GuiBase.TXT_RED;

		widget.drawString(ctx, cloudX, y, HIGHLIGHT_COLOR, (cloud >= total ? green : red) + cloud);
		widget.drawString(ctx, missingX, y, HIGHLIGHT_COLOR, (missing <= 0L ? green : red) + missing);
	}
}
