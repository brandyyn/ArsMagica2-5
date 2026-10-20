package am2;

import am2.utility.IExtendedPropertiesHolder;import am2.affinity.AffinityHelper;
import am2.api.ArsMagicaApi;
import am2.api.events.ManaCostEvent;
import am2.api.items.BoundItemHandler;
import am2.api.items.IBoundItem;
import am2.api.items.ManaItemHandler;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.BuffPowerLevel;
import am2.api.spell.enums.SpellModifiers;
import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.tileentities.TileEntityAstralBarrier;
import am2.blocks.tileentities.TileEntityInfusedStem;
import am2.bosses.BossSpawnHelper;
import am2.buffs.BuffEffectScrambleSynapses;
import am2.buffs.BuffEffectTemporalAnchor;
import am2.buffs.BuffList;
import am2.buffs.BuffStatModifiers;
import am2.damage.DamageSourceFire;
import am2.damage.DamageSources;
import am2.enchantments.AMEnchantments;
import am2.entities.EntityFlicker;
import am2.entities.EntityHallucination;
import am2.entities.EntitySpecificHallucinations;
import am2.items.ItemOre;
import am2.items.ItemSoulspike;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.RiftStorage;
import am2.playerextensions.SkillData;
import am2.power.PowerNodeRegistry;
import am2.spell.SkillManager;
import am2.spell.SkillTreeManager;
import am2.spell.SpellUtils;
import am2.utility.*;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.brewing.PotionBrewedEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
@SuppressWarnings("unused")
public class AMEventHandler{


	@SubscribeEvent
	public void onclone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event){
		EntityPlayer original = event.original;
		EntityPlayer newplayer = event.entityPlayer;
		//ExtendedProp.
		NBTTagCompound OldNBT = new NBTTagCompound();
		ExtendedProperties.For(original).saveNBTData(OldNBT);
		ExtendedProperties.For(newplayer).loadNBTData(OldNBT);

		//skill data.
		NBTTagCompound OldSkill = new NBTTagCompound();
		SkillData.For(original).saveNBTData(OldSkill);
		SkillData.For(newplayer).loadNBTData(OldSkill);

		//Rift storage.
		NBTTagCompound OldRift = new NBTTagCompound();
		RiftStorage.For(original).saveNBTData(OldRift);
		RiftStorage.For(newplayer).loadNBTData(OldRift);
		//Affinity data.
		NBTTagCompound OldAffinity = new NBTTagCompound();
		AffinityData.For(original).saveNBTData(OldAffinity);
		AffinityData.For(newplayer).loadNBTData(OldAffinity);

	}
	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void onPotionBrewed(PotionBrewedEvent brewEvent){
		for (ItemStack stack : brewEvent.brewingStacks){
			if (stack == null) continue;
			if (stack.getItem() instanceof ItemPotion){
				ItemPotion ptn = ((ItemPotion)stack.getItem());
				List<PotionEffect> fx = ptn.getEffects(stack.getItemDamage());
				if (fx == null) return;
				for (PotionEffect pe : fx){
					if (pe.getPotionID() == BuffList.greaterManaPotion.id){
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.greaterManaPotion);
						break;
					}else if (pe.getPotionID() == BuffList.epicManaPotion.id){
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.epicManaPotion);
						break;
					}else if (pe.getPotionID() == BuffList.legendaryManaPotion.id){
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.legendaryManaPotion);
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEndermanTeleport(EnderTeleportEvent event){
		EntityLivingBase ent = event.entityLiving;


		ArrayList<Long> keystoneKeys = KeystoneUtilities.instance.GetKeysInInvenory(ent);
		TileEntityAstralBarrier blockingBarrier = DimensionUtilities.GetBlockingAstralBarrier(event.entityLiving.worldObj, (int)event.targetX, (int)event.targetY, (int)event.targetZ, keystoneKeys);

		if (ent.isPotionActive(BuffList.astralDistortion.id) || blockingBarrier != null){
			event.setCanceled(true);
			if (blockingBarrier != null){
				blockingBarrier.onEntityBlocked(ent);
			}
			return;
		}

		if (!ent.worldObj.isRemote && ent instanceof EntityEnderman && ent.worldObj.rand.nextDouble() < 0.01f){
			EntityFlicker flicker = new EntityFlicker(ent.worldObj);
			flicker.setPosition(ent.posX, ent.posY, ent.posZ);
			flicker.setFlickerType(Affinity.ENDER);
			ent.worldObj.spawnEntityInWorld(flicker);
		}
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event){

		if (event.entity instanceof EntityLivingBase){
			EntityLivingBase entityLB = (EntityLivingBase) event.entity;
			ExtendedProperties properties = new ExtendedProperties();
			entityLB.registerExtendedProperties(ExtendedProperties.identifier, properties);
			((IExtendedPropertiesHolder)entityLB).am2$setExtendedProperties(properties);
			entityLB.getAttributeMap().registerAttribute(ArsMagicaApi.maxManaBonus);
			entityLB.getAttributeMap().registerAttribute(ArsMagicaApi.maxBurnoutBonus);
			entityLB.getAttributeMap().registerAttribute(ArsMagicaApi.xpGainModifier);
			entityLB.getAttributeMap().registerAttribute(ArsMagicaApi.burnoutReductionRate);
			entityLB.getAttributeMap().registerAttribute(ArsMagicaApi.manaRegenTimeModifier);
			ExtendedProperties.For(entityLB).register(event.entity.worldObj, entityLB);
			if (event.entity instanceof EntityPlayer){
				event.entity.registerExtendedProperties(RiftStorage.identifier, new RiftStorage());
				event.entity.registerExtendedProperties(AffinityData.identifier, new AffinityData());
				event.entity.registerExtendedProperties(SkillData.identifier, new SkillData((EntityPlayer)event.entity));
			}
		}else if (event.entity instanceof EntityItemFrame && !event.entity.worldObj.isRemote){
			AMCore.proxy.itemFrameWatcher.startWatchingFrame((EntityItemFrame)event.entity);
		}
	}
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeathChrono(LivingDeathEvent event){
		EntityLivingBase soonToBeDead = event.entityLiving;
		if (soonToBeDead.isPotionActive(BuffList.temporalAnchor.id)){
			event.setCanceled(true);
			PotionEffect pe = soonToBeDead.getActivePotionEffect(BuffList.temporalAnchor);
			if (pe instanceof BuffEffectTemporalAnchor){
				BuffEffectTemporalAnchor buff = (BuffEffectTemporalAnchor)pe;
				buff.stopEffect(soonToBeDead);
			}
			soonToBeDead.removePotionEffect(BuffList.temporalAnchor.id);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDrops(PlayerDropsEvent event){
			EntityPlayer player = event.entityPlayer;
			if (player.isPotionActive(BuffList.psychedelic)){
				if (player.worldObj.provider.dimensionId == 1 && player.getActivePotionEffect(BuffList.psychedelic).getAmplifier() == 1) {
					int slotCount = 0;
					int rareCount = 0;
					ListIterator<EntityItem> iter =event.drops.listIterator();
					while(iter.hasNext()){
						EntityItem Eitem = iter.next();
						ItemStack Istack = Eitem.getEntityItem();
						if(Istack.getRarity() != EnumRarity.common && slotCount < 6){
							rareCount++;
							slotCount++;
							iter.remove();
						}
					}
					if (rareCount >= 5) {
						EntityItem fragment = new EntityItem(player.worldObj);
						ItemStack stack = new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_SOULFRAGMENT);
						fragment.setPosition(player.posX+rand.nextInt(5)-2, player.posY + 10, player.posZ+rand.nextInt(5)-2);
						fragment.setEntityItemStack(stack);
						player.worldObj.spawnEntityInWorld(fragment);
						player.worldObj.playSoundAtEntity(player, "ambient.weather.thunder",2F, 2F);
					}
				}
			}

	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		if (event.entityPlayer.getHeldItem() != null) {
			if (event.entityPlayer.getHeldItem().getItem() instanceof ItemSoulspike) {
				if (event.target instanceof EntityHallucination) {
					ManaItemHandler.AddMana(event.entityPlayer.getHeldItem(), 15);
					event.target.attackEntityFrom(DamageSource.outOfWorld, 15);
					event.target.attackEntityFrom(DamageSource.magic, 15);
				} else if (event.target instanceof EntityLivingBase) {
					if (event.target instanceof EntityPlayer) {
						EntityPlayer pl = (EntityPlayer) event.target;
						if (!AMCore.config.getAllowCreativeTargets() && pl.capabilities.isCreativeMode) return;
						ExtendedProperties properties = ExtendedProperties.For(pl);
						if (properties.hasExtraVariable("ethereal")) {
							ManaItemHandler.AddMana(event.entityPlayer.getHeldItem(), (int)(properties.getCurrentMana()/4));
							properties.deductMana(properties.getCurrentMana()/4);
						}
					}
					ManaItemHandler.AddMana(event.entityPlayer.getHeldItem(), 3);
					event.target.attackEntityFrom(DamageSource.outOfWorld, 3);

				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityDeath(LivingDeathEvent event){
		EntityLivingBase soonToBeDead = event.entityLiving;
		if (!(ExtendedProperties.For(soonToBeDead).getContingencyEffect(1).getItem() instanceof ItemSnowball)){
			ExtendedProperties.For(soonToBeDead).procContingency(1, null);
		}

		if (soonToBeDead instanceof EntityHallucination && event.source.getSourceOfDamage() != null) {
			if (event.source.getSourceOfDamage() instanceof EntityPlayer) { // bad karma with the world
				ExtendedProperties exProps = ExtendedProperties.For((EntityPlayer)event.source.getSourceOfDamage());
				exProps.addToExtraVariables("karma", "bad");
			}
		}
		else if (soonToBeDead instanceof EntityCreature){
			if (!EntityUtilities.isSummon(soonToBeDead) && EntityUtilities.isAIEnabled((EntityCreature)soonToBeDead) && event.source.getSourceOfDamage() instanceof EntityPlayer){
				EntityUtilities.handleCrystalPhialAdd((EntityCreature)soonToBeDead, (EntityPlayer)event.source.getSourceOfDamage());
			}
		}

		if (EntityUtilities.isSummon(soonToBeDead)){
			int ownerID = EntityUtilities.getOwner(soonToBeDead);
			Entity e = soonToBeDead.worldObj.getEntityByID(ownerID);
			if (e != null & e instanceof EntityLivingBase){
				ExtendedProperties.For((EntityLivingBase)e).removeSummon();
			}
		}

		if (soonToBeDead instanceof EntityVillager){
			BossSpawnHelper.instance.onVillagerChildKilled((EntityVillager)soonToBeDead);
		}

		if (soonToBeDead instanceof EntityLiving && !soonToBeDead.worldObj.isRemote) {
			int x = (int)Math.floor(soonToBeDead.posX);
			int y = (int)Math.floor(soonToBeDead.posY);
			int z = (int)Math.floor(soonToBeDead.posZ);
			for (int newx = x-3; newx <= x+3; newx++) {
				for (int newy = y-3; newy <= y+3; newy++) {
					for (int newz = z-3; newz <= z+3; newz++) {
						if (soonToBeDead.worldObj.getTileEntity(newx, newy, newz) instanceof TileEntityInfusedStem) {
							((TileEntityInfusedStem)soonToBeDead.worldObj.getTileEntity(newx, newy, newz)).killedEntities.add((EntityLiving)soonToBeDead);
							return;
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event){
		if (EntityUtilities.isSummon(event.entityLiving) && !(event.entityLiving instanceof EntityHorse)){
			event.setCanceled(true);
		}
		if (event.source == DamageSources.darkNexus){
			event.setCanceled(true);
		}
		if (!event.entityLiving.worldObj.isRemote && event.entityLiving instanceof EntityPig && event.entityLiving.getRNG().nextDouble() < 0.3f){
			EntityItem animalFat = new EntityItem(event.entityLiving.worldObj);
			ItemStack stack = new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_ANIMALFAT);
			animalFat.setPosition(event.entity.posX, event.entity.posY, event.entity.posZ);
			animalFat.setEntityItemStack(stack);
			event.drops.add(animalFat);
		}
	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event){
		if (event.entityLiving.isPotionActive(BuffList.agility.id)){
			event.entityLiving.motionY *= 1.5f;
		}
		if (event.entityLiving.isPotionActive(BuffList.leap.id)){

			Entity velocityTarget = event.entityLiving;

			if (event.entityLiving.ridingEntity != null){
				if (event.entityLiving.ridingEntity instanceof EntityMinecart){
					event.entityLiving.ridingEntity.setPosition(event.entityLiving.ridingEntity.posX, event.entityLiving.ridingEntity.posY + 1.5, event.entityLiving.ridingEntity.posZ);
				}
				velocityTarget = event.entityLiving.ridingEntity;
			}

			double yVelocity = 0;
			double xVelocity = 0;
			double zVelocity = 0;

			Vec3 vec = event.entityLiving.getLookVec().normalize();
			switch (event.entityLiving.getActivePotionEffect(BuffList.leap).getAmplifier()){
			case BuffPowerLevel.Low:
				yVelocity = 0.4;
				xVelocity = velocityTarget.motionX * 1.08 * Math.abs(vec.xCoord);
				zVelocity = velocityTarget.motionZ * 1.08 * Math.abs(vec.zCoord);
				break;
			case BuffPowerLevel.Medium:
				yVelocity = 0.7;
				xVelocity = velocityTarget.motionX * 1.25 * Math.abs(vec.xCoord);
				zVelocity = velocityTarget.motionZ * 1.25 * Math.abs(vec.zCoord);
				break;
			case BuffPowerLevel.High:
				yVelocity = 1;
				xVelocity = velocityTarget.motionX * 1.75 * Math.abs(vec.xCoord);
				zVelocity = velocityTarget.motionZ * 1.75 * Math.abs(vec.zCoord);
				break;
			default:
				break;
			}

			float maxHorizontalVelocity = 1.45f;

			if (event.entityLiving.ridingEntity != null && (event.entityLiving.ridingEntity instanceof EntityMinecart || event.entityLiving.ridingEntity instanceof EntityBoat) || event.entityLiving.isPotionActive(BuffList.haste.id)){
				maxHorizontalVelocity += 25;
				xVelocity *= 2.5;
				zVelocity *= 2.5;
			}

			if (xVelocity > maxHorizontalVelocity){
				xVelocity = maxHorizontalVelocity;
			}else if (xVelocity < -maxHorizontalVelocity){
				xVelocity = -maxHorizontalVelocity;
			}

			if (zVelocity > maxHorizontalVelocity){
				zVelocity = maxHorizontalVelocity;
			}else if (zVelocity < -maxHorizontalVelocity){
				zVelocity = -maxHorizontalVelocity;
			}

			if (ExtendedProperties.For(event.entityLiving).getIsFlipped()){
				yVelocity *= -1;
			}

			velocityTarget.addVelocity(xVelocity, yVelocity, zVelocity);
		}
		if (event.entityLiving.isPotionActive(BuffList.entangled.id)){
			event.entityLiving.motionY = 0;
		}

		if (event.entityLiving instanceof EntityPlayer){
			ItemStack boots = ((EntityPlayer)event.entityLiving).inventory.armorInventory[0];
			if (boots != null && boots.getItem() == ItemsCommonProxy.enderBoots && event.entityLiving.isSneaking()){
				ExtendedProperties.For(event.entityLiving).toggleFlipped();
			}

			if (ExtendedProperties.For(event.entityLiving).getFlipRotation() > 0)
				event.entityLiving.addVelocity(0, -2 * event.entityLiving.motionY, 0);
		}
	}

	@SubscribeEvent
	public void onEntityFall(LivingFallEvent event){

		EntityLivingBase ent = event.entityLiving;
		float f = event.distance;
		ent.isAirBorne = false;

		//slowfall buff
		if (ent.isPotionActive(BuffList.slowfall.id) || ent.isPotionActive(BuffList.shrink.id) || (ent instanceof EntityPlayer && AffinityData.For(ent).getAffinityDepth(Affinity.NATURE) == 1.0f)){
			event.setCanceled(true);
			ent.fallDistance = 0;
			return;
		}

		if (ent instanceof EntityPlayer && ((EntityPlayer)ent).inventory.armorInventory[0] != null){
			if (((EntityPlayer)ent).inventory.armorInventory[0].getItem() == ItemsCommonProxy.archmageBoots){
				event.setCanceled(true);
				return;
			}
		}

		//gravity well
		if (ent.isPotionActive(BuffList.gravityWell.id)){
			ent.fallDistance *= 1.5f;
		}

		//fall protection stat
		f -= ExtendedProperties.For(ent).getFallProtection();
		ExtendedProperties.For(ent).setFallProtection(0);
		if (f <= 0){
			ent.fallDistance = 0;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (TimeManipulation.isShielded(event.world, event.x, event.y, event.z)) {
			event.setCanceled(true);
			event.world.playSoundAtEntity(event.getPlayer(), "arsmagica2:spell.cast.arcane", 1F, rand.nextFloat() + 0.5f);
			event.world.spawnParticle("depthsuspend", event.x+0.5, event.y+0.5, event.z+0.5,0,0,0);
			event.world.spawnParticle("depthsuspend", event.x+rand.nextFloat(), event.y+rand.nextFloat(), event.z+rand.nextFloat(),0,0,0);
			event.world.spawnParticle("depthsuspend", event.x+rand.nextFloat(), event.y+rand.nextFloat(), event.z+rand.nextFloat(),0,0,0);
		}
	}

	/** Entity.fall is protected; resolved once (findMethod also makes it accessible). */
	private static Method fallMethod;

	/** A mob with AM2 state (flipped, shrunk, mana linked...) is resynced when a player starts seeing it. */
	@SubscribeEvent
	public void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event){
		if (event.target instanceof EntityLivingBase && !(event.target instanceof EntityPlayer)){
			ExtendedProperties properties = ExtendedProperties.For((EntityLivingBase)event.target);
			if (properties != null && !properties.isDefaultState())
				properties.setFullSync();
		}
	}

	@SubscribeEvent
	public void onEntityLiving(LivingUpdateEvent event){

		EntityLivingBase ent = event.entityLiving;
		World world = ent.worldObj;
		if (!world.isRemote && TimeManipulation.onLivingUpdate(ent)){
			event.setCanceled(true);
			return;
		}

		ExtendedProperties extendedProperties = ExtendedProperties.For(ent);
		extendedProperties.mayHaveBuffModifiers = BuffStatModifiers.instance.applyStatModifiersBasedOnBuffs(ent, extendedProperties.mayHaveBuffModifiers);
		extendedProperties.handleSpecialSyncData();
		extendedProperties.manaBurnoutTick();

		if (ent instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)ent;
			HandlePlayerSpellExtendedProperties(player, extendedProperties);

			ArmorHelper.HandleArmorInfusion(player);
			ArmorHelper.HandleArmorEffects(player);
			HandleArmorInfusionExtendedProperties(player, extendedProperties);


			AffinityData.For(ent).handleExtendedPropertySync();
			SkillData.For((EntityPlayer)ent).handleExtendedPropertySync();

			// hallucination effects
			if (!ent.worldObj.isRemote){
				handlePsychedelics(player);
			}
		}

		// unflip flipped players
		if (extendedProperties.tempFlipUntil != 0 && ent.ticksExisted >= extendedProperties.tempFlipUntil) {
			extendedProperties.tempFlipUntil = 0;
			extendedProperties.toggleFlipped();
		}

		extendedProperties.flipTick();

		if (extendedProperties.getIsFlipped()){
			if ((ent).motionY < 2)
				(ent).motionY += 0.15f;

			double posY = ent.posY + ent.height;
			if (!world.isRemote)
				posY += ent.getEyeHeight();
			if (world.rayTraceBlocks(Vec3.createVectorHelper(ent.posX, posY, ent.posZ), Vec3.createVectorHelper(ent.posX, posY + 1, ent.posZ), true) != null){
				if (!ent.onGround){
					if (ent.fallDistance > 0){
						try{
							if (fallMethod == null)
								fallMethod = ReflectionHelper.findMethod(Entity.class, ent, new String[]{"func_70069_a", "fall"}, float.class);
							fallMethod.invoke(ent, ent.fallDistance);
						}catch (Throwable e){
							e.printStackTrace();
						}
						ent.fallDistance = 0;
					}
				}
				ent.onGround = true;
			}else{
				if (event.entityLiving instanceof EntityPlayer){
					if (ent.motionY > 0){
						if (world.isRemote)
							ent.fallDistance += (float)(ent.posY - ent.prevPosY);
						else
							ent.fallDistance += (float)((((EntityPlayer)ent).field_71095_bQ - ((EntityPlayer)ent).field_71096_bN) * 2);
					}
				}
				ent.onGround = false;
			}
		}

		if (!ent.onGround && ent.fallDistance >= 4f && !(extendedProperties.getContingencyEffect(2).getItem() instanceof ItemSnowball)){
			int distanceToGround = MathUtilities.getDistanceToGround(ent, world);
			if (distanceToGround < -8 * ent.motionY){
				extendedProperties.procContingency(2, null);
			}
		}
		if (!(extendedProperties.getContingencyEffect(3).getItem() instanceof ItemSnowball) && ent.isBurning()){
			extendedProperties.procContingency(3, null);
		}

		if (!ent.worldObj.isRemote && ent.ticksExisted % 200 == 0){
			extendedProperties.setSyncAuras();
		}

		//buff particles
		//if (ent.worldObj.isRemote)
		//	AMCore.instance.proxy.particleManager.spawnBuffParticles(ent);

		//data sync
		extendedProperties.handleExtendedPropertySync();

		HandleSpellEffects(event, extendedProperties);

		if (world.isRemote){
			AMCore.proxy.sendLocalMovementData(ent);
		}
	}

	private void HandleSpellEffects(LivingUpdateEvent event, ExtendedProperties extendedProperties){
		EntityLivingBase ent = event.entityLiving;

		// most entities have no potion effect at all: skip every buff lookup for them
		boolean hasPotions = !ent.getActivePotionEffects().isEmpty();

		if (hasPotions && ent.isPotionActive(BuffList.agility.id)){
			if (ent.stepHeight != 1.01f)
				ent.stepHeight = 1.01f;
		}else if (ent.stepHeight == 1.01f){
			ent.stepHeight = 0.5f;
		}

		if (!ent.worldObj.isRemote && EntityUtilities.isSummon(ent) && !EntityUtilities.isTileSpawnedAndValid(ent)){
			int owner = EntityUtilities.getOwner(ent);
			Entity ownerEnt = ent.worldObj.getEntityByID(owner);
			if (!EntityUtilities.decrementSummonDuration(ent)){
				ent.attackEntityFrom(DamageSources.unsummon, 5000);
			}
			if (owner == -1 || ownerEnt == null || ownerEnt.isDead || ownerEnt.getDistanceSqToEntity(ent) > 900){
				if (ent instanceof EntityLiving && !((EntityLiving)ent).getCustomNameTag().isEmpty()){
					EntityUtilities.setOwner(ent, null);
					EntityUtilities.setSummonDuration(ent, -1);
					EntityUtilities.revertAI((EntityCreature)ent);
				}else{
					ent.attackEntityFrom(DamageSources.unsummon, 5000);
				}
			}
		}

		//leap buff
		if (hasPotions && event.entityLiving.isPotionActive(BuffList.leap)){
			int amplifier = event.entityLiving.getActivePotionEffect(BuffList.leap).getAmplifier();

			switch (amplifier){
			case BuffPowerLevel.Low:
				extendedProperties.setFallProtection(8);
				break;
			case BuffPowerLevel.Medium:
				extendedProperties.setFallProtection(20);
				break;
			case BuffPowerLevel.High:
				extendedProperties.setFallProtection(45);
				break;
			default:
				break;
			}
		}

		if (hasPotions && event.entityLiving.isPotionActive(BuffList.gravityWell)){
			if (event.entityLiving.motionY < 0 && event.entityLiving.motionY > -3f){
				event.entityLiving.motionY *= 1.59999999999999998D;
			}
		}


		//slowfall/shrink buff
		// (isSneaking calls DataWatcher which are slow, so we test it late)
		if ((hasPotions && (event.entityLiving.isPotionActive(BuffList.slowfall) || event.entityLiving.isPotionActive(BuffList.shrink)))
				|| (ent instanceof EntityPlayer && AffinityData.For(ent).getAffinityDepth(Affinity.NATURE) == 1.0f && !ent.isSneaking())){
			if (!event.entityLiving.onGround && event.entityLiving.motionY < 0.0D){
				event.entityLiving.motionY *= 0.79999999999999998D;
			}
		}

		//swift swim
		if (hasPotions && event.entityLiving.isPotionActive(BuffList.swiftSwim)){
			if (event.entityLiving.isInWater()){
				if (!(event.entityLiving instanceof EntityPlayer) || !((EntityPlayer)event.entityLiving).capabilities.isFlying){
					event.entityLiving.motionX *= (1.133f + 0.03 * event.entityLiving.getActivePotionEffect(BuffList.swiftSwim).getAmplifier());
					event.entityLiving.motionZ *= (1.133f + 0.03 * event.entityLiving.getActivePotionEffect(BuffList.swiftSwim).getAmplifier());

					if (event.entityLiving.motionY > 0){
						event.entityLiving.motionY *= 1.134;
					}
				}
			}
		}

		//watery grave
		if (hasPotions && event.entityLiving.isPotionActive(BuffList.wateryGrave)){
			if (event.entityLiving.isInWater()){
				double pullVel = -0.5f;
				pullVel *= (event.entityLiving.getActivePotionEffect(BuffList.wateryGrave).getAmplifier() + 1);
				if (event.entityLiving.motionY > pullVel)
					event.entityLiving.motionY -= 0.1;
			}
		}

		//mana link pfx
		if (ent.worldObj.isRemote)
			extendedProperties.spawnManaLinkParticles();

		if (ent.ticksExisted % 20 == 0)
			extendedProperties.cleanupManaLinks();
	}

	private void HandlePlayerSpellExtendedProperties(EntityPlayer player, ExtendedProperties extendedProperties){
		if (extendedProperties.hasExtraVariable("ethereal")){ // ethereal form handling
			int durationLeft = Integer.parseInt(extendedProperties.getExtraVariable("ethereal"));
			if (durationLeft < 3){
				// deactivate form
				ItemSoulspike.removeTagFromBoots(player.inventory.armorInventory[0]);
				player.capabilities.disableDamage = false;
				player.capabilities.allowEdit = true;
				player.capabilities.isFlying = false;
				player.noClip = false;
				player.setInvisible(false);
				extendedProperties.removeFromExtraVariables("ethereal");
			}else{
				// tick ethereal form
				// decrease duration
				extendedProperties.addToExtraVariables("ethereal", String.valueOf(durationLeft - 1));
			}
		}else{ // isn't ethereal
			if (ItemSoulspike.bootsHaveEtherealTag(player.inventory.armorInventory[0])){ // but boots have tag
				ItemSoulspike.removeTagFromBoots(player.inventory.armorInventory[0]); // remove tag; prevent possible workaround
			}
		}

		TimeManipulation.tickCaster(player, extendedProperties);

		if (player.isPotionActive(BuffList.flight.id) || player.isPotionActive(BuffList.levitation.id) || player.capabilities.isCreativeMode){
			extendedProperties.hadFlight = true;
			if (player.isPotionActive(BuffList.levitation)){
				if (player.capabilities.isFlying){
					float factor = 0.4f;
					player.motionX *= factor;
					player.motionZ *= factor;
					player.motionY *= 0.0001f;
				}
			}
		}else if (extendedProperties.hadFlight){
			player.capabilities.allowFlying = false;
			player.capabilities.isFlying = false;
			extendedProperties.hadFlight = false;
		}
	}

	private void handlePsychedelics(EntityPlayer player){
		if (player.isPotionActive(BuffList.psychedelic)){
			if (player.worldObj.provider.dimensionId == -1) { // any amplifier can cause lower level hallucinations
				if (player.getActivePotionEffect(BuffList.psychedelic).getDuration() > 10) lowerHallucinationTick(player);
				else cleanupLowerHallucination(player);
			} else if (player.worldObj.provider.dimensionId == 1 && player.getActivePotionEffect(BuffList.psychedelic).getAmplifier() == 1) { // only amplified for higher
				if (player.getActivePotionEffect(BuffList.psychedelic).getDuration() > 10) higherHallucinationTick(player);
				else cleanupHigherHallucination(player);
			}
		}
	}

	private void HandleArmorInfusionExtendedProperties(EntityPlayer player, ExtendedProperties extendedProperties){
		if (player.worldObj.isRemote){
			int divisor = extendedProperties.getAuraDelay() > 0 ? extendedProperties.getAuraDelay() : 1;
			if (player.ticksExisted % divisor == 0)
				AMCore.proxy.particleManager.spawnAuraParticles(player);
			AMCore.proxy.setViewSettings();
		}


		if (ArmorHelper.isInfusionPreset(player.getCurrentArmor(1), GenericImbuement.stepAssist)){
			if (player.stepHeight != 1.0111f)
				player.stepHeight = 1.0111f;
		}else if (player.stepHeight == 1.0111f){
			player.stepHeight = 0.5f;
		}

		IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		if (ArmorHelper.isInfusionPreset((player).getCurrentArmor(0), GenericImbuement.runSpeed)){
			if (attr.getModifier(GenericImbuement.imbuedHasteID) == null){
				attr.applyModifier(GenericImbuement.imbuedHaste);
			}
		}else{
			if (attr.getModifier(GenericImbuement.imbuedHasteID) != null){
				attr.removeModifier(GenericImbuement.imbuedHaste);
			}
		}
	}

	// weak: only the lists are ever cleared, so strong keys kept every player object (and its world) forever
	public static Map<EntityPlayer, ArrayList<EntityCreature>> hallucinationMap = new java.util.WeakHashMap<EntityPlayer, ArrayList<EntityCreature>>();
	public static Map<EntityPlayer, ArrayList<EntityItem>> dustMap = new java.util.WeakHashMap<EntityPlayer, ArrayList<EntityItem>>();
	/** Temporary curse creatures and their remaining ticks (server thread). */
	public static final it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap<EntityCreature> tempCurseMap = new it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap<EntityCreature>();

	private void cleanupLowerHallucination(EntityPlayer player) {
		player.removePotionEffect(BuffList.scrambleSynapses.id);
		player.extinguish();
		player.removePotionEffect(Potion.confusion.id);
		if (hallucinationMap.get(player) != null) {
			for (EntityCreature creature : hallucinationMap.get(player)) {
				if (creature != null) creature.setDead();
			}
			hallucinationMap.get(player).clear();
		}
		if (dustMap.get(player) != null) {
			for (EntityItem item : dustMap.get(player)) {
				if (item != null) item.setDead();
			}
			dustMap.get(player).clear();
		}
	}

	private void cleanupHigherHallucination(EntityPlayer player) {
		cleanupLowerHallucination(player);
		player.curePotionEffects(new ItemStack(Items.milk_bucket));
	}

	private void lowerHallucinationTick(EntityPlayer player) {
		if (player.worldObj.rand.nextInt(103) == 0) { // scramble
			player.addPotionEffect(new BuffEffectScrambleSynapses(50, 0));
		}
		if (player.worldObj.rand.nextInt(75) == 0) { // spontaneous combustion
			player.setFire(7);
		}
		if (player.worldObj.rand.nextInt(50) == 0) { // nausea
			player.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 0));
		}

		int x = MathHelper.floor_double(player.posX); // creatures
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);
		if(player.worldObj.rand.nextInt(550) == 0) {
			Class<? extends EntityHallucination> halclass = null;
			switch (player.worldObj.rand.nextInt(5)) {
				case 1:
					halclass = EntitySpecificHallucinations.EntityHallucinationZombie.class;
					break;
				case 2:
					halclass = EntitySpecificHallucinations.EntityHallucinationSpider.class;
					break;
				case 3:
					halclass = EntitySpecificHallucinations.EntityHallucinationWitherSkeleton.class;
					break;
				case 4:
					halclass = EntitySpecificHallucinations.EntityHallucinationMagmacube.class;
					break;
				default:
					halclass = EntitySpecificHallucinations.EntityHallucinationCreeper.class;
					break;
			}

			EntityCreature summoned = summonCreature(player.worldObj, halclass, x, y, z, player, 4, 9);
			hallucinationMap.computeIfAbsent(player, k -> new ArrayList<>());
			hallucinationMap.get(player).add(summoned);
		}
		if(player.worldObj.rand.nextInt(100) == 0) { // dust
			EntityItem item = new EntityItem(player.worldObj);
			item.setPosition((x-15) + player.worldObj.rand.nextInt(16), y + player.worldObj.rand.nextInt(6), (z-15) + player.worldObj.rand.nextInt(16));
			item.setEntityItemStack(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_COGNITIVEDUST));
			player.worldObj.spawnEntityInWorld(item);
			dustMap.computeIfAbsent(player, k -> new ArrayList<EntityItem>());
			dustMap.get(player).add(item);
		}
	}

	private void higherHallucinationTick(EntityPlayer player) {
		lowerHallucinationTick(player); // all the effects of the lower... and more!
		if(player.worldObj.rand.nextInt(75) == 0) { // random effects
			player.addPotionEffect(new PotionEffect(player.worldObj.rand.nextInt(20)+1, 100, 0));
		}
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);
		if(player.worldObj.rand.nextInt(80) == 0) { // random noises
			String soundString = null;
			switch (player.worldObj.rand.nextInt(8)) {
				case 1:
					soundString = "mob.skeleton.say";
					break;
				case 2:
					soundString = "game.tnt.primed";
					break;
				case 3:
					soundString = "random.bow";
					break;
				case 4:
					soundString = "random.chestopen";
					break;
				case 5:
					soundString = "random.chestclosed";
					break;
				case 6:
					soundString = "random.door_open";
					break;
				case 7:
					soundString = "random.fuse";
					break;
				default:
					soundString = "mob.zombie.say";
					break;
			}
			player.worldObj.playSoundAtEntity(player, soundString,1F, 1F);
		}
		if (player.worldObj.rand.nextInt(150) == 0) { // random floating explosion things
			int explosionX = (x-30) + player.worldObj.rand.nextInt(31);
			int explosionY = y + player.worldObj.rand.nextInt(25);
			int explosionZ = (z-30) + player.worldObj.rand.nextInt(31);
			player.worldObj.spawnParticle("largeexplode", explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
			player.worldObj.spawnParticle("hugeexplosion", explosionX, explosionY, explosionZ, 0.0D, 0.0D, 0.0D);
			player.worldObj.playSoundEffect(explosionX, explosionY, explosionZ, "random.explode", 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);
			player.attackEntityFrom(DamageSource.magic, 1 + player.worldObj.rand.nextInt(10));
		}
		if (player.worldObj.rand.nextInt(50) == 0) { // random teleporations and snaps
			int cx = 0, cy = 0, cz = 0;
			if (player.worldObj.rand.nextBoolean()) { // teleport
				cx = rand.nextInt(50);
				cy = rand.nextInt(7);
				cz = rand.nextInt(50);
				if (!player.worldObj.isAirBlock((int)player.posX + cx, (int)player.posY + cy, (int)player.posZ + cz) ||
						!player.worldObj.isAirBlock((int)player.posX + cx, (int)player.posY + cy - 1, (int)player.posZ + cz)) {
					cx = 0;
					cy = 0;
					cz = 0;
				}
			}
			player.setPositionAndRotation(player.posX + cx, player.posY + cy, player.posZ + cz, player.rotationYaw * (rand.nextFloat() + rand.nextFloat()), player.rotationPitch * (rand.nextFloat() + rand.nextFloat()));
		}
		if(player.worldObj.rand.nextInt(750) == 0) { // even more hallucinations + ones specific to end
			Class<? extends EntityHallucination> halclass = null;
			switch (player.worldObj.rand.nextInt(8)) {
				case 1:
					halclass = EntitySpecificHallucinations.EntityHallucinationZombie.class;
					break;
				case 2:
					halclass = EntitySpecificHallucinations.EntityHallucinationSpider.class;
					break;
				case 3:
					halclass = EntitySpecificHallucinations.EntityHallucinationWitherSkeleton.class;
					break;
				case 4:
					halclass = EntitySpecificHallucinations.EntityHallucinationEnderman.class;
					break;
				case 5:
				case 6:
				case 7:
					halclass = EntitySpecificHallucinations.EntityHallucinationEndermite.class;
					break;
				default:
					halclass = EntitySpecificHallucinations.EntityHallucinationCreeper.class;
					break;
			}

			EntityCreature summoned = summonCreature(player.worldObj, halclass, x, y, z, player, 5, 13);
			hallucinationMap.computeIfAbsent(player, k -> new ArrayList<>());
			hallucinationMap.get(player).add(summoned);
		}
	}

	public static EntityCreature summonCreature(World world, Class<? extends EntityHallucination> creatureClass, int x, int y, int z, EntityLivingBase target, int minRange, int maxRange) {
		if(!world.isRemote) {
			int activeRadius = maxRange - minRange;
			int ax = world.rand.nextInt(activeRadius * 2 + 1);
			if(ax > activeRadius) {
				ax += minRange * 2;
			}
			int nx = x - maxRange + ax;
			int az = world.rand.nextInt(activeRadius * 2 + 1);
			if(az > activeRadius) {
				az += minRange * 2;
			}
			int nz = z - maxRange + az;
			int ny;
			for(ny = y; ny < y + 8; ++ny) {
				if(world.isAirBlock(nx, ny, nz)) break;
			}
			while(world.isAirBlock(nx, ny, nz) && ny > 0) {
				--ny;
			}
			int hy;
			for(hy = 0;hy < 6; ++hy) {
				if(!world.isAirBlock(nx, ny + hy + 1, nz)) break;
			}
			if(hy >= 2) {
				try {
					Constructor<? extends EntityHallucination> ex = creatureClass.getConstructor(World.class);
					EntityHallucination creature = ex.newInstance(world);
					if(target instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer)target;
						creature.setTarget(player.getCommandSenderName());
					}

					creature.setLocationAndAngles(0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 0.0F, 0.0F);
					world.spawnEntityInWorld(creature);

					return creature;
				} catch (ReflectiveOperationException | RuntimeException ignored) {

				}
			}
		}
		return null;
	}

	private static final Random rand = new Random();

	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event){
		ItemStack result = attemptFill(event.world, event.target);

		if (result != null){
			event.result = result;
			event.setResult(Result.ALLOW);
		}
	}

	private ItemStack attemptFill(World world, MovingObjectPosition p){
		Block block = world.getBlock(p.blockX, p.blockY, p.blockZ);

		if (block == BlocksCommonProxy.liquidEssence){
			if (world.getBlockMetadata(p.blockX, p.blockY, p.blockZ) == 0) // Check that it is a source block
			{
				world.setBlock(p.blockX, p.blockY, p.blockZ, Blocks.air); // Remove the fluid block

				return new ItemStack(ItemsCommonProxy.itemAMBucket);
			}
		}

		return null;
	}

	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event){
		if (!(event.entityLiving instanceof FakePlayer) && event.target instanceof EntityItemFrame && !event.entity.worldObj.isRemote){
			AMCore.proxy.itemFrameWatcher.startWatchingFrame((EntityItemFrame)event.target);
	}

	}

	@SubscribeEvent
	public void onPlayerTossItem(ItemTossEvent event){
		if (!event.entityItem.worldObj.isRemote)
			EntityItemWatcher.instance.addWatchedItem(event.entityItem);
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event){
		if (event.source.isFireDamage() && event.entityLiving instanceof EntityPlayer && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3] != null){
			if (((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.fireEars || ((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood){
				event.entityLiving.setFire(0);
				event.setCanceled(true);
				return;
			}
		}
		else if (!event.source.isMagicDamage() && event.entityLiving instanceof EntityPlayer && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3] != null && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood) {
			Entity attacker = event.source.getEntity();
			if (attacker != null) {
				attacker.attackEntityFrom(new DamageSourceFire(event.entityLiving), event.entityLiving.worldObj.rand.nextInt(3));
				attacker.setFire(8);
			}
		}

		if (event.entityLiving.isPotionActive(BuffList.manaShield)){
			if (ExtendedProperties.For(event.entityLiving).getCurrentMana() >= event.ammount * 250f){
				ExtendedProperties.For(event.entityLiving).deductMana(event.ammount * 100f);
				ExtendedProperties.For(event.entityLiving).forceSync();
				for (int i = 0; i < Math.min(event.ammount, 5 * AMCore.config.getGFXLevel()); ++i)
					AMCore.proxy.particleManager.BoltFromPointToPoint(event.entityLiving.worldObj,
							event.entityLiving.posX,
							event.entityLiving.posY + event.entityLiving.worldObj.rand.nextFloat() * event.entityLiving.getEyeHeight(),
							event.entityLiving.posZ,
							event.entityLiving.posX - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2,
							event.entityLiving.posY - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2,
							event.entityLiving.posZ - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2, 6, -1);
				event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:spell.cast.arcane", 1.0f, event.entityLiving.worldObj.rand.nextFloat() + 0.5f);
				event.setCanceled(true);
			}
		}
	}

	private double oldKnockbackValue = -1;

	@SubscribeEvent
	public void onEntityHurt(LivingHurtEvent event){

		if (event.source.isFireDamage() && event.entityLiving instanceof EntityPlayer && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3] != null){
			if (((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.fireEars || ((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood){
				event.setCanceled(true);
				return;
			}
		} else if (event.entityLiving instanceof EntityPlayer && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3] != null && ((EntityPlayer)event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood) {
			if (event.source.getEntity() != null) {
				event.source.getEntity().attackEntityFrom(new DamageSourceFire(event.entityLiving), event.entityLiving.worldObj.rand.nextInt(3));
				event.source.getEntity().setFire(8);
			}
		}
		if (event.entityLiving instanceof EntityPlayer && ((EntityPlayer)event.entityLiving).inventory.armorInventory[2] != null){
			if (((EntityPlayer)event.entityLiving).inventory.armorInventory[2].getItem() == ItemsCommonProxy.archmageArmor){
				if (oldKnockbackValue == -1) {
					oldKnockbackValue = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue();
				}
				event.entityLiving.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1);
			} else {
				event.entityLiving.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(oldKnockbackValue);
				oldKnockbackValue = -1;
			}
		} else {
			event.entityLiving.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(oldKnockbackValue);
			oldKnockbackValue = -1;
		}

		PotionEffect magicShield = event.entityLiving.getActivePotionEffect(BuffList.magicShield);
		if (magicShield != null){
			event.ammount *= 0.5f;
			// amplifiers are signed bytes; 255 reads as -1 and divided by zero here
			event.ammount *= (1f / (Math.max(0, magicShield.getAmplifier()) + 1f));
		}

		if (event.entityLiving.isPotionActive(BuffList.manaShield)){
			float manaToTake = Math.min(ExtendedProperties.For(event.entityLiving).getCurrentMana(), event.ammount * 250f);
			event.ammount -= manaToTake / 250f;
			ExtendedProperties.For(event.entityLiving).deductMana(manaToTake);
			ExtendedProperties.For(event.entityLiving).forceSync();
			for (int i = 0; i < Math.min(event.ammount, 5 * AMCore.config.getGFXLevel()); ++i)
				AMCore.proxy.particleManager.BoltFromPointToPoint(event.entityLiving.worldObj,
						event.entityLiving.posX,
						event.entityLiving.posY + event.entityLiving.worldObj.rand.nextFloat() * event.entityLiving.getEyeHeight(),
						event.entityLiving.posZ,
						event.entityLiving.posX - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2,
						event.entityLiving.posY + event.entityLiving.getEyeHeight() - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2,
						event.entityLiving.posZ - 1 + event.entityLiving.worldObj.rand.nextFloat() * 2, 6, -1);
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:spell.cast.arcane", 1.0f, event.entityLiving.worldObj.rand.nextFloat() + 0.5f);
			if (event.ammount <= 0){
				event.setCanceled(true);
				return;
			}
		}

		Entity entityAttacker = event.source.getSourceOfDamage();
		if (entityAttacker instanceof EntityPlayer && ((EntityPlayer)entityAttacker).inventory.armorInventory[2] != null && ((EntityPlayer)entityAttacker).inventory.armorInventory[2].getItem() == ItemsCommonProxy.earthGuardianArmor
				&& ((EntityPlayer)entityAttacker).getCurrentEquippedItem() == null){
			event.ammount += 4;

			double deltaZ = event.entityLiving.posZ - entityAttacker.posZ;
			double deltaX = event.entityLiving.posX - entityAttacker.posX;
			double angle = Math.atan2(deltaZ, deltaX);
			double speed = entityAttacker.isSprinting() ? 3 : 2;
			double vertSpeed = entityAttacker.isSprinting() ? 0.5 : 0.325;

			if (event.entityLiving instanceof EntityPlayer){
				AMNetHandler.INSTANCE.sendVelocityAddPacket(event.entityLiving.worldObj, event.entityLiving, speed * Math.cos(angle), vertSpeed, speed * Math.sin(angle));
			}else{
				event.entityLiving.motionX += (speed * Math.cos(angle));
				event.entityLiving.motionZ += (speed * Math.sin(angle));
				event.entityLiving.motionY += vertSpeed;
			}
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:spell.cast.earth", 0.4f, event.entityLiving.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}
		if (entityAttacker instanceof EntityPlayer
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[2] != null
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[2].getItem() == ItemsCommonProxy.archmageArmor){
			event.ammount += 5;

			double deltaZ = event.entityLiving.posZ - entityAttacker.posZ;
			double deltaX = event.entityLiving.posX - entityAttacker.posX;
			double angle = Math.atan2(deltaZ, deltaX);
			double speed = entityAttacker.isSprinting() ? 5 : 3;
			double vertSpeed = entityAttacker.isSprinting() ? 1 : 0.65;

			if (event.entityLiving instanceof EntityPlayer){
				AMNetHandler.INSTANCE.sendVelocityAddPacket(event.entityLiving.worldObj, event.entityLiving, speed * Math.cos(angle), vertSpeed, speed * Math.sin(angle));
			}else{
				event.entityLiving.motionX += (speed * Math.cos(angle));
				event.entityLiving.motionZ += (speed * Math.sin(angle));
				event.entityLiving.motionY += vertSpeed;
			}
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:spell.cast.earth", 0.4f, event.entityLiving.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}

		ExtendedProperties extendedProperties = ExtendedProperties.For(event.entityLiving);
		EntityLivingBase entityAttacked = event.entityLiving;
		if (!(extendedProperties.getContingencyEffect(0).getItem() instanceof ItemSnowball)){
			extendedProperties.procContingency(0, (event.source.getEntity() != null && event.source.getEntity() instanceof EntityLivingBase) ? (EntityLivingBase)event.source.getEntity() : null);
		}
		if (!(extendedProperties.getContingencyEffect(4).getItem() instanceof ItemSnowball) && entityAttacked.getHealth() <= entityAttacked.getMaxHealth() / 3){
			extendedProperties.procContingency(4, (event.source.getEntity() != null && event.source.getEntity() instanceof EntityLivingBase) ? (EntityLivingBase)event.source.getEntity() : null);
		}

		if (entityAttacker instanceof EntityPlayer
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[3] != null
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[2] != null
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[1] != null
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[0] != null
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[2].getItem() == ItemsCommonProxy.archmageArmor
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[1].getItem() == ItemsCommonProxy.archmageLeggings
				&& ((EntityPlayer)entityAttacker).inventory.armorInventory[0].getItem() == ItemsCommonProxy.archmageBoots){
			if (entityAttacker.isSneaking()) {
				extendedProperties.toggleFlipped();
				extendedProperties.tempFlipUntil = entityAttacked.ticksExisted + 50;
			}
		}

		if (entityAttacked.isPotionActive(BuffList.fury.id))
			event.ammount /= 2;

		if (entityAttacker instanceof EntityLivingBase
				&& ((EntityLivingBase)entityAttacker).isPotionActive(BuffList.shrink))
			event.ammount /= 2;
	}


	@SubscribeEvent
	public void onBlockOverlay(RenderBlockOverlayEvent event) {
		if (event.overlayType == RenderBlockOverlayEvent.OverlayType.WATER) {
			if (AffinityHelper.isNotInWaterActually(event.player)) {
				event.setCanceled(true);
			}
		} else if (event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
			if (event.player != null && event.player.inventory.armorInventory[3] != null && event.player.inventory.armorInventory[3].getItem() == ItemsCommonProxy.archmageHood) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (event.entity instanceof EntityLivingBase && ((EntityLivingBase)event.entity).isPotionActive(BuffList.temporalAnchor.id)){
			((EntityLivingBase)event.entity).removePotionEffect(BuffList.temporalAnchor.id);
		}
	}

	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event){
		EntityPlayer player = event.entityPlayer;
		if (player.isPotionActive(BuffList.fury.id))
			event.newSpeed = event.originalSpeed * 5;
	}

	@SubscribeEvent
	public void onManaCost(ManaCostEvent event){
		if (event.caster.getHeldItem() != null && event.caster.getHeldItem().getItem() == ItemsCommonProxy.arcaneSpellbook){
			event.manaCost *= 0.75f;
			event.burnout *= 0.4f;
		}
	}
	
//handle bound item dig speed modifiers
	@SubscribeEvent
	public void digspeed(BreakSpeed event){
		if(event.entity instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)event.entity;
			if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IBoundItem){
				ItemStack spellstack = BoundItemHandler.getSpellStack(player.getHeldItem());
				if(SpellUtils.instance.modifierIsPresent(SpellModifiers.SPEED, spellstack)){
					event.newSpeed = (float)SpellUtils.instance.getModifiedDouble_Mul(event.originalSpeed,spellstack,player,null,player.worldObj,SpellModifiers.SPEED);
				}
			}
		}
	}
	@SubscribeEvent
	public void onPlayerPickupItem(EntityItemPickupEvent event){
		if (event.entityPlayer == null)
			return;

		if (!event.entityPlayer.worldObj.isRemote && ExtendedProperties.For(event.entityPlayer).getMagicLevel() <= 0 && event.item.getEntityItem().getItem() == ItemsCommonProxy.arcaneCompendium){
			event.entityPlayer.addChatMessage(new ChatComponentText("You have unlocked the secrets of the arcane!"));
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.entityPlayer, "shapes", true);
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.entityPlayer, "components", true);
			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.entityPlayer, "modifiers", true);
			ExtendedProperties.For(event.entityPlayer).setMagicLevelWithMana(1);
			ExtendedProperties.For(event.entityPlayer).forceSync();
			if(AMCore.config.isEasyStart()){
				ISkillTreeEntry touch = SkillManager.instance.getSkill("Touch");
				ISkillTreeEntry self = SkillManager.instance.getSkill("Self");
				ISkillTreeEntry proj = SkillManager.instance.getSkill("Projectile");
				SkillData.For(event.entityPlayer).learn(touch);
				SkillData.For(event.entityPlayer).learn(self);
				SkillData.For(event.entityPlayer).learn(proj);
			}

			return;
		}

		if (event.item.getEntityItem().getItem() == ItemsCommonProxy.spell){
			if (event.entityPlayer.worldObj.isRemote){
				AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.entityPlayer, "spell_book", false);
			}
		}else{
			Item item = event.item.getEntityItem().getItem();
			int meta = event.item.getEntityItem().getItemDamage();

			if (event.entityPlayer.worldObj.isRemote &&
					item.getUnlocalizedName() != null && (
					AMCore.proxy.items.getArsMagicaItems().contains(item)) ||
					(item instanceof ItemBlock && AMCore.proxy.blocks.getArsMagicaBlocks().contains(((ItemBlock)item).field_150939_a))){
				AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.entityPlayer, item.getUnlocalizedName().replace("item.", "").replace("arsmagica2:", "").replace("tile.", "") + ((meta > -1) ? "@" + meta : ""), false);
			}
		}
	}

}

