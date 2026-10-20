package am2.mixin.early;

import am2.TimeManipulation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Dilute Time: World.updateEntities only ticks a tile entity while hasWorldObj() is true, so a slowed tile answers false
 * on the ticks it skips. This runs for every tile entity tick, so it stays a field read while nothing is slowed.
 */
@Mixin(TileEntity.class)
public abstract class MixinTileEntity{

	@Shadow
	protected World worldObj;

	@ModifyReturnValue(method = "hasWorldObj", at = @At("RETURN"))
	private boolean am2$diluteTime(boolean hasWorld){
		return hasWorld && !TimeManipulation.isTileSlowedThisTick((TileEntity)(Object)this, worldObj);
	}
}
