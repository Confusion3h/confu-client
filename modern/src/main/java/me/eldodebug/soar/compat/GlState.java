package me.eldodebug.soar.compat;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * NanoVG's GL3 backend changes raw GL state that Minecraft's GlStateManager caches. The 1.8.9
 * client wrapped NanoVG in glPushAttrib/glPopAttrib; a core profile has no attribute stack, so
 * the state NanoVG touches is read before the frame and written back after it.
 */
public final class GlState {

	private int program, vao, arrayBuffer, activeTexture, texture0, unpackAlignment, unpackRowLength, unpackSkipPixels, unpackSkipRows;
	private boolean blend, cull, depth, stencil, scissor;
	private int blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha, blendEquation, cullFace, frontFace;
	private int stencilMask, stencilFunc, stencilRef, stencilValueMask, stencilFail, stencilPassDepthFail, stencilPassDepthPass;
	private final int[] colorMask = new int[4];
	private final boolean[] colorMaskBool = new boolean[4];

	public static GlState save() {
		GlState s = new GlState();
		s.program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		s.vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		s.arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		s.activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		s.texture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		s.unpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
		s.unpackRowLength = GL11.glGetInteger(GL11.GL_UNPACK_ROW_LENGTH);
		s.unpackSkipPixels = GL11.glGetInteger(GL11.GL_UNPACK_SKIP_PIXELS);
		s.unpackSkipRows = GL11.glGetInteger(GL11.GL_UNPACK_SKIP_ROWS);
		s.blend = GL11.glIsEnabled(GL11.GL_BLEND);
		s.cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
		s.depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		s.stencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
		s.scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
		s.blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
		s.blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
		s.blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
		s.blendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
		s.blendEquation = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
		s.cullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
		s.frontFace = GL11.glGetInteger(GL11.GL_FRONT_FACE);
		s.stencilMask = GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK);
		s.stencilFunc = GL11.glGetInteger(GL11.GL_STENCIL_FUNC);
		s.stencilRef = GL11.glGetInteger(GL11.GL_STENCIL_REF);
		s.stencilValueMask = GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK);
		s.stencilFail = GL11.glGetInteger(GL11.GL_STENCIL_FAIL);
		s.stencilPassDepthFail = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
		s.stencilPassDepthPass = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
		GL11.glGetIntegerv(GL11.GL_COLOR_WRITEMASK, s.colorMask);
		for (int i = 0; i < 4; i++) {
			s.colorMaskBool[i] = s.colorMask[i] != 0;
		}
		return s;
	}

	public void restore() {
		GL20.glUseProgram(program);
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBuffer);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture0);
		GL13.glActiveTexture(activeTexture);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, unpackAlignment);
		GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, unpackRowLength);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, unpackSkipPixels);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, unpackSkipRows);
		set(GL11.GL_BLEND, blend);
		set(GL11.GL_CULL_FACE, cull);
		set(GL11.GL_DEPTH_TEST, depth);
		set(GL11.GL_STENCIL_TEST, stencil);
		set(GL11.GL_SCISSOR_TEST, scissor);
		GL14.glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
		GL14.glBlendEquation(blendEquation);
		GL11.glCullFace(cullFace);
		GL11.glFrontFace(frontFace);
		GL11.glStencilMask(stencilMask);
		GL11.glStencilFunc(stencilFunc, stencilRef, stencilValueMask);
		GL11.glStencilOp(stencilFail, stencilPassDepthFail, stencilPassDepthPass);
		GL11.glColorMask(colorMaskBool[0], colorMaskBool[1], colorMaskBool[2], colorMaskBool[3]);
	}

	private static void set(int cap, boolean on) {
		if (on) {
			GL11.glEnable(cap);
		} else {
			GL11.glDisable(cap);
		}
	}
}
