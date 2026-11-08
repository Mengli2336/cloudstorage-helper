package com.cloudstoragerei.compat;

import com.cloudstoragerei.compat.rei.CloudStorageReiAdapter;

final class ReiAdapterBootstrap {
	private ReiAdapterBootstrap() {
	}

	static void register() {
		CloudStorageAdapters.register(new CloudStorageReiAdapter());
	}
}
