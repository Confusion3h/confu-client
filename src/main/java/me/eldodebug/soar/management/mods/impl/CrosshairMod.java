package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRenderCrosshair;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ColorSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class CrosshairMod extends Mod {

	private BooleanSetting hideThirdPersonViewSetting = new BooleanSetting(TranslateText.HIDE_THIRD_PERSON_VIEW, this, false);
	private BooleanSetting customCrosshairSetting = new BooleanSetting(TranslateText.CUSTOM_CROSSHAIR, this, false);
	private NumberSetting typeSetting = new NumberSetting(TranslateText.TYPE, this, 1, 0, 8, true);
	private NumberSetting scaleSetting = new NumberSetting(TranslateText.SCALE, this, 1, 0.3, 2, false);
	private ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, Color.WHITE, true);

	private static final ResourceLocation SHEET = new ResourceLocation("soar/crosshair.png");
	private static boolean[][][] masks;
	
	public CrosshairMod() {
		super(TranslateText.CROSSHAIR, TranslateText.CROSSHAIR_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onRenderCrosshair(EventRenderCrosshair event) {
		
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        
        if((hideThirdPersonViewSetting.isToggled() && mc.gameSettings.thirdPersonView != 0)) {
        	event.setCancelled(true);
        }
        
        if(customCrosshairSetting.isToggled()) {
        	
        	event.setCancelled(true);
        	
        	if((!hideThirdPersonViewSetting.isToggled()) || (hideThirdPersonViewSetting.isToggled() && mc.gameSettings.thirdPersonView == 0)) {
        		
        		int index = typeSetting.getValueInt();
        		Color c = colorSetting.getColor();
        		
                GlStateManager.enableBlend();
                if(isInvertColor()) {
                	GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
                } else {
                	GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                	GlStateManager.color(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, c.getAlpha() / 255F);
                }
                GlStateManager.enableAlpha();
                GlStateManager.enableDepth();
            	mc.getTextureManager().bindTexture(new ResourceLocation("soar/crosshair.png"));
            	GlUtils.startScale(sr.getScaledWidth() / 2 - 7, sr.getScaledHeight() / 2 - 7, 16, 16, scaleSetting.getValueFloat());
            	RenderUtils.drawTexturedModalRect(sr.getScaledWidth() / 2 - 7, sr.getScaledHeight() / 2 - 7, index * 16, 0, 16, 16);
            	GlUtils.stopScale();
            	GlStateManager.color(1, 1, 1, 1);
            	GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        	}
        }
	}

	/** Plain white keeps vanilla's inverted-colour crosshair; any other colour tints it. */
	private boolean isInvertColor() {
		Color c = colorSetting.getColor();
		return c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255 && c.getAlpha() == 255;
	}

	/**
	 * Live preview for the mod menu: draws the current crosshair with NanoVG, one rect per
	 * opaque pixel of its 16x16 sprite, so Type, Color and Scale changes show at once.
	 */
	public void drawPreview(NanoVGManager nvg, float x, float y, float size) {
		nvg.drawRoundedRect(x, y, size, size, 8, new Color(112, 150, 205));
		nvg.drawRoundedRectVarying(x, y + size * 0.6F, size, size * 0.4F, 0, 0, 8, 8, new Color(92, 132, 58));
		boolean custom = customCrosshairSetting.isToggled();
		boolean[][] mask = custom ? mask(typeSetting.getValueInt()) : vanillaMask();
		if(mask == null) {
			return;
		}
		Color c = custom && !isInvertColor() ? colorSetting.getColor() : Color.WHITE;
		float px = size / 32F * (custom ? scaleSetting.getValueFloat() : 1F);
		float ox = x + size / 2 - 8 * px, oy = y + size / 2 - 8 * px;
		for(int j = 0; j < 16; j++) {
			for(int i = 0; i < 16; i++) {
				if(mask[j][i]) {
					nvg.drawRect(ox + i * px, oy + j * px, px + 0.05F, px + 0.05F, c);
				}
			}
		}
	}

	private static boolean[][] vanillaMask() {
		boolean[][] m = new boolean[16][16];
		for(int k = 3; k <= 11; k++) {
			m[7][k] = true;
			m[k][7] = true;
		}
		return m;
	}

	private static boolean[][] mask(int index) {
		if(masks == null) {
			masks = new boolean[9][][];
			try(InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(SHEET).getInputStream()) {
				BufferedImage img = ImageIO.read(in);
				int cell = Math.max(1, img.getWidth() / 256);
				for(int t = 0; t < 9; t++) {
					boolean[][] m = new boolean[16][16];
					for(int j = 0; j < 16; j++) {
						for(int i = 0; i < 16; i++) {
							m[j][i] = (img.getRGB((t * 16 + i) * cell, j * cell) >>> 24) > 40;
						}
					}
					masks[t] = m;
				}
			} catch(Exception e) {
				return null;
			}
		}
		return index >= 0 && index < masks.length ? masks[index] : null;
	}
}
