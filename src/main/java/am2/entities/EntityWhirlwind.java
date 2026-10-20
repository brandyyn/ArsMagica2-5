package am2.entities;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import am2.api.math.AMVector3;
import am2.damage.DamageSources;
import am2.navigation.PathNavigator;
import am2.network.AMNetHandler;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.HashMap;

public class EntityWhirlwind extends EntityFlying{

	private final Reference2IntOpenHashMap<EntityPlayer> cooldownList;
	private AMVector3 currentTarget;
	private final PathNavigator nav;

	public EntityWhirlwind(World par1World){
		super(par1World);
		cooldownList = new Reference2IntOpenHashMap<EntityPlayer>();
		nav = new PathNavigator(this);
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player){
		if (!worldObj.isRemote){
			if (cooldownList.getInt(player) <= 0){
				if (!worldObj.isRemote && rand.nextInt(100) < 10){
					int slot = player.inventory.mainInventory.length + rand.nextInt(4);
					if (player.inventory.getStackInSlot(slot) != null){
						ItemStack armorStack = player.inventory.getStackInSlot(slot).copy();
						if (!player.inventory.addItemStackToInventory(armorStack)){
							EntityItem item = new EntityItem(worldObj);
							item.setPosition(player.posX, player.posY, player.posZ);
							item.setVelocity(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2 - 0.1);
							worldObj.spawnEntityInWorld(item);
						}
						player.inventory.setInventorySlotContents(slot, null);
					}
				}
				player.attackEntityFrom(DamageSources.causeEntityWindDamage(this), 2);
				float velX = worldObj.rand.nextFloat() * 0.2f;
				float veZ = worldObj.rand.nextFloat() * 0.2f;
				player.addVelocity(velX, 0.8, veZ);
				AMNetHandler.INSTANCE.sendVelocityAddPacket(worldObj, player, velX, 0.8, veZ);
				player.fallDistance = 0;
				setCooldownFor(player);
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		return false;
	}

	@Override
	public void onUpdate(){
		if (currentTarget == null || new AMVector3(this).distanceSqTo(currentTarget) < 2)
			generateNewTarget();

		nav.tryMoveFlying(worldObj, this);

		if (this.ticksExisted > 140 && !this.worldObj.isRemote)
			this.setDead();


		tickCooldowns();

		super.onUpdate();
	}

	private void generateNewTarget(){
		EntityPlayer closest = null;
		for (Object player : this.worldObj.playerEntities){
			if (closest == null || ((EntityPlayer)player).getDistanceSqToEntity(this) < this.getDistanceSqToEntity(closest)){
				closest = (EntityPlayer)player;
			}
		}
		if (closest != null && this.getDistanceSqToEntity(closest) < 64D)
			currentTarget = new AMVector3(closest);
		else
			currentTarget = new AMVector3(this).add(new AMVector3(worldObj.rand.nextInt(10) - 5, 0, worldObj.rand.nextInt(10) - 5));

		nav.SetWaypoint(worldObj, (int)currentTarget.x, (int)currentTarget.y, (int)currentTarget.z, this);
	}

	private void setCooldownFor(EntityPlayer player){
		cooldownList.put(player, 20);
	}

	private void tickCooldowns(){
		// expired players are dropped so the whirlwind does not keep them referenced
		for (ObjectIterator<Reference2IntMap.Entry<EntityPlayer>> it = Reference2IntMaps.fastIterator(cooldownList); it.hasNext(); ){
			Reference2IntMap.Entry<EntityPlayer> entry = it.next();
			if (entry.getIntValue() <= 1)
				it.remove();
			else
				entry.setValue(entry.getIntValue() - 1);
		}
	}

	@Override
	public ItemStack getHeldItem(){
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack){
	}

	@Override
	public boolean canBePushed(){
		return false;
	}

	@Override
	public ItemStack[] getLastActiveItems(){
		return new ItemStack[0];
	}

}
