package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** 1.8.9 BlockInfoMod: looked-at block name + icon. */
public final class BlockInfoModule extends NvgHudModule {

	private BlockState state;
	private String label = "Air";

	public BlockInfoModule() {
		super("block_info", Category.HUD, "tt:BLOCK_INFO", "tt:BLOCK_INFO_DESCRIPTION", 0.5F, 0.02F);
	}

	@Override
	public void onTick() {
		if (isEditing() || mc.world == null) {
			state = null;
			label = "Stone";
			return;
		}
		HitResult hit = mc.crosshairTarget;
		if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
			state = mc.world.getBlockState(bhr.getBlockPos());
			label = state.getBlock().getName().getString();
		} else {
			state = null;
			label = "Air";
		}
	}

	@Override
	protected void drawNanoVG() {
		float tw = this.getTextWidth(label, 9, getHudFont(1));
		this.drawBackground(tw + 28, 22);
		this.drawText(label, 22, 7, 9, getHudFont(1));
		this.setWidth((int) (tw + 28));
		this.setHeight(22);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		ItemStack stack = state == null
				? new ItemStack(net.minecraft.item.Items.STONE)
				: state.getBlock().asItem().getDefaultStack();
		MatrixStack m = context.getMatrices();
		m.push();
		m.translate(px, py, 0);
		m.scale(scale, scale, 1F);
		context.drawItem(stack, 3, 3);
		m.pop();
	}
}
