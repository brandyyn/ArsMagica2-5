package am2.mixin.early;

import am2.utility.AMHooks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Player-only behaviour, added as overrides on EntityPlayer so every other entity runs the vanilla methods untouched:
 * ethereal players (Soulspike) pass through entities, water and ladders, and archmage leggings make water act like air.
 */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase{

	private MixinEntityPlayer(World world){
		super(world);
	}

	@Override
	public boolean handleWaterMovement(){
		return !AMHooks.isPlayerEthereal((EntityPlayer)(Object)this) && super.handleWaterMovement();
	}

	@Override
	public boolean isInWater(){
		return super.isInWater() && !AMHooks.ignoresWater(this);
	}

	@Override
	public boolean isInsideOfMaterial(Material material){
		return super.isInsideOfMaterial(material) && !(material.isLiquid() && AMHooks.ignoresWater(this));
	}

	@Override
	public boolean canBePushed(){
		return super.canBePushed() && !AMHooks.isPlayerEthereal((EntityPlayer)(Object)this);
	}

	@Override
	public boolean canBeCollidedWith(){
		return super.canBeCollidedWith() && !AMHooks.isPlayerEthereal((EntityPlayer)(Object)this);
	}

	@Override
	public boolean isOnLadder(){
		return !AMHooks.isPlayerEthereal((EntityPlayer)(Object)this) && super.isOnLadder();
	}

	@Override
	public void applyEntityCollision(Entity other){
		if (!AMHooks.isPlayerEthereal((EntityPlayer)(Object)this) && !AMHooks.isEthereal(other))
			super.applyEntityCollision(other);
	}
}
