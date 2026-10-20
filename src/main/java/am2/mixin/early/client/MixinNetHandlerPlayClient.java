package am2.mixin.early.client;

import am2.utility.IPotionIdPacket;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Uses the full potion ID of S1DPacketEntityEffect (see MixinS1DPacketEntityEffect); vanilla read it as a signed byte. */
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient{

	@Shadow
	private WorldClient clientWorldController;

	@Inject(method = "handleEntityEffect", at = @At("HEAD"), cancellable = true)
	private void am2$fullPotionId(S1DPacketEntityEffect packet, CallbackInfo ci){
		Entity entity = clientWorldController.getEntityByID(packet.func_149426_d());
		if (entity instanceof EntityLivingBase){
			PotionEffect effect = new PotionEffect(((IPotionIdPacket)packet).am2$getPotionId(), packet.func_149425_g(), packet.func_149428_f());
			effect.setPotionDurationMax(packet.func_149429_c());
			((EntityLivingBase)entity).addPotionEffect(effect);
		}
		ci.cancel();
	}
}
