package me.eldodebug.soar.management.cape.impl;

import me.eldodebug.soar.management.cape.CapeCategory;
import me.eldodebug.soar.compat.ResourceLocation;

public class NormalCape extends Cape {

	private ResourceLocation sample;
	
	public NormalCape(String name, ResourceLocation sample, ResourceLocation cape, CapeCategory category) {
		super(name, cape, category);
		this.sample = sample;
	}

	public ResourceLocation getSample() {
		return sample;
	}
}
