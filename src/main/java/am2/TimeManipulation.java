package am2;

import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeRegistry;
import cofh.api.energy.IEnergyHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side state of the time and protection spells (Concentrate Time, Dilute Time, Fortify Time, Shield on blocks)
 * and the spatial vortex transfer.
 * <p>
 * The spells persist their effects as extra variables on the caster, which stay the source of truth for saving. Every
 * {@link #CADENCE} ticks each caster that has such variables counts them down and refreshes the indexes here; the
 * per-tile and per-entity hooks then only do a primitive-keyed lookup, and nothing is scanned while no effect is active.
 * Index entries that are not refreshed (caster offline or dead) lapse after {@link #LAPSE} ticks.
 */
public final class TimeManipulation{

	private static final String ACCELERATED_TILE = "accelerated_fast_tile_";
	private static final String ACCELERATED_ENTITY = "accelerated_fast_entity_";
	private static final String SLOWED_TILE = "accelerated_slow_tile_";
	private static final String SLOWED_ENTITY = "accelerated_slow_entity_";
	private static final String FORTIFIED_TILE = "timefortified_tile_";
	private static final String SHIELDED_TILE = "shielded_tile_";
	private static final String SPATIAL_VORTEX = "spatialvortex_";

	/** Ticks between a caster's countdown steps; durations are counted down by this amount per step. */
	private static final int CADENCE = 5;
	private static final int LAPSE = CADENCE * 2 + 1;

	static boolean accelerationEnabled = true;
	static boolean slowEnabled = true;
	static boolean fortifyEnabled = true;
	static boolean shieldEnabled = true;
	static boolean vortexEnabled = true;

	private static final Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> acceleratedTiles = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>>();
	private static final Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> slowedTiles = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>>();
	private static final Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> shieldedTiles = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>>();
	private static final HashMap<UUID, Effect> acceleratedEntities = new HashMap<UUID, Effect>();
	private static final HashMap<UUID, Effect> slowedEntities = new HashMap<UUID, Effect>();

	/** Written on the server thread, read by the tile entity hook on both threads before any map access. */
	private static int slowedTileCount;

	private static final ArrayList<EntityLivingBase> pendingAccelerated = new ArrayList<EntityLivingBase>();
	private static boolean runningAccelerated;

	// caster countdown scratch, server thread only
	private static final ArrayList<String> scratchRemove = new ArrayList<String>();
	private static final ArrayList<String> scratchKeys = new ArrayList<String>();
	private static final ArrayList<String> scratchValues = new ArrayList<String>();
	private static final ArrayList<int[]> scratchVortices = new ArrayList<int[]>();

	private TimeManipulation(){
	}

	private static final class Effect{
		final int x, y, z;
		int power;
		int expires;

		Effect(int x, int y, int z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static void configure(){
		accelerationEnabled = AMCore.skillConfig.isSkillEnabled("ConcentrateTime");
		slowEnabled = AMCore.skillConfig.isSkillEnabled("DiluteTime");
		fortifyEnabled = AMCore.skillConfig.isSkillEnabled("FortifyTime");
		shieldEnabled = AMCore.skillConfig.isSkillEnabled("Shield");
		vortexEnabled = AMCore.config.enableSpatialVortex();
	}

	public static boolean isTimeEffectKey(String key){
		return key.startsWith(ACCELERATED_TILE) || key.startsWith(ACCELERATED_ENTITY) || key.startsWith(SLOWED_TILE) || key.startsWith(SLOWED_ENTITY)
				|| key.startsWith(FORTIFIED_TILE) || key.startsWith(SHIELDED_TILE) || key.startsWith(SPATIAL_VORTEX);
	}

	private static int now(){
		return MinecraftServer.getServer().getTickCounter();
	}

	private static long pack(int x, int y, int z){
		return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (y & 0xFFF);
	}

	//==================================================================================================================
	// caster countdown
	//==================================================================================================================

	/** Called from the caster's living update. */
	public static void tickCaster(EntityPlayer player, ExtendedProperties props){
		if (player.worldObj.isRemote || !props.hasTimeEffectVariables())
			return;
		// staggered per player; the effects are refreshed long before they lapse
		if ((player.ticksExisted + player.getEntityId()) % CADENCE != 0)
			return;

		int expires = now() + LAPSE;
		for (Map.Entry<String, String> entry : props.getExtraVariables().entrySet()){
			String key = entry.getKey();
			if (!isTimeEffectKey(key))
				continue;
			try{
				if (key.startsWith(SPATIAL_VORTEX)){
					String[] v = key.split("_");
					scratchVortices.add(new int[]{Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[4])});
					continue;
				}
				int remaining = Integer.parseInt(entry.getValue());
				if (remaining < 3){
					scratchRemove.add(key);
					onExpired(key);
				}else{
					scratchKeys.add(key);
					scratchValues.add(String.valueOf(remaining - CADENCE));
					refresh(key, expires);
				}
			}catch (RuntimeException e){
				LogHelper.warn("Dropping malformed time effect %s=%s: %s", key, entry.getValue(), e);
				scratchRemove.add(key);
			}
		}

		for (int i = 0; i < scratchRemove.size(); i++)
			props.removeFromExtraVariables(scratchRemove.get(i));
		for (int i = 0; i < scratchKeys.size(); i++)
			props.addToExtraVariables(scratchKeys.get(i), scratchValues.get(i));
		scratchRemove.clear();
		scratchKeys.clear();
		scratchValues.clear();

		if (vortexEnabled && scratchVortices.size() > 1)
			SpatialVortex.transfer(scratchVortices);
		scratchVortices.clear();
	}

	private static void refresh(String key, int expires){
		String[] v = key.split("_");
		if (key.startsWith(ACCELERATED_TILE)){
			if (accelerationEnabled)
				putTile(acceleratedTiles, Integer.parseInt(v[6]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), Integer.parseInt(v[5]), Integer.parseInt(v[7]), expires);
		}else if (key.startsWith(SLOWED_TILE)){
			if (slowEnabled && putTile(slowedTiles, Integer.parseInt(v[6]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), Integer.parseInt(v[5]), Integer.parseInt(v[7]), expires))
				slowedTileCount++;
		}else if (key.startsWith(SHIELDED_TILE)){
			if (shieldEnabled)
				putTile(shieldedTiles, Integer.parseInt(v[5]), Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), 1, expires);
		}else if (key.startsWith(ACCELERATED_ENTITY)){
			if (accelerationEnabled)
				putEntity(acceleratedEntities, UUID.fromString(v[4]), Integer.parseInt(v[3]), expires);
		}else if (key.startsWith(SLOWED_ENTITY)){
			if (slowEnabled)
				putEntity(slowedEntities, UUID.fromString(v[4]), Integer.parseInt(v[3]), expires);
		}
		// fortified tiles keep their chunk ticket until they expire; nothing to index
	}

	private static void onExpired(String key){
		String[] v = key.split("_");
		if (key.startsWith(ACCELERATED_TILE)){
			removeTile(acceleratedTiles, Integer.parseInt(v[6]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), Integer.parseInt(v[5]));
		}else if (key.startsWith(SLOWED_TILE)){
			if (removeTile(slowedTiles, Integer.parseInt(v[6]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), Integer.parseInt(v[5])))
				slowedTileCount--;
		}else if (key.startsWith(SHIELDED_TILE)){
			removeTile(shieldedTiles, Integer.parseInt(v[5]), Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[4]));
		}else if (key.startsWith(ACCELERATED_ENTITY)){
			acceleratedEntities.remove(UUID.fromString(v[4]));
		}else if (key.startsWith(SLOWED_ENTITY)){
			slowedEntities.remove(UUID.fromString(v[4]));
		}else if (key.startsWith(FORTIFIED_TILE)){
			World world = DimensionManager.getWorld(Integer.parseInt(v[5]));
			if (world != null)
				AMChunkLoader.INSTANCE.releaseStaticChunkLoad(TileEntity.class, Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[4]), world);
		}
	}

	/** @return true if the position was not indexed before */
	private static boolean putTile(Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> byDim, int dim, int x, int y, int z, int power, int expires){
		Long2ObjectOpenHashMap<Effect> tiles = byDim.get(dim);
		if (tiles == null){
			tiles = new Long2ObjectOpenHashMap<Effect>();
			byDim.put(dim, tiles);
		}
		long key = pack(x, y, z);
		Effect effect = tiles.get(key);
		boolean added = effect == null;
		if (added){
			effect = new Effect(x, y, z);
			tiles.put(key, effect);
		}
		effect.power = Math.max(1, power);
		effect.expires = expires;
		return added;
	}

	/** @return true if the position was indexed */
	private static boolean removeTile(Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> byDim, int dim, int x, int y, int z){
		Long2ObjectOpenHashMap<Effect> tiles = byDim.get(dim);
		if (tiles == null)
			return false;
		boolean removed = tiles.remove(pack(x, y, z)) != null;
		if (tiles.isEmpty())
			byDim.remove(dim);
		return removed;
	}

	private static Effect getTile(Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<Effect>> byDim, int dim, int x, int y, int z){
		Long2ObjectOpenHashMap<Effect> tiles = byDim.get(dim);
		return tiles == null ? null : tiles.get(pack(x, y, z));
	}

	private static void putEntity(HashMap<UUID, Effect> map, UUID id, int power, int expires){
		Effect effect = map.get(id);
		if (effect == null){
			effect = new Effect(0, 0, 0);
			map.put(id, effect);
		}
		effect.power = Math.max(1, power);
		effect.expires = expires;
	}

	//==================================================================================================================
	// hooks
	//==================================================================================================================

	/**
	 * Called from every server-side living update before AM2's other handling.
	 *
	 * @return true if the update must be cancelled because the entity is slowed this tick
	 */
	public static boolean onLivingUpdate(EntityLivingBase ent){
		if (!slowedEntities.isEmpty()){
			Effect slowed = slowedEntities.get(ent.getUniqueID());
			if (slowed != null){
				if (slowed.expires < now())
					slowedEntities.remove(ent.getUniqueID());
				else if (ent.ticksExisted % slowed.power != 0)
					return true;
			}
		}
		if (!acceleratedEntities.isEmpty() && !runningAccelerated){
			Effect accelerated = acceleratedEntities.get(ent.getUniqueID());
			if (accelerated != null){
				if (accelerated.expires < now())
					acceleratedEntities.remove(ent.getUniqueID());
				else
					pendingAccelerated.add(ent);
			}
		}
		return false;
	}

	/**
	 * Tile entity tick gate (see am2.mixin.early.MixinTileEntity). Only server tiles are slowed; the client
	 * never receives the index.
	 */
	public static boolean isTileSlowedThisTick(TileEntity te, World world){
		if (slowedTileCount <= 0 || world.isRemote)
			return false;
		Effect effect = getTile(slowedTiles, world.provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
		if (effect == null)
			return false;
		if (effect.expires < now()){
			if (removeTile(slowedTiles, world.provider.dimensionId, te.xCoord, te.yCoord, te.zCoord))
				slowedTileCount--;
			return false;
		}
		return world.getTotalWorldTime() % effect.power != 0;
	}

	public static boolean isShielded(World world, int x, int y, int z){
		if (shieldedTiles.isEmpty() || world.isRemote)
			return false;
		Effect effect = getTile(shieldedTiles, world.provider.dimensionId, x, y, z);
		return effect != null && effect.expires >= now();
	}

	/** Runs the extra updates of accelerated tiles and entities; called once per server tick (END phase). */
	public static void onServerTickEnd(){
		if (!pendingAccelerated.isEmpty()){
			runningAccelerated = true;
			try{
				for (int i = 0; i < pendingAccelerated.size(); i++){
					EntityLivingBase ent = pendingAccelerated.get(i);
					Effect effect = acceleratedEntities.get(ent.getUniqueID());
					if (effect == null)
						continue;
					for (int n = 0; n < effect.power && !ent.isDead; n++)
						ent.onUpdate();
				}
			}finally{
				runningAccelerated = false;
				pendingAccelerated.clear();
			}
		}

		if (acceleratedTiles.isEmpty())
			return;
		int now = now();
		for (ObjectIterator<Int2ObjectMap.Entry<Long2ObjectOpenHashMap<Effect>>> dims = Int2ObjectMaps.fastIterator(acceleratedTiles); dims.hasNext(); ){
			Int2ObjectMap.Entry<Long2ObjectOpenHashMap<Effect>> dim = dims.next();
			World world = DimensionManager.getWorld(dim.getIntKey());
			Long2ObjectOpenHashMap<Effect> tiles = dim.getValue();
			for (ObjectIterator<Long2ObjectMap.Entry<Effect>> it = Long2ObjectMaps.fastIterator(tiles); it.hasNext(); ){
				Effect effect = it.next().getValue();
				if (world == null || effect.expires < now){
					it.remove();
					continue;
				}
				// never load a chunk to accelerate it
				if (!world.blockExists(effect.x, effect.y, effect.z))
					continue;
				TileEntity te = world.getTileEntity(effect.x, effect.y, effect.z);
				Block block = world.getBlock(effect.x, effect.y, effect.z);
				boolean randomTicks = block.getTickRandomly();
				for (int i = 0; i < effect.power; i++){
					if (te != null && !te.isInvalid() && te.canUpdate())
						te.updateEntity();
					if (randomTicks && world.rand.nextInt(100) == 0)
						block.updateTick(world, effect.x, effect.y, effect.z, world.rand);
				}
			}
			if (tiles.isEmpty())
				dims.remove();
		}
	}

	/** Drops all indexed effects; casters rebuild them on their next countdown step. */
	public static void clear(){
		acceleratedTiles.clear();
		slowedTiles.clear();
		shieldedTiles.clear();
		acceleratedEntities.clear();
		slowedEntities.clear();
		pendingAccelerated.clear();
		slowedTileCount = 0;
	}

	//==================================================================================================================
	// spatial vortex
	//==================================================================================================================

	/**
	 * Balances RF and etherium between a caster's spatial vortices: half of the difference between the fullest and the
	 * emptiest vortex moves per step (RF limited by what the devices can extract and accept, etherium by 50000).
	 * Each vortex is the four diagonal neighbours at its height. Unloaded vortices are skipped, never loaded.
	 */
	private static final class SpatialVortex{
		private static final int[][] OFFSETS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
		private static final int MAX_TRANSFER = 50000;

		static void transfer(ArrayList<int[]> vortices){
			ArrayList<World> worlds = new ArrayList<World>(vortices.size());
			ArrayList<int[]> loaded = new ArrayList<int[]>(vortices.size());
			for (int[] v : vortices){
				World world = DimensionManager.getWorld(v[3]);
				if (world != null && world.blockExists(v[0], v[1], v[2])){
					worlds.add(world);
					loaded.add(v);
				}
			}
			int count = loaded.size();
			if (count < 2)
				return;

			int[] energy = new int[count];
			int[] extractable = new int[count];
			int[] dark = new int[count];
			int[] light = new int[count];
			for (int i = 0; i < count; i++){
				int[] v = loaded.get(i);
				World world = worlds.get(i);
				for (int[] o : OFFSETS){
					TileEntity te = world.getTileEntity(v[0] + o[0], v[1], v[2] + o[1]);
					if (te instanceof IEnergyHandler){
						energy[i] += ((IEnergyHandler)te).getEnergyStored(ForgeDirection.UNKNOWN);
						extractable[i] += ((IEnergyHandler)te).extractEnergy(ForgeDirection.UNKNOWN, MAX_TRANSFER, true);
					}
					if (te instanceof IPowerNode){
						dark[i] += (int)PowerNodeRegistry.For(world).getPower((IPowerNode)te, PowerTypes.DARK);
						light[i] += (int)PowerNodeRegistry.For(world).getPower((IPowerNode)te, PowerTypes.LIGHT);
					}
				}
			}

			int maxE = maxIndex(energy), minE = minIndex(energy);
			int maxD = maxIndex(dark), minD = minIndex(dark);
			int maxL = maxIndex(light), minL = minIndex(light);
			if (AMCore.config.getDebugVortex())
				LogHelper.info("Vortex balance: E %d->%d, D %d->%d, L %d->%d", energy[maxE], energy[minE], dark[maxD], dark[minD], light[maxL], light[minL]);

			int toTransferE = Math.min(extractable[maxE], (energy[maxE] - energy[minE]) / 2);
			int toTransferD = Math.min(MAX_TRANSFER, (dark[maxD] - dark[minD]) / 2);
			int toTransferL = Math.min(MAX_TRANSFER, (light[maxL] - light[minL]) / 2);

			if (toTransferE > 0){
				int acceptable = 0;
				for (TileEntity te : neighbours(worlds.get(minE), loaded.get(minE)))
					if (te instanceof IEnergyHandler)
						acceptable += ((IEnergyHandler)te).receiveEnergy(ForgeDirection.UNKNOWN, MAX_TRANSFER, true);
				toTransferE = Math.min(toTransferE, acceptable);
			}

			if (toTransferE > 0){
				int left = toTransferE;
				for (TileEntity te : neighbours(worlds.get(maxE), loaded.get(maxE)))
					if (te instanceof IEnergyHandler && left > 0)
						left -= ((IEnergyHandler)te).extractEnergy(ForgeDirection.UNKNOWN, left, false);
				left = toTransferE;
				for (TileEntity te : neighbours(worlds.get(minE), loaded.get(minE)))
					if (te instanceof IEnergyHandler && left > 0)
						left -= ((IEnergyHandler)te).receiveEnergy(ForgeDirection.UNKNOWN, left, false);
			}
			if (toTransferD > 0)
				moveEtherium(worlds, loaded, maxD, minD, PowerTypes.DARK, toTransferD);
			if (toTransferL > 0)
				moveEtherium(worlds, loaded, maxL, minL, PowerTypes.LIGHT, toTransferL);
		}

		private static void moveEtherium(ArrayList<World> worlds, ArrayList<int[]> vortices, int from, int to, PowerTypes type, int amount){
			int left = amount;
			World source = worlds.get(from);
			for (TileEntity te : neighbours(source, vortices.get(from)))
				if (te instanceof IPowerNode && left > 0)
					left -= (int)PowerNodeRegistry.For(source).consumePower((IPowerNode)te, type, left);
			left = amount;
			World target = worlds.get(to);
			for (TileEntity te : neighbours(target, vortices.get(to)))
				if (te instanceof IPowerNode && left > 0)
					left -= (int)PowerNodeRegistry.For(target).insertPower((IPowerNode)te, type, left);
		}

		private static ArrayList<TileEntity> neighbours(World world, int[] v){
			ArrayList<TileEntity> list = new ArrayList<TileEntity>(4);
			for (int[] o : OFFSETS){
				TileEntity te = world.getTileEntity(v[0] + o[0], v[1], v[2] + o[1]);
				if (te != null)
					list.add(te);
			}
			return list;
		}

		private static int maxIndex(int[] values){
			int index = 0;
			for (int i = 1; i < values.length; i++)
				if (values[i] > values[index]) index = i;
			return index;
		}

		private static int minIndex(int[] values){
			int index = 0;
			for (int i = 1; i < values.length; i++)
				if (values[i] < values[index]) index = i;
			return index;
		}
	}
}
