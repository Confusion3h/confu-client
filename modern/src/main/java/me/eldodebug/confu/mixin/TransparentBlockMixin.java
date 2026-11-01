package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.ClearGlassModule;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransparentBlock.class)
public abstract class TransparentBlockMixin {

	@Inject(method = "isSideInvisible", at = @At("HEAD"), cancellable = true)
	private void confu$clearGlass(BlockState state, BlockState stateFrom, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClearGlassModule mod = ClearGlassModule.get();
		if (mod == null || !mod.isEnabled()) {
			return;
		}
		if (state.getBlock() instanceof StainedGlassBlock) {
			if (mod.stained()) {
				cir.setReturnValue(true);
			}
		} else if (state.getBlock() instanceof GlassBlock) {
			if (mod.normal()) {
				cir.setReturnValue(true);
			}
		}
	}
}
