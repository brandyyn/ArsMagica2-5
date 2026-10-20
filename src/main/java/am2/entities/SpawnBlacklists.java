package am2.entities;

import am2.AMCore;
import am2.LogHelper;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;

public class SpawnBlacklists{
	private static final ArrayListMultimap<Integer, Class<? extends EntityLivingBase>> blacklistedDimensionSpawns = ArrayListMultimap.create();
	private static final ArrayListMultimap<Integer, Class<? extends EntityLivingBase>> blacklistedBiomeSpawns = ArrayListMultimap.create();
	private static final ArrayList<Integer> blacklistedWorldgenDimensions = new ArrayList<>();
	private static final ArrayList<Class<? extends EntityAnimal>> progenyBlacklist = new ArrayList<>();
	private static final ArrayList<Class<? extends EntityAnimal>> butcheryBlacklist = new ArrayList<>();

	public static void addBlacklistedDimensionSpawn(String entityClass, Integer dimensionID){
		Class<? extends EntityLivingBase> clazz;
		try{
			clazz = Class.forName(entityClass).asSubclass(EntityLivingBase.class);
			blacklistedDimensionSpawns.put(dimensionID, clazz);
			LogHelper.info("Blacklisted %s from spawning in dimension %d.", entityClass, dimensionID);
		}catch (ClassNotFoundException e){
			LogHelper.info("Unable to parse class name %s from IMC!  This needs to be corrected by the other mod author!", entityClass);
		}
	}

	public static void addBlacklistedBiomeSpawn(String entityClass, Integer biomeID){
		Class<? extends EntityLivingBase> clazz;
		try{
			clazz = Class.forName(entityClass).asSubclass(EntityLivingBase.class);
			blacklistedBiomeSpawns.put(biomeID, clazz);
			LogHelper.info("Blacklisted %s from spawning in biome %d.", entityClass, biomeID);
		}catch (ClassNotFoundException e){
			LogHelper.info("Unable to parse class name %s from IMC!  This needs to be corrected by the other mod author!", entityClass);
		}
	}

	public static boolean entityCanSpawnHere(double x, double z, World world, EntityLivingBase entity){
		if (blacklistedDimensionSpawns.containsEntry(world.provider.dimensionId, entity.getClass()))
			return false;
		BiomeGenBase biome = world.getBiomeGenForCoords((int)x, (int)z);
		if (blacklistedBiomeSpawns.containsEntry(biome.biomeID, entity.getClass()))
			return false;
		return getPermanentBlacklistValue(world, entity);
	}

	public static boolean getPermanentBlacklistValue(World world, EntityLivingBase entity){
		for (int i : AMCore.config.getMobBlacklist()){
			if (i == world.provider.dimensionId){
				return false;
			}
		}
		return true;
	}


	public static void addBlacklistedDimensionForWorldgen(int dimensionID){
		if (dimensionID == 0 || dimensionID == -1)
			return;
		blacklistedWorldgenDimensions.add(dimensionID);
	}

	public static boolean worldgenCanHappenInDimension(int dimensionID){
		return !blacklistedWorldgenDimensions.contains(dimensionID);
	}

	public static void addButcheryBlacklist(Class<? extends EntityAnimal> clazz){
		if (!butcheryBlacklist.contains(clazz))
			butcheryBlacklist.add(clazz);
	}

	public static void addProgenyBlacklist(Class<? extends EntityAnimal> clazz){
		if (!progenyBlacklist.contains(clazz))
			progenyBlacklist.add(clazz);
	}

	public static boolean canButcheryAffect(Class<? extends EntityAnimal> clazz){
		return !butcheryBlacklist.contains(clazz);
	}

	public static boolean canProgenyAffect(Class<? extends EntityAnimal> clazz){
		return !progenyBlacklist.contains(clazz);
	}
}
