package am2.entities;

import am2.AMCore;
import am2.LogHelper;
import am2.api.entities.IEntityManager;
import am2.bosses.*;
import am2.bosses.renderers.*;
import am2.entities.models.ModelBattleChicken;
import am2.entities.models.ModelHecate;
import am2.entities.renderers.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityManager implements IEntityManager{

	public static final EntityManager instance = new EntityManager();

	private EntityManager(){
	}

	public static String WispMobID = "MobWisp";
	public static String SummonedSkeletonMobID = "SummonedSkeleton";
	public static String SummonedLichMobID = "SummonedLich";
	public static String InsectSwarmMobID = "InsectSwarm";
	public static String SummonedShadowMobID = "SummonedShadow";
	public static String GatewayPortalID = "GatewayPortal";
	public static String ManaElemMobID = "MobManaElemental";
	public static String MageVillagerMobID = "MobMageVillager";
	public static String HecateMobID = "MobHecate";
	public static String ManaCreeperMobID = "MobManaCreeper";
	public static String DryadMobID = "MobDryad";
	public static String LightMageMobID = "MobLightMage";
	public static String DarkMageMobID = "MobDarkMage";
	public static String EarthGolemMobID = "EarthElemental";
	public static String SummonedBattleChickenMobID = "BattleChicken";
	public static String ZoneSpellID = "ZoneSpell";
	public static String WaterElementalMobID = "MobWaterElemental";
	public static String FireElementalMobID = "MobFireElemental";
	public static String ManaVortexID = "ManaVortex";
	public static String SpellProjectileID = "SpellProjectile";
	public static String DarklingID = "MobDarkling";
	public static String RiftStorageID = "RiftStorage";
	public static String WhirlwindID = "Whirlwind";
	public static String ShockwaveID = "Shockwave";
	public static String HellCowID = "HellCow";
	public static String BroomID = "DaBroom";
	public static String AirSledID = "AirSled";
	public static String FlickerID = "Flicker";
	public static String ShadowHelperID = "ShadowHelper";
	public static String NatureGuardianMobID = "BossNatureGuardian";
	public static String ArcaneGuardianMobID = "BossArcaneGuardian";
	public static String EarthGuardianMobID = "BossEarthGuardian";
	public static String WaterGuardianMobID = "BossWaterGuardian";
	public static String WinterGuardianMobID = "BossWinterGuardian";
	public static String AirGuardianMobID = "BossAirGuardian";
	public static String FireGuardianMobID = "BossFireGuardian";
	public static String LifeGuardianMobID = "BossLifeGuardian";
	public static String LightningGuardianMobID = "BossLightningGuardian";
	public static String EnderGuardianMobID = "BossEnderGuardian";

	public static String HAL1ID = "HallucinationMagmacube";
	public static String HAL2ID = "HallucinationWitherSkeleton";
	public static String HAL3ID = "HallucinationSpider";
	public static String HAL4ID = "HallucinationEndermite";
	public static String HAL5ID = "HallucinationEnderman";
	public static String HAL6ID = "HallucinationZombie";
	public static String HAL7ID = "HallucinationCreeper";

	public static String ThrownSickleID = "ThrownSickle";
	public static String ThrownRockID = "ThrownRock";
	public static String ThrownArmID = "ThrownArm";

	private static final SpawnListEntry flickerSpawns = new SpawnListEntry(EntityFlicker.class, 3, 2, 4);

	public void registerEntities(){

		int updateFrequency = 2;
		int updateDistance = 64;
		boolean updateVelocity = true;

		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationCreeper.class, HAL7ID, 126, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationZombie.class, HAL6ID, 125, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationEnderman.class, HAL5ID, 124, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationEndermite.class, HAL4ID, 123, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationSpider.class, HAL3ID, 122, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationWitherSkeleton.class, HAL2ID, 121, AMCore.instance, 75, 3, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpecificHallucinations.EntityHallucinationMagmacube.class, HAL1ID, 120, AMCore.instance, 75, 3, updateVelocity);

		EntityRegistry.registerModEntity(EntityEarthElemental.class, EarthGolemMobID, 119, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityFireElemental.class, FireElementalMobID, 117, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityBattleChicken.class, SummonedBattleChickenMobID, 113, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityDryad.class, DryadMobID, 112, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpellProjectile.class, SpellProjectileID, 111, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityHecate.class, HecateMobID, 110, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityManaElemental.class, ManaElemMobID, 109, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityManaCreeper.class, ManaCreeperMobID, 108, AMCore.instance, updateDistance, updateFrequency, updateVelocity);

		EntityRegistry.registerModEntity(EntityManaVortex.class, ManaVortexID, 107, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityWaterElemental.class, WaterElementalMobID, 106, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityLightMage.class, LightMageMobID, 105, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityDarkMage.class, DarkMageMobID, 104, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntitySpellEffect.class, ZoneSpellID, 101, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityMageVillager.class, MageVillagerMobID, 100, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityDarkling.class, DarklingID, 99, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityRiftStorage.class, RiftStorageID, 98, AMCore.instance, updateDistance, updateFrequency, false);
		EntityRegistry.registerModEntity(EntityNatureGuardian.class, NatureGuardianMobID, 97, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityThrownSickle.class, ThrownSickleID, 96, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityArcaneGuardian.class, ArcaneGuardianMobID, 95, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityEarthGuardian.class, EarthGuardianMobID, 94, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityThrownRock.class, ThrownRockID, 93, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityWaterGuardian.class, WaterGuardianMobID, 92, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityWinterGuardian.class, WinterGuardianMobID, 91, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityWinterGuardianArm.class, ThrownArmID, 90, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityAirGuardian.class, AirGuardianMobID, 89, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityFireGuardian.class, FireGuardianMobID, 88, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityWhirlwind.class, WhirlwindID, 87, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityShockwave.class, ShockwaveID, 86, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityHellCow.class, HellCowID, 85, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityBroom.class, BroomID, 84, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityAirSled.class, AirSledID, 83, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityLifeGuardian.class, LifeGuardianMobID, 82, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityFlicker.class, FlickerID, 81, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityLightningGuardian.class, LightningGuardianMobID, 80, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityEnderGuardian.class, EnderGuardianMobID, 79, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
		EntityRegistry.registerModEntity(EntityShadowHelper.class, ShadowHelperID, 78, AMCore.instance, updateDistance, updateFrequency, updateVelocity);
	}

	@SideOnly(Side.CLIENT)
	public void registerRenderInformation(){
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthElemental.class, new RenderEarthElemental());
		RenderingRegistry.registerEntityRenderingHandler(EntityFireElemental.class, new RenderFireElemental());
		//RenderingRegistry.registerEntityRenderingHandler(EntityWisp.class, new RenderWisp(new ModelWisp(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityBattleChicken.class, new RenderBattleChicken(new ModelBattleChicken(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityHecate.class, new RenderHecate(new ModelHecate(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityManaElemental.class, new RenderManaElemental());
		RenderingRegistry.registerEntityRenderingHandler(EntityManaVortex.class, new RenderManaVortex());
		RenderingRegistry.registerEntityRenderingHandler(EntityWaterElemental.class, new RenderWaterElemental());

		RenderingRegistry.registerEntityRenderingHandler(EntityLightMage.class, new RenderMage());
		RenderingRegistry.registerEntityRenderingHandler(EntityDarkMage.class, new RenderMage());
		RenderingRegistry.registerEntityRenderingHandler(EntityMageVillager.class, new RenderMageWizard());
		RenderingRegistry.registerEntityRenderingHandler(EntityRiftStorage.class, new RenderRiftStorage());
		RenderingRegistry.registerEntityRenderingHandler(EntityManaCreeper.class, new RenderManaCreeper());
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new RenderSpellProjectile());
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellEffect.class, new RenderHidden());
		RenderingRegistry.registerEntityRenderingHandler(EntityDryad.class, new RenderDryad());

		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationCreeper.class, new RenderHallucination(new ModelCreeper(), new ResourceLocation("textures/entity/creeper/creeper.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationMagmacube.class, new RenderHallucination(new ModelMagmaCube(), new ResourceLocation("textures/entity/slime/magmacube.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationWitherSkeleton.class, new RenderHallucination(new ModelSkeleton(), new ResourceLocation("textures/entity/skeleton/wither_skeleton.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationSpider.class, new RenderHallucination(new ModelSpider(), new ResourceLocation("textures/entity/spider/spider.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationEndermite.class, new RenderHallucination(new ModelSilverfish(), new ResourceLocation("arsmagica2", "textures/mobs/enderfish.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationEnderman.class, new RenderHallucination(new ModelEnderman(), new ResourceLocation("textures/entity/enderman/enderman.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecificHallucinations.EntityHallucinationZombie.class, new RenderHallucination(new ModelZombie(), new ResourceLocation("textures/entity/zombie/zombie.png")));

		RenderingRegistry.registerEntityRenderingHandler(EntityNatureGuardian.class, new RenderPlantGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityArcaneGuardian.class, new RenderArcaneGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthGuardian.class, new RenderEarthGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityWaterGuardian.class, new RenderWaterGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardian.class, new RenderIceGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityAirGuardian.class, new RenderAirGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityFireGuardian.class, new RenderFireGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningGuardian.class, new RenderLightningGuardian());

		RenderingRegistry.registerEntityRenderingHandler(EntityThrownSickle.class, new RenderThrownSickle());
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownRock.class, new RenderThrownRock());
		RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardianArm.class, new RenderWinterGuardianArm());

		RenderingRegistry.registerEntityRenderingHandler(EntityWhirlwind.class, new RenderWhirlwind());
		RenderingRegistry.registerEntityRenderingHandler(EntityShockwave.class, new RenderHidden());

		RenderingRegistry.registerEntityRenderingHandler(EntityHellCow.class, new RenderHellCow());
		RenderingRegistry.registerEntityRenderingHandler(EntityDarkling.class, new RenderDarkling());
		RenderingRegistry.registerEntityRenderingHandler(EntityBroom.class, new RenderBroom());
		RenderingRegistry.registerEntityRenderingHandler(EntityAirSled.class, new RenderAirSled());

		RenderingRegistry.registerEntityRenderingHandler(EntityLifeGuardian.class, new RenderLifeGuardian());
		RenderingRegistry.registerEntityRenderingHandler(EntityFlicker.class, new RenderFlicker());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderGuardian.class, new RenderEnderGuardian());

		RenderingRegistry.registerEntityRenderingHandler(EntityShadowHelper.class, new RenderShadowHelper());
	}

	public void initializeSpawns(){
		BiomeDictionary.registerAllBiomes();
		EntityRegistry.addSpawn(EntityDryad.class,AMCore.config.GetDryadSpawnRate(),1,2,EnumCreatureType.creature, getBiomes(EntityDryad.class));
		EntityRegistry.addSpawn(EntityHecate.class,AMCore.config.GetHecateSpawnRate(),1, 1,EnumCreatureType.monster, getBiomes(EntityHecate.class));
		EntityRegistry.addSpawn(EntityManaCreeper.class, AMCore.config.GetManaCreeperSpawnRate(), 1, 1,EnumCreatureType.monster, getBiomes(EntityManaCreeper.class));
		EntityRegistry.addSpawn(EntityLightMage.class, AMCore.config.GetMageSpawnRate(), 1, 3,EnumCreatureType.creature, getBiomes(EntityLightMage.class));
		EntityRegistry.addSpawn(EntityDarkMage.class, AMCore.config.GetMageSpawnRate(), 1, 3,EnumCreatureType.creature, getBiomes(EntityDarkMage.class));
		EntityRegistry.addSpawn(EntityDarkling.class, AMCore.config.GetDarklingSpawnRate(), 4, 8,EnumCreatureType.monster, getBiomes(EntityDarkling.class));
		EntityRegistry.addSpawn(EntityWaterElemental.class, AMCore.config.GetWaterElementalSpawnRate(), 1, 3,EnumCreatureType.waterCreature, getBiomes(EntityWaterElemental.class));
		EntityRegistry.addSpawn(EntityEarthElemental.class, AMCore.config.GetEarthElementalSpawnRate(), 1, 2,EnumCreatureType.monster, getBiomes(EntityEarthElemental.class));
		EntityRegistry.addSpawn(EntityFireElemental.class, AMCore.config.GetFireElementalSpawnRate(), 1, 1,EnumCreatureType.monster, getBiomes(EntityFireElemental.class));
		EntityRegistry.addSpawn(EntityManaElemental.class, AMCore.config.GetManaElementalSpawnRate(), 1, 1,EnumCreatureType.monster, getBiomes(EntityManaElemental.class));

	}
	public static BiomeGenBase[] getBiomes(Class<? extends Entity> entity){
		List<BiomeGenBase> array = new ArrayList<>();
		if(entity == EntityDryad.class){
			Type[] types = {Type.FOREST, Type.MAGICAL, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}else if(entity == EntityWaterElemental.class){
			Type[] types = {Type.OCEAN,Type.RIVER};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}else if(entity == EntityEarthElemental.class){
			Type[] types = {Type.HILLS, Type.MOUNTAIN,Type.BEACH, Type.SANDY};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}else if(entity == EntityManaElemental.class){
			Type[] types = { Type.SNOWY, Type.HILLS, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}

		else if(entity == EntityDarkMage.class ||  entity == EntityLightMage.class || entity == EntityManaCreeper.class){
			Type[] types = {Type.BEACH, Type.SANDY, Type.FOREST, Type.SNOWY, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}
		else if(entity == EntityHecate.class || entity == EntityDarkling.class || entity == EntityFireElemental.class){
			Type[] types = {Type.NETHER};
			for(Type type : types){
				array.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(type)));
			}
		}
		return array.toArray(new BiomeGenBase[]{});
	}
}
