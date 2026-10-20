package am2.mixin.early;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Full int potion IDs; a vanilla-format packet (unsigned byte ID) is still accepted. */
@Mixin(S1EPacketRemoveEntityEffect.class)
public abstract class MixinS1EPacketRemoveEntityEffect{

	@Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readUnsignedByte()S"))
	private short am2$readPotionId(PacketBuffer buffer){
		return (short)(buffer.readableBytes() == 4 ? buffer.readInt() : buffer.readUnsignedByte());
	}

	@Redirect(method = "writePacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeByte(I)Lio/netty/buffer/ByteBuf;"))
	private io.netty.buffer.ByteBuf am2$writePotionId(PacketBuffer buffer, int potionId){
		return buffer.writeInt(potionId);
	}
}
