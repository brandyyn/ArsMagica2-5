package am2.mixin.early;

import am2.playerextensions.ExtendedProperties;
import am2.utility.IExtendedPropertiesHolder;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements IExtendedPropertiesHolder{

	@Unique
	private ExtendedProperties am2$extendedProperties;

	@Override
	public ExtendedProperties am2$getExtendedProperties(){
		return am2$extendedProperties;
	}

	@Override
	public void am2$setExtendedProperties(ExtendedProperties properties){
		am2$extendedProperties = properties;
	}
}
