package com.cloudstoragerei.compat;

import com.cloudstoragerei.compat.jei.CloudStorageJeiAdapter;

final class JeiAdapterBootstrap {
	private JeiAdapterBootstrap() {
	}

	static void register() {
		CloudStorageAdapters.register(new CloudStorageJeiAdapter());
	}
}
