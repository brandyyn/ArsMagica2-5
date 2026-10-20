package am2.mixin.early;

import am2.utility.IPotionIdPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sends the full potion ID as an int. A vanilla-format packet (byte ID) is still accepted: after the entity ID it has
 * 4 readable bytes instead of 7.
 */
@Mixin(S1DPacketEntityEffect.class)
public abstract class MixinS1DPacketEntityEffect implements IPotionIdPacket{

	@Unique
	private int am2$potionId;

	@Inject(method = "<init>(ILnet/minecraft/potion/PotionEffect;)V", at = @At("RETURN"))
	private void am2$capturePotionId(int entityId, PotionEffect effect, CallbackInfo ci){
		am2$potionId = effect.getPotionID();
	}

	@Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readByte()B", ordinal = 0))
	private byte am2$readPotionId(PacketBuffer buffer){
		am2$potionId = buffer.readableBytes() == 7 ? buffer.readInt() : buffer.readByte() & 255;
		return (byte)am2$potionId;
	}

	@Redirect(method = "writePacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeByte(I)Lio/netty/buffer/ByteBuf;", ordinal = 0))
	private io.netty.buffer.ByteBuf am2$writePotionId(PacketBuffer buffer, int truncated){
		return buffer.writeInt(am2$potionId);
	}

	@Override
	public int am2$getPotionId(){
		return am2$potionId;
	}
}
