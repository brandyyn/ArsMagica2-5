package am2.mixin.early;

import am2.blocks.liquid.BlockLiquidEssence;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cognitive dust floats upwards, and items float up through liquid essence. */
@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends Entity{

	private MixinEntityItem(World world){
		super(world);
	}

	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void am2$floatInEssence(CallbackInfo ci){
		ItemStack stack = ((EntityItem)(Object)this).getEntityItem();
		if (stack == null)
			return;
		if (stack.getItem() == ItemsCommonProxy.itemOre && stack.getItemDamage() == ItemOre.META_COGNITIVEDUST){
			moveEntity(motionX, motionY, motionZ);
			motionY *= 0.0500000011920929D;
			motionY += 0.025D;
		}
		if (worldObj.isMaterialInBB(boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), BlockLiquidEssence.liquidEssenceMaterial)){
			double startY = posY;
			moveEntity(motionX, motionY, motionZ);
			motionX *= 0.500000011920929D;
			motionY *= 0.0500000011920929D;
			motionZ *= 0.500000011920929D;
			motionY += 0.05D;
			if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, motionY + 0.6000000238418579D - posY + startY, motionZ))
				motionY = 0.30000001192092896D;
			fallDistance = 0;
		}
	}
}
