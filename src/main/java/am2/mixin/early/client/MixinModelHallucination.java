package am2.mixin.early.client;

import am2.entities.EntityHallucination;
import net.minecraft.client.model.ModelMagmaCube;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hallucinations borrow these models but are not magma cubes or skeletons. */
@Mixin({ModelMagmaCube.class, ModelSkeleton.class})
public abstract class MixinModelHallucination{

	@Inject(method = "setLivingAnimations", at = @At("HEAD"), cancellable = true)
	private void am2$skipHallucination(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, CallbackInfo ci){
		if (entity instanceof EntityHallucination)
			ci.cancel();
	}
}
