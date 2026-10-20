package am2.utility;

import am2.affinity.AffinityHelper;
import am2.items.ItemSoulspike;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State and logic behind AM2's patches to vanilla classes (am2.mixin). The mixins only forward here, so the behaviour
 * stays readable and the patched methods stay small.
 */
public final class AMHooks{

	private AMHooks(){
	}

	//==================================================================================================================
	// Ethereal form (Soulspike)
	//==================================================================================================================

	public static boolean isPlayerEthereal(EntityPlayer player){
		return player != null && player.inventory != null && ItemSoulspike.bootsHaveEtherealTag(player.inventory.armorInventory[0]);
	}

	public static boolean isEthereal(Entity entity){
		return entity instanceof EntityPlayer && isPlayerEthereal((EntityPlayer)entity);
	}

	/**
	 * Entity queries run constantly (every entity's collision check uses one each tick), so the list is returned as is
	 * unless it actually contains an ethereal player.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static List withoutEtherealPlayers(List entities){
		for (int i = 0; i < entities.size(); i++){
			if (isEthereal((Entity)entities.get(i))){
				ArrayList filtered = new ArrayList(entities.size() - 1);
				for (int j = 0; j < entities.size(); j++){
					Object other = entities.get(j);
					if (!isEthereal((Entity)other))
						filtered.add(other);
				}
				return filtered;
			}
		}
		return entities;
	}

	/** Archmage leggings: water behaves like air for the wearer. */
	public static boolean ignoresWater(Entity entity){
		return AffinityHelper.isNotInWaterActually(entity);
	}

	//==================================================================================================================
	// Redstone Signal spell
	//==================================================================================================================

	/** dimension -> packed position -> remaining power queries; server thread only. */
	private static final Int2ObjectOpenHashMap<Long2IntOpenHashMap> providingRedstone = new Int2ObjectOpenHashMap<Long2IntOpenHashMap>();

	private static long pack(int x, int y, int z){
		return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (y & 0xFFF);
	}

	public static void provideRedstone(World world, int x, int y, int z, int activeQueries){
		Long2IntOpenHashMap positions = providingRedstone.get(world.provider.dimensionId);
		if (positions == null){
			positions = new Long2IntOpenHashMap();
			providingRedstone.put(world.provider.dimensionId, positions);
		}
		positions.put(pack(x, y, z), activeQueries);
	}

	/**
	 * Called for every isBlockIndirectlyGettingPowered query. A position powered by the Redstone Signal spell answers
	 * true for its remaining number of queries and notifies the block each time, as before.
	 */
	public static boolean consumeProvidedRedstone(World world, int x, int y, int z){
		if (providingRedstone.isEmpty())
			return false;
		Long2IntOpenHashMap positions = providingRedstone.get(world.provider.dimensionId);
		if (positions == null)
			return false;
		long key = pack(x, y, z);
		int remaining = positions.get(key);
		if (remaining <= 0)
			return false;
		if (remaining == 1){
			positions.remove(key);
			if (positions.isEmpty())
				providingRedstone.remove(world.provider.dimensionId);
		}else{
			positions.put(key, remaining - 1);
		}
		world.getBlock(x, y, z).onNeighborBlockChange(world, x, y, z, Blocks.stonebrick);
		return true;
	}

	//==================================================================================================================
	// Box of Illusions player models (client)
	//==================================================================================================================

	/** Player name -> model name, synced from the server. */
	public static final Map<String, String> playerModelMap = new HashMap<String, String>();

	public static boolean hasMaidModel(Entity entity){
		if (!(entity instanceof EntityPlayer) || playerModelMap.isEmpty())
			return false;
		String model = playerModelMap.get(((EntityPlayer)entity).getCommandSenderName());
		return model != null && model.startsWith("maid");
	}
}
