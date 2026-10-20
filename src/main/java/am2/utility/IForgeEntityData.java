package am2.utility;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Implemented on Entity by am2.mixin: Forge's custom entity data without creating it. Entity.getEntityData() creates
 * (and from then on saves) an empty compound for every entity it is called on.
 */
public interface IForgeEntityData{

	/** @return the custom entity data, or null if the entity has none */
	NBTTagCompound am2$getCustomEntityData();
}
