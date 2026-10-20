package am2.mixin.early;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Potion IDs above 127 (AM2 extends the potion array to 255): effect IDs are saved as ints instead of bytes.
 * Not applied when DragonAPI is present, which extends potion IDs itself.
 */
@Mixin(PotionEffect.class)
public abstract class MixinPotionEffect{

	@Shadow
	public abstract int getPotionID();

	@Redirect(method = "writeCustomPotionEffectToNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setByte(Ljava/lang/String;B)V", ordinal = 0))
	private void am2$writeIntId(NBTTagCompound nbt, String key, byte truncated){
		nbt.setInteger(key, getPotionID());
	}

	@Inject(method = "readCustomPotionEffectFromNBT", at = @At("HEAD"), cancellable = true)
	private static void am2$readIntId(NBTTagCompound nbt, CallbackInfoReturnable<PotionEffect> cir){
		// byte tags (vanilla saves) are read unsigned, so IDs 128-255 saved by vanilla code survive too
		int id = nbt.func_150299_b("Id") == 1 ? nbt.getByte("Id") & 255 : nbt.getInteger("Id");
		if (id >= 0 && id < Potion.potionTypes.length && Potion.potionTypes[id] != null)
			cir.setReturnValue(new PotionEffect(id, nbt.getInteger("Duration"), nbt.getByte("Amplifier"), nbt.getBoolean("Ambient")));
		else
			cir.setReturnValue(null);
	}
}
