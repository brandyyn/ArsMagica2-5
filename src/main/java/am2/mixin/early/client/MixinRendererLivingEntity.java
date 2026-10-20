package am2.mixin.early.client;

import am2.utility.RenderUtilities;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity{

	/** Shrink spell scaling. */
	@Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderLivingAt(Lnet/minecraft/entity/EntityLivingBase;DDD)V", shift = At.Shift.AFTER))
	private void am2$shrink(EntityLivingBase entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci){
		RenderUtilities.setupShrinkRender(entity);
	}
}
