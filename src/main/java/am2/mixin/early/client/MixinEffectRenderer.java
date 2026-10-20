package am2.mixin.early.client;

import am2.network.AMPacketProcessorClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer{

	/** Cloaking hides all particles. */
	@Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
	private void am2$cloaking(Entity viewer, float partialTicks, CallbackInfo ci){
		if (AMPacketProcessorClient.cloaking > 0)
			ci.cancel();
	}
}
