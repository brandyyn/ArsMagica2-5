package am2.mixin.early;

import am2.blocks.liquid.BlockLiquidEssence;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid{

	/** Lava flowing onto liquid essence turns it into blue stained glass. */
	@Inject(method = "updateTick", at = @At("HEAD"))
	private void am2$lavaOnEssence(World world, int x, int y, int z, Random random, CallbackInfo ci){
		if (((BlockDynamicLiquid)(Object)this).getMaterial() == Material.lava && world.getBlock(x, y - 1, z) instanceof BlockLiquidEssence){
			world.setBlock(x, y - 1, z, Blocks.stained_glass, 11, 3);
			world.playSoundEffect(x + 0.5, y - 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			for (int l = 0; l < 8; ++l)
				world.spawnParticle("largesmoke", x + Math.random(), y + 0.2, z + Math.random(), 0.0D, 0.0D, 0.0D);
		}
	}
}
