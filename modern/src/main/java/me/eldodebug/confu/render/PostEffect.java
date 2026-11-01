package me.eldodebug.confu.render;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.mixin.PostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

/** Lazily built 1.20.1 replacement for the 1.8.9 ShaderGroup used by the post-process mods. */
public final class PostEffect {

	private final Identifier id;
	private PostEffectProcessor processor;
	private int width = -1;
	private int height = -1;

	public PostEffect(String name) {
		this.id = new Identifier("minecraft", "shaders/post/" + name + ".json");
	}

	/** Creates the group when missing and keeps it sized to the window; returns true when it was just created. */
	public boolean ensure() {
		MinecraftClient mc = MinecraftClient.getInstance();
		int framebufferWidth = mc.getWindow().getFramebufferWidth();
		int framebufferHeight = mc.getWindow().getFramebufferHeight();

		if (processor == null) {
			try {
				processor = new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), id);
				width = framebufferWidth;
				height = framebufferHeight;
				processor.setupDimensions(framebufferWidth, framebufferHeight);
				return true;
			} catch (Throwable t) {
				ConfuClient.LOGGER.error("Could not load post effect {}", id, t);
				processor = null;
				return false;
			}
		}

		if (width != framebufferWidth || height != framebufferHeight) {
			width = framebufferWidth;
			height = framebufferHeight;
			processor.setupDimensions(framebufferWidth, framebufferHeight);
		}
		return false;
	}

	public boolean ready() {
		return processor != null;
	}

	public void reset() {
		if (processor != null) {
			processor.close();
			processor = null;
		}
		width = -1;
		height = -1;
	}

	public void render(float tickDelta) {
		if (processor != null) {
			processor.render(tickDelta);
		}
	}

	public void setUniform(String name, float value) {
		if (processor == null) {
			return;
		}
		for (PostEffectPass pass : ((PostEffectProcessorAccessor) processor).confu$getPasses()) {
			pass.getProgram().getUniformByNameOrDummy(name).set(value);
		}
	}
}
