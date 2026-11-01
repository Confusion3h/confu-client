package me.eldodebug.confu.mixin;

import me.eldodebug.confu.render.FemaleGenderModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin implements FemaleGenderModel, PlayerModelAccess {

	@Shadow @Final
	private boolean thinArms;

	@Unique
	private ModelPart confu$boobs;

	@Override
	public boolean confu$isThinArms() {
		return thinArms;
	}
	@Unique
	private boolean confu$femaleActive;
	@Unique
	private boolean confu$femaleSneaking;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void confu$initBoobs(ModelPart root, boolean thinArms, CallbackInfo ci) {
		confu$boobs = new ModelPart(
				List.of(new ModelPart.Cuboid(16, 20, -4.0F, -1.5F, -5.0F, 8.0F, 4.0F, 4.0F, 0.0F, 0.0F, 0.0F, false, 64.0F, 64.0F, EnumSet.allOf(Direction.class))),
				Map.of());
	}

	@Override
	public void confu$setFemaleGender(boolean active, boolean sneaking) {
		confu$femaleActive = active;
		confu$femaleSneaking = sneaking;
	}

	@Override
	public void confu$renderFemaleGender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		if (!confu$femaleActive || confu$boobs == null) {
			return;
		}
		matrices.push();
		matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotation(45.0F));
		matrices.translate(0.0F, confu$femaleSneaking ? 0.25F * 0.0625F : 0.0F, confu$femaleSneaking ? 0.1F * 0.0625F : 0.0F);
		confu$boobs.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		matrices.pop();
	}
}
