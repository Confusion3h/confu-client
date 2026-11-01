package me.eldodebug.confu.module.impl.rearview;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

/**
 * 1.8.9 RearviewCamera — re-enters the world render into an 800x600 FBO with the
 * view entity's yaw flipped 180 degrees. hideGUI/currentScreen/fov overrides and the
 * second RenderGlobal have no 1.20.1 counterparts: the HUD, fire/water/pumpkin overlays
 * and the hand live outside renderWorld there, so only the hand needs muting.
 */
public final class RearviewCamera {

	public static final int MIRROR_WIDTH = 800;
	public static final int MIRROR_HEIGHT = 600;

	private static boolean recording;

	public static boolean isRecording() {
		return recording;
	}

	private final MinecraftClient mc = MinecraftClient.getInstance();

	private int mirrorFBO;
	private int mirrorTex;
	private int mirrorDepth;
	private boolean firstUpdate;
	private float fov;
	private boolean lockCamera = true;

	public RearviewCamera() {
		fov = 70;
	}

	private void initGl() {

		int prevUnit = GL13.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		mirrorFBO = GL30.glGenFramebuffers();
		mirrorTex = GL11.glGenTextures();
		mirrorDepth = GL11.glGenTextures();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, MIRROR_WIDTH, MIRROR_HEIGHT, 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, (IntBuffer) null);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT24, MIRROR_WIDTH, MIRROR_HEIGHT, 0,
				GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (IntBuffer) null);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, mirrorFBO);
		attach();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
		GL13.glActiveTexture(prevUnit);
	}

	private void attach() {
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, mirrorTex, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, mirrorDepth, 0);
	}

	public void updateMirror(float partialTicks) {

		if (!firstUpdate) {
			initGl();
			firstUpdate = true;
		}

		Entity view = mc.getCameraEntity();
		if (view == null) {
			return;
		}

		GameRenderer renderer = mc.gameRenderer;
		Perspective perspective = mc.options.getPerspective();

		int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		int prevUnit = GL13.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		mc.options.setPerspective(Perspective.FIRST_PERSON);
		renderer.setRenderHand(false);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, mirrorFBO);
		GL11.glViewport(0, 0, MIRROR_WIDTH, MIRROR_HEIGHT);

		float y = view.getYaw();
		float py = view.prevYaw;
		float p = view.getPitch();
		float pp = view.prevPitch;

		view.setYaw(y + 180F);
		view.prevYaw = py + 180F;
		if (lockCamera) {
			view.setPitch(0F);
			view.prevPitch = 0F;
		} else {
			view.setPitch(-p + 18F);
			view.prevPitch = -pp + 18F;
		}

		recording = true;
		try {
			GL11.glPushAttrib(272393);
			renderer.renderWorld(partialTicks, System.nanoTime(), new MatrixStack());
			GL11.glPopAttrib();
		} finally {
			recording = false;

			view.setYaw(y);
			view.prevYaw = py;
			view.setPitch(p);
			view.prevPitch = pp;
			if (perspective != Perspective.FIRST_PERSON) {
				mc.options.setPerspective(perspective);
			}
			renderer.setRenderHand(true);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
			GL11.glViewport(0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
			GL13.glActiveTexture(prevUnit);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
		}
	}

	public int getTexture() {
		return mirrorTex;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public void setLockCamera(boolean lockCamera) {
		this.lockCamera = lockCamera;
	}
}
