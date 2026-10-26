package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventPlayerHeadRotation;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class FreelookMod extends Mod {

	private boolean active;
	private float yaw;
	private float pitch;
	private int previousPerspective;
	private boolean toggled;
	/** Still easing back to the player's view after the key was released. */
	private boolean returning;
	private SimpleAnimation transition = new SimpleAnimation(0.0F);
	
	private BooleanSetting invertYawSetting = new BooleanSetting(TranslateText.INVERT_YAW, this, false);
	private BooleanSetting invertPitchSetting = new BooleanSetting(TranslateText.INVERT_PITCH, this, false);
	private ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.KEYDOWN, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.TOGGLE), new Option(TranslateText.KEYDOWN))));
	private BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH_FREELOOK, this, true);
	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_V);
	
	public FreelookMod() {
		super(TranslateText.FREELOOK, TranslateText.FREELOOK_DESCRIPTION, ModCategory.PLAYER,"perspectivemod", true);
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		
		Option mode = modeSetting.getOption();
		
		if(mode.getTranslate().equals(TranslateText.KEYDOWN)) {
			if(keybindSetting.isKeyDown()) {
				start();
			}
			else {
				stop();
			}
		}
		
		if(mode.getTranslate().equals(TranslateText.TOGGLE)) {
			if(toggled) {
				start();
			}else {
				stop();
			}
		}
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		
		Option mode = modeSetting.getOption();
		
		if(mode.getTranslate().equals(TranslateText.TOGGLE)) {
			if(keybindSetting.isKeyDown() ) {
				toggled = !toggled;
			}
		}
		
		if(event.getKeyCode() == mc.gameSettings.keyBindTogglePerspective.getKeyCode()) {
			toggled = false;
		}
	}
	
	@EventTarget
	public void onCameraRotation(EventCameraRotation event) {
		if(!active && !returning) {
			return;
		}
		float p = 1F;
		if(smoothSetting.isToggled()) {
			transition.setAnimation(active ? 1.0F : 0.0F, 12);
			p = Math.max(0F, Math.min(1F, transition.getValue()));
		}
		float baseYaw = event.getYaw();
		float basePitch = event.getPitch();
		event.setYaw(baseYaw + MathHelper.wrapAngleTo180_float(yaw - baseYaw) * p);
		event.setPitch(basePitch + (pitch - basePitch) * p);
		event.setThirdPersonDistance(event.getThirdPersonDistance() * p);
		if(returning && p < 0.02F) {
			finishReturn();
		}
	}
	
	@EventTarget
	public void onPlayerHeadRotation(EventPlayerHeadRotation event) {
		
		if(active) {
			float yaw = event.getYaw();
			float pitch = event.getPitch();
			event.setCancelled(true);
			pitch = -pitch;
			
			if(!invertPitchSetting.isToggled()) {
				pitch = -pitch;
			}
			
			if(invertYawSetting.isToggled()) {
				 yaw = -yaw;
			}
			
			this.yaw += yaw * 0.15F;
			this.pitch = MathHelper.clamp_float(this.pitch + (pitch * 0.15F), -90, 90);
			mc.renderGlobal.setDisplayListEntitiesDirty();
		}
	}
	
	private void start() {
		if(!active) {
			active = true;
			Entity renderView = mc.getRenderViewEntity();
			if(!returning) {
				// keep the free angle when re-pressed mid-return, so there is no jump
				yaw = renderView.rotationYaw;
				pitch = renderView.rotationPitch;
				previousPerspective = mc.gameSettings.thirdPersonView;
				transition = new SimpleAnimation(smoothSetting.isToggled() ? 0.0F : 1.0F);
			}
			returning = false;
			mc.gameSettings.thirdPersonView = 3;
		}
	}
	
	private void stop() {
		if(active) {
			active = false;
			if(smoothSetting.isToggled()) {
				returning = true;
			} else {
				finishReturn();
			}
		}
	}

	private void finishReturn() {
		returning = false;
		mc.gameSettings.thirdPersonView = previousPerspective;
		mc.renderGlobal.setDisplayListEntitiesDirty();
	}
}
