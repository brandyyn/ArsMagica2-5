package am2.mixin.early;

import am2.utility.AMHooks;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockLiquid.class)
public abstract class MixinBlockLiquid{

	/** Currents do not push players whose archmage leggings let them walk through water. */
	@Inject(method = "velocityToAddToEntity", at = @At("HEAD"), cancellable = true)
	private void am2$noCurrentForWaterWalkers(World world, int x, int y, int z, Entity entity, Vec3 velocity, CallbackInfo ci){
		if (AMHooks.ignoresWater(entity))
			ci.cancel();
	}
}
