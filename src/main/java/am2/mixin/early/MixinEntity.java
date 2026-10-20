package am2.mixin.early;

import am2.utility.IForgeEntityData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class MixinEntity implements IForgeEntityData{

	@Shadow
	public boolean noClip;

	/** Forge field (not obfuscated). */
	@Shadow(remap = false)
	private NBTTagCompound customEntityData;

	/** No-clipping entities (ethereal players) do not suffocate in blocks. */
	@ModifyReturnValue(method = "isEntityInsideOpaqueBlock", at = @At("RETURN"))
	private boolean am2$noSuffocationWhileNoClip(boolean inside){
		return inside && !noClip;
	}

	@Override
	public NBTTagCompound am2$getCustomEntityData(){
		return customEntityData;
	}
}
