package com.cloudstoragerei.compat;

import com.cloudstoragerei.compat.litematica.LitematicaSchematicResolver;

final class LitematicaTargetBootstrap {
	private LitematicaTargetBootstrap() {
	}

	static void register() {
		CloudStorageAdapters.registerWorldItemResolver(new LitematicaSchematicResolver());
	}
}
