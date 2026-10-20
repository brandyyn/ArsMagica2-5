package am2.worldgen;

import am2.AMCore;
import am2.entities.EntityHellCow;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenPlains;

import java.util.ArrayList;

public class SCLBiome extends BiomeGenPlains {
   public static final SCLBiome instance;
   ArrayList list = new ArrayList();

   private SCLBiome(int par1) {
      super(par1);
      this.biomeName = "MooMooFarm";
      this.topBlock = Blocks.grass;
      this.fillerBlock = Blocks.netherrack;
      this.spawnableCreatureList.clear();
      this.spawnableCaveCreatureList.clear();
      this.spawnableMonsterList.clear();
      this.spawnableWaterCreatureList.clear();
      this.spawnableMonsterList.add(new SpawnListEntry(EntityHellCow.class, 10, 4, 12));
   }

   public int getBiomeFoliageColor() {
      return 16729122;
   }

   public int getBiomeGrassColor() {
      return 16729122;
   }

   public int getWaterColorMultiplier() {
      return 16711680;
   }

   public int getSkyColorByTemp(float par1) {
      return 16711680;
   }

   static {
      instance = new SCLBiome(AMCore.config.getMMFBiomeID());
   }
}
