package me.eldodebug.soar.management.cape.impl;

import java.io.File;

import me.eldodebug.soar.management.cape.CapeCategory;
import me.eldodebug.soar.management.cape.CapeStore;
import net.minecraft.util.ResourceLocation;

public class CustomCape extends Cape {

	private final CapeStore.Entry entry;

	public CustomCape(String name, CapeStore.Entry entry, ResourceLocation cape, CapeCategory category) {
		super(name, cape, category);
		this.entry = entry;
	}

	public File getSample() {
		return entry.preview;
	}

	public CapeStore.Entry getEntry() {
		return entry;
	}
}
