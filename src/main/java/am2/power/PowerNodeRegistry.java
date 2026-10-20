package am2.power;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import am2.LogHelper;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class PowerNodeRegistry{

	private static final Int2ObjectOpenHashMap<PowerNodeRegistry> serverDimensionPowerManagers = new Int2ObjectOpenHashMap<PowerNodeRegistry>();
	private static final Int2ObjectOpenHashMap<PowerNodeRegistry> clientDimensionPowerManagers = new Int2ObjectOpenHashMap<PowerNodeRegistry>();
	private static final PowerNodeRegistry dummyRegistry = new PowerNodeRegistry();

	static final int POWER_SEARCH_RADIUS = 10; //The literal power search radius
	static final int POWER_SEARCH_RADIUS_SQ = 100; //The power search radius squared.  This is used for faster vector math.
	static final int MAX_POWER_SEARCH_RADIUS = 10000; //The maximum power search radius (squared)

	public static final PowerNodeRegistry For(World world){
		if (world == null)
			return dummyRegistry;

		// called by every power node every tick: one primitive lookup
		Int2ObjectOpenHashMap<PowerNodeRegistry> dimensionPowerManagers = world.isRemote ? clientDimensionPowerManagers : serverDimensionPowerManagers;

		PowerNodeRegistry reg = dimensionPowerManagers.get(world.provider.dimensionId);
		if (reg == null){
			reg = new PowerNodeRegistry();
			dimensionPowerManagers.put(world.provider.dimensionId, reg);
		}
		return reg;

	}

	private PowerNodeRegistry(){
	}

	/**
	 * Chunk key (ChunkCoordIntPair.chunkXZ2Int) -> packed block position -> node data. Power nodes look themselves up
	 * several times per tick; this replaces a TreeMap whose comparator was not a valid ordering (chunks could compare
	 * greater than each other, so lookups missed and node lists were re-created over existing data) wrapping
	 * AMVector3-keyed maps whose hash was x+y+z.
	 */
	private final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<PowerNodeEntry>> powerNodes = new Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<PowerNodeEntry>>();

	private static long chunkKey(int chunkX, int chunkZ){
		return ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
	}

	private static int chunkKeyX(long key){
		return (int)key;
	}

	private static int chunkKeyZ(long key){
		return (int)(key >>> 32);
	}

	private static long posKey(int x, int y, int z){
		return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (y & 0xFFF);
	}

	private static int posX(long key){
		return (int)(key >> 38);
	}

	private static int posY(long key){
		return (int)(key << 52 >> 52);
	}

	private static int posZ(long key){
		return (int)(key << 26 >> 38);
	}

	public void registerPowerNode(IPowerNode node){
		registerPowerNodeInternal(node);
	}

	PowerNodeEntry registerPowerNodeInternal(IPowerNode node){
		TileEntity te = ((TileEntity)node);
		World world = te.getWorldObj();
		int chunkX = te.xCoord >> 4, chunkZ = te.zCoord >> 4;
		long chunk = chunkKey(chunkX, chunkZ);
		Long2ObjectOpenHashMap<PowerNodeEntry> nodeList = powerNodes.get(chunk);

		if (nodeList == null){
			LogHelper.trace("Node list not found.  Checking cache/files for prior data");
			ChunkCoordIntPair pair = new ChunkCoordIntPair(chunkX, chunkZ);
			NBTTagCompound compound = world == null ? null : PowerNodeCache.instance.getNBTForChunk(world, pair);
			if (compound != null && compound.hasKey("AM2PowerData"))
				LoadChunkFromNBT(pair, compound);
			nodeList = powerNodes.get(chunk);
			if (nodeList == null){
				nodeList = new Long2ObjectOpenHashMap<PowerNodeEntry>();
				powerNodes.put(chunk, nodeList);
			}
		}

		long pos = posKey(te.xCoord, te.yCoord, te.zCoord);
		//prevent duplicate registrations
		PowerNodeEntry pnd = nodeList.get(pos);
		if (pnd == null){
			pnd = new PowerNodeEntry();
			nodeList.put(pos, pnd);
			LogHelper.trace("Successfully registered power node at {%d, %d, %d}", te.xCoord, te.yCoord, te.zCoord);
		}
		return pnd;
	}

	public void removePowerNode(IPowerNode node){
		TileEntity te = (TileEntity)node;
		removePowerNode(chunkKey(te.xCoord >> 4, te.zCoord >> 4), posKey(te.xCoord, te.yCoord, te.zCoord));
	}

	/**
	 * Attempts to pair the source and destination nodes by either a direct link or by going through conduits.
	 *
	 * @param powerSource The power source
	 * @param destination The destination point
	 * @return A localized message to return to the entity attempting to pair the nodes, either of success or why it failed.
	 */
	public String tryPairNodes(IPowerNode powerSource, IPowerNode destination){
		//some simple validation

		if (powerSource == destination){
			return StatCollector.translateToLocal("am2.tooltip.nodePairToSelf");
		}

		//Can the power source provide any of the valid power types for the destination?
		ArrayList<PowerTypes> typesProvided = new ArrayList<PowerTypes>();
		for (PowerTypes type : destination.getValidPowerTypes()){
			if (powerSource.canProvidePower(type)){
				typesProvided.add(type);
			}
		}
		if (typesProvided.isEmpty()){
			//no valid power types can be provided
			return StatCollector.translateToLocal("am2.tooltip.noSupportedPowertypes");
		}

		//set up vectors and calculate distance for pathing purposes
		AMVector3 sourceLocation = new AMVector3((TileEntity)powerSource);
		AMVector3 destLocation = new AMVector3((TileEntity)destination);
		double rawDist = sourceLocation.distanceSqTo(destLocation);

		if (rawDist > MAX_POWER_SEARCH_RADIUS){
			return StatCollector.translateToLocal("am2.tooltip.nodesTooFar");
		}

		//construct a list of all valid power types common between the source and destination
		int successes = 0;

		for (PowerTypes type : typesProvided){
			LinkedList<AMVector3> powerPath = new LinkedList<AMVector3>();
			PowerNodePathfinder pathfinder = new PowerNodePathfinder(((TileEntity)powerSource).getWorldObj(), sourceLocation, destLocation, type);
			List<AMVector3> path = pathfinder.compute(sourceLocation);
			if (path == null)
				continue;
			for (AMVector3 vec : path){
				powerPath.addFirst(vec);
			}
			successes++;
			getPowerNodeData(destination).registerNodePath(type, powerPath);
		}

		//are the nodes too far apart?
		if (successes == 0){
			return StatCollector.translateToLocal("am2.tooltip.noPathFound");
		}

		if (successes == typesProvided.size())
			return StatCollector.translateToLocal("am2.tooltip.success");
		return StatCollector.translateToLocal("am2.tooltip.partialSuccess");
	}

	/**
	 * Attempts to disconnect all sources powering the passed-in block
	 */
	public void tryDisconnectAllNodes(IPowerNode node){
		getPowerNodeData(node).clearNodePaths();
	}

	private void removePowerNode(long chunk, long pos){
		Long2ObjectOpenHashMap<PowerNodeEntry> nodeList = powerNodes.get(chunk);
		if (nodeList == null){
			LogHelper.error("Power Node removal requested in a non-tracked chunk (%d, %d)!", chunkKeyX(chunk), chunkKeyZ(chunk));
			return;
		}
		nodeList.remove(pos);
		if (nodeList.isEmpty())
			powerNodes.remove(chunk);
	}

	public float requestPower(IPowerNode destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);

		if (data == null){
			return 0;
		}

		float requested = data.requestPower(((TileEntity)destination).getWorldObj(), type, amount, destination.getCapacity());

		return requested;
	}

	public float consumePower(IPowerNode consumer, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(consumer);

		if (data == null){
			return 0;
		}

		float availablePower = data.getPower(type);
		if (availablePower < amount)
			amount = availablePower;
		data.setPower(type, availablePower - amount);
		return amount;
	}

	public float insertPower(IPowerNode destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);
		if (data == null){
			return 0;
		}
		float curPower = data.getPower(type);
		if (curPower + amount > destination.getCapacity())
			amount = destination.getCapacity() - curPower;
		data.setPower(type, curPower + amount);

		return amount;
	}

	public void setPower(IPowerNode destination, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(destination);

		if (data == null){
			return;
		}

		if (amount > destination.getCapacity())
			amount = destination.getCapacity();
		data.setPower(type, amount);
	}

	public boolean checkPower(IPowerNode node, PowerTypes type, float amount){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return false;
		}

		return data.getPower(type) >= amount;
	}

	public boolean checkPower(IPowerNode node, float amount){
		for (PowerTypes type : PowerTypes.all())
			if (checkPower(node, type, amount))
				return true;
		return false;
	}

	public boolean checkPower(IPowerNode node){
		return getHighestPower(node) > 0;
	}

	public float getPower(IPowerNode node, PowerTypes type){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return 0;
		}

		return data.getPower(type);
	}

	public float getHighestPower(IPowerNode node){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return 0;
		}

		return data.getHighestPower();
	}

	public PowerTypes getHighestPowerType(IPowerNode node){
		PowerNodeEntry data = getPowerNodeData(node);

		if (data == null){
			return PowerTypes.NONE;
		}

		return data.getHighestPowerType();
	}

	PowerNodeEntry getPowerNodeData(IPowerNode node){
		TileEntity te = (TileEntity)node;
		Long2ObjectOpenHashMap<PowerNodeEntry> nodeList = powerNodes.get(chunkKey(te.xCoord >> 4, te.zCoord >> 4));
		if (nodeList != null){
			PowerNodeEntry pnd = nodeList.get(posKey(te.xCoord, te.yCoord, te.zCoord));
			if (pnd != null)
				return pnd;
		}
		return registerPowerNodeInternal(node);
	}

	/**
	 * Returns all power nodes within POWER_SEARCH_RADIUS
	 *
	 * @param location The center point to search from
	 * @param power    The power type that the provider needs to supply or accept to be considered valid
	 * @return An array of IPowerNodes
	 */
	public IPowerNode[] getAllNearbyNodes(World world, AMVector3 location, PowerTypes power){
		int cx = (int)location.x, cy = (int)location.y, cz = (int)location.z;
		int chunkX = cx >> 4, chunkZ = cz >> 4;
		ArrayList<IPowerNode> nodes = new ArrayList<IPowerNode>();
		int deadNodesRemoved = 0;
		// the home chunk and the neighbours the search radius reaches into
		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				if ((i != 0 || j != 0) && ((cx + POWER_SEARCH_RADIUS * i) >> 4 != chunkX + i || (cz + POWER_SEARCH_RADIUS * j) >> 4 != chunkZ + j))
					continue;
				long chunk = chunkKey(chunkX + i, chunkZ + j);
				Long2ObjectOpenHashMap<PowerNodeEntry> nodesInChunk = powerNodes.get(chunk);
				if (nodesInChunk == null)
					continue;
				long[] positions = nodesInChunk.keySet().toLongArray();
				for (long pos : positions){
					int x = posX(pos), y = posY(pos), z = posZ(pos);
					int dx = x - cx, dy = y - cy, dz = z - cz;
					if (dx * dx + dy * dy + dz * dz > POWER_SEARCH_RADIUS_SQ || (dx == 0 && dy == 0 && dz == 0))
						continue;
					// never load a chunk to look for nodes
					if (!world.blockExists(x, y, z))
						continue;
					TileEntity te = world.getTileEntity(x, y, z);
					if (!(te instanceof IPowerNode)){
						//opportune time to remove dead power nodes
						removePowerNode(chunk, pos);
						deadNodesRemoved++;
						continue;
					}
					nodes.add((IPowerNode)te);
				}
			}
		}

		if (deadNodesRemoved > 0)
			LogHelper.trace("Removed %d dead power nodes", deadNodesRemoved);

		return nodes.toArray(new IPowerNode[nodes.size()]);
	}

	public void SaveChunkToNBT(ChunkCoordIntPair chunk, NBTTagCompound compound){
		Long2ObjectOpenHashMap<PowerNodeEntry> nodeData = powerNodes.get(chunkKey(chunk.chunkXPos, chunk.chunkZPos));
		if (nodeData == null){
			//we're not tracking anything in this chunk
			return;
		}
		NBTTagList powerNodeTagList = new NBTTagList();
		for (Long2ObjectMap.Entry<PowerNodeEntry> entry : nodeData.long2ObjectEntrySet()){
			long pos = entry.getLongKey();
			NBTTagCompound nodeCompound = new NBTTagCompound();
			nodeCompound.setInteger("xCoord", posX(pos));
			nodeCompound.setInteger("yCoord", posY(pos));
			nodeCompound.setInteger("zCoord", posZ(pos));
			nodeCompound.setTag("nodeData", entry.getValue().saveToNBT());
			powerNodeTagList.appendTag(nodeCompound);
		}

		LogHelper.trace("Saved %d power node entries", powerNodeTagList.tagCount());

		compound.setTag("AM2PowerData", powerNodeTagList);
	}

	public void LoadChunkFromNBT(ChunkCoordIntPair chunk, NBTTagCompound compound){
		if (!compound.hasKey("AM2PowerData"))
			return; //nothing was saved in this chunk for the power system
		NBTTagList powerNodeTagList = compound.getTagList("AM2PowerData", Constants.NBT.TAG_COMPOUND);
		Long2ObjectOpenHashMap<PowerNodeEntry> chunkPowerData = new Long2ObjectOpenHashMap<PowerNodeEntry>();
		for (int i = 0; i < powerNodeTagList.tagCount(); ++i){
			NBTTagCompound nodeCompound = powerNodeTagList.getCompoundTagAt(i);
			PowerNodeEntry pnd = new PowerNodeEntry();
			pnd.readFromNBT(nodeCompound.getCompoundTag("nodeData"));
			chunkPowerData.put(posKey(nodeCompound.getInteger("xCoord"), nodeCompound.getInteger("yCoord"), nodeCompound.getInteger("zCoord")), pnd);
		}

		LogHelper.trace("Loaded %d power node entries", chunkPowerData.size());

		powerNodes.put(chunkKey(chunk.chunkXPos, chunk.chunkZPos), chunkPowerData);
	}

	public void unloadChunk(Chunk chunk){
		powerNodes.remove(chunkKey(chunk.xPosition, chunk.zPosition));
	}

	public boolean hasDataForChunk(Chunk chunk){
		return powerNodes.containsKey(chunkKey(chunk.xPosition, chunk.zPosition));
	}

	private class IPowerNodeComparer implements Comparator<IPowerNode>{

		@Override
		public int compare(IPowerNode o1, IPowerNode o2){
			TileEntity te1 = (TileEntity)o1;
			TileEntity te2 = (TileEntity)o2;

			if (te1.xCoord == te2.xCoord && te1.zCoord == te2.zCoord && te1.yCoord == te2.yCoord)
				return 0;

			if (te1.xCoord > te2.xCoord){
				return 1;
			}else{
				if (te1.zCoord > te2.zCoord){
					return 1;
				}else{
					if (te1.yCoord > te2.yCoord){
						return 1;
					}else{
						return -1;
					}
				}
			}
		}

	}

	public NBTTagCompound getDataCompoundForNode(IPowerNode node){
		PowerNodeEntry pnd = getPowerNodeData(node);
		if (pnd == null)
			return null;
		return pnd.saveToNBT();
	}

	public void setDataCompoundForNode(IPowerNode node, NBTTagCompound compound){
		PowerNodeEntry pnd = getPowerNodeData(node);
		if (pnd == null)
			return;
		pnd.readFromNBT((NBTTagCompound)compound.copy());
	}

	public PowerNodeEntry parseFromNBT(NBTTagCompound compound){
		PowerNodeEntry pnd = new PowerNodeEntry();
		pnd.readFromNBT((NBTTagCompound)compound.copy());
		return pnd;
	}

	public HashMap<ChunkCoordIntPair, NBTTagCompound> saveAll(){
		HashMap<ChunkCoordIntPair, NBTTagCompound> allData = new HashMap<ChunkCoordIntPair, NBTTagCompound>();
		for (long key : powerNodes.keySet()){
			ChunkCoordIntPair pair = new ChunkCoordIntPair(chunkKeyX(key), chunkKeyZ(key));
			NBTTagCompound chunkCompound = new NBTTagCompound();
			SaveChunkToNBT(pair, chunkCompound);
			allData.put(pair, chunkCompound);
		}

		return allData;
	}

	public void unloadAll(){
		powerNodes.clear();
	}
}
