package am2.api.blocks;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

public class MultiblockStructureDefinition{
	public final StructureGroup mainGroup;
	private final ArrayList<StructureGroup> blockGroups;
	private final ArrayList<Integer> mutexCache;

	public static final int MAINGROUP_MUTEX = 1;

	private final String id;

	private int maxX = 0;
	private int minX = 0;
	private int maxY = 0;
	private int minY = 0;
	private int maxZ = 0;
	private int minZ = 0;

	public MultiblockStructureDefinition(String id){

		blockGroups = new ArrayList<>();
		mutexCache = new ArrayList<>();
		this.id = id;

		//default group
		mainGroup = createGroup("main", MAINGROUP_MUTEX);
	}

	public String getID(){
		return this.id;
	}

	public ArrayList<Integer> getMutexList(){
		return mutexCache;
	}

	public ArrayList<StructureGroup> getGroupsForMutex(int mutex){
		ArrayList<StructureGroup> toReturn = new ArrayList<StructureGroup>();

		for (StructureGroup group : blockGroups){
			if (group.mutex == mutex){
				toReturn.add(group);
			}
		}

		return toReturn;
	}

	public ArrayList<BlockDec> getAllowedBlocksAt(StructureGroup group, BlockCoord coord){
		return group.getAllowedBlocksAt(coord);
	}

	public ArrayList<BlockDec> getAllowedBlocksAt(BlockCoord coord){
		return mainGroup.getAllowedBlocksAt(coord);
	}

	public void addAllowedBlock(int offsetX, int offsetY, int offsetZ, Block block, int meta){

		if (offsetY > maxY){
			maxY = offsetY;
		}else if (offsetY < minY){
			minY = offsetY;
		}

		if (offsetX > maxX){
			maxX = offsetX;
		}else if (offsetX < minX){
			minX = offsetX;
		}

		if (offsetZ > maxZ){
			maxZ = offsetZ;
		}else if (offsetZ < minZ){
			minZ = offsetZ;
		}

		mainGroup.addAllowedBlock(offsetX, offsetY, offsetZ, block, meta);
	}

	public void addAllowedBlock(int offsetX, int offsetY, int offsetZ, Block block){
		addAllowedBlock(offsetX, offsetY, offsetZ, block, WILDCARD_VALUE);
	}

	public void addAllowedBlock(StructureGroup group, int offsetX, int offsetY, int offsetZ, Block block, int meta){
		if (!blockGroups.contains(group)){
			blockGroups.add(group);
		}

		if (offsetY > maxY){
			maxY = offsetY;
		}else if (offsetY < minY){
			minY = offsetY;
		}

		if (offsetX > maxX){
			maxX = offsetX;
		}else if (offsetX < minX){
			minX = offsetX;
		}

		if (offsetZ > maxZ){
			maxZ = offsetZ;
		}else if (offsetZ < minZ){
			minZ = offsetZ;
		}

		group.addAllowedBlock(offsetX, offsetY, offsetZ, block, meta);
	}
	public void addAllowedBlock(StructureGroup group, int offsetX, int offsetY, int offsetZ, List<BlockDec> decs){
		if (!blockGroups.contains(group)){
			blockGroups.add(group);
		}

		if (offsetY > maxY){
			maxY = offsetY;
		}else if (offsetY < minY){
			minY = offsetY;
		}

		if (offsetX > maxX){
			maxX = offsetX;
		}else if (offsetX < minX){
			minX = offsetX;
		}

		if (offsetZ > maxZ){
			maxZ = offsetZ;
		}else if (offsetZ < minZ){
			minZ = offsetZ;
		}

		group.addAllowedBlocks(offsetX, offsetY, offsetZ, decs);
	}

	public void addAllowedBlock(StructureGroup group, int offsetX, int offsetY, int offsetZ, Block block){
		addAllowedBlock(group, offsetX, offsetY, offsetZ, block, WILDCARD_VALUE);
	}

	public StructureGroup createGroup(String name, int mutex){
		if (!mutexCache.contains(mutex)){
			mutexCache.add(mutex);
		}
		StructureGroup group = new StructureGroup(name, mutex);
		blockGroups.add(group);
		return group;
	}

	public StructureGroup copyGroup(String originalName, String destinationName, int newMutex){
		StructureGroup copyGroup = null;
		for (StructureGroup group : blockGroups){
			if (group.name.equals(originalName)){
				copyGroup = group;
				break;
			}
		}
		if (copyGroup == null) return null;
		StructureGroup newGroup = createGroup(destinationName, newMutex > -1 ? newMutex : copyGroup.mutex);
		newGroup.allowedBlocks.putAll(copyGroup.allowedBlocks);
		return newGroup;
	}

	public StructureGroup copyGroup(String originalName, String destinationName){
		return copyGroup(originalName, destinationName, -1);
	}

	public ArrayList<StructureGroup> getMatchedGroups(int mutex, World world, int originX, int originY, int originZ){
		ArrayList<StructureGroup> toReturn = new ArrayList<>();
		for (int i= 0; i < 4; i ++){
			toReturn = getMatchedGroupsDirectional(mutex,world,originX,originY,originZ,i);
			if (!toReturn.isEmpty())break;
		}
		return toReturn;
	}
	public ArrayList<StructureGroup> getMatchedGroupsDirectional(int mutex, World world, int originX, int originY, int originZ,int direction){
		ArrayList<StructureGroup> toReturn = new ArrayList<>();
		for (StructureGroup group : blockGroups){
			if ((group.mutex & mutex) == group.mutex){
				if (group.matchGroup(world, originX, originY, originZ, direction)){
					toReturn.add(group);
				}
			}
		}
		return toReturn;
	}
	public boolean matchGroup(StructureGroup group, World world, int X, int Y, int Z){
		boolean value = false;
		for (int i= 0; i < 4; i ++){
			if(group.matchGroup(world,X,Y,Z, i)){
				value = true;
				break;
			}
		}
		return value;
	}
	public boolean matchGroup(StructureGroup group, World world, int X, int Y, int Z, int direction){
		return group.matchGroup(world,X,Y,Z, direction);
	}
	private boolean matchMutex(int mutex, World world, int originX, int originY, int originZ, int direction){
		for (StructureGroup group : blockGroups){
			if ((group.mutex & mutex) == group.mutex){
				if (group.matchGroup(world, originX, originY, originZ, direction)){
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkStructure(World world, int originX, int originY, int originZ){
		boolean valid = false;
		for (int i= 0; i < 4; i ++){
			if(checkStructureDirectional( world, originX, originY, originZ, i)){
				valid = true;
				break;
			}
		}
		return valid;
	}
	public boolean checkStructureDirectional(World world, int originX, int originY, int originZ, int direction ){
		boolean valid = true;
		for (int i : mutexCache){
			if(!matchMutex(i, world, originX, originY, originZ, direction)){
				valid = false;
				break;
			}
		}
		return valid;
	}

	public int getMinLayer(){
		return this.minY;
	}

	public int getMaxLayer(){
		return this.maxY;
	}

	public int getHeight(){
		return this.maxY - this.minY;
	}

	public int getWidth(){
		return this.maxX - this.minX;
	}

	public int getLength(){
		return this.maxZ - this.minZ;
	}

	public HashMap<BlockCoord, ArrayList<BlockDec>> getStructureLayer(int layer){
		return mainGroup.getStructureLayer(layer);
	}

	public HashMap<BlockCoord, ArrayList<BlockDec>> getStructureLayer(StructureGroup group, int layer){
		return group.getStructureLayer(layer);
	}

	public void removeMutex(int mutex, World world, int x, int y, int z){
		for (StructureGroup group : blockGroups){
			if (group.mutex == mutex){
				group.deleteBlocksFromWorld(world, x, y, z);
			}
		}
	}

	public static class StructureComponent{
		private final List<BlockDec> validblocks;
		private final BlockCoord coord;

		public StructureComponent(BlockCoord coord,List<BlockDec> dec){
			validblocks = dec;
			this.coord = coord;
		}
		public StructureComponent(BlockCoord coord,Block block,short meta){
			this.coord = coord;
			validblocks = new ArrayList<>();
			validblocks.add(new BlockDec(block,meta));
		}
		public BlockCoord getCoord(){
			return coord;
		}
		public List<BlockDec> getValidblocks(){
			return validblocks;
		}
		public boolean match(BlockDec dec){
			for(BlockDec pair : validblocks){
				if(pair.getBlock() == dec.block && (pair.getMeta() == dec.meta || pair.meta == WILDCARD_VALUE))
					return true;
			}
			return false;
		}
	}
	public class StructureGroup{
		String name;
		int mutex;
		HashMap<BlockCoord, ArrayList<BlockDec>> allowedBlocks;
		List<StructureComponent> components;

		public StructureGroup(String name, int mutex){
			this.name = name;
			this.mutex = mutex;
			allowedBlocks = new HashMap<>();
			components = new ArrayList<>();
		}

		void addAllowedBlock(int offsetX, int offsetY, int offsetZ, Block block, int meta){
			BlockCoord originOffset = new BlockCoord(offsetX, offsetY, offsetZ);
			allowedBlocks.computeIfAbsent(originOffset, v -> new ArrayList<>()).add(new BlockDec(block, meta));
		}
		void addAllowedBlocks(int offsetX, int offsetY, int offsetZ, List<BlockDec> decs){
			BlockCoord originOffset = new BlockCoord(offsetX, offsetY, offsetZ);
			allowedBlocks.computeIfAbsent(originOffset, k -> new ArrayList<>()).addAll(decs);
		}

		ArrayList<BlockDec> getAllowedBlocksAt(BlockCoord coord){
			return allowedBlocks.get(coord);
		}

		boolean matchGroup(World world, int originX, int originY, int originZ, int direction){
			for (BlockCoord offset : allowedBlocks.keySet()){
				Block block = world.getBlock(originX + offset.getX(direction), originY + offset.getY(), originZ + offset.getZ(direction));
				int meta = world.getBlockMetadata(originX + offset.getX(direction), originY + offset.getY(), originZ + offset.getZ(direction));
				ArrayList<BlockDec> positionReplacements = allowedBlocks.get(offset);
				boolean valid = false;
				for (BlockDec bd : positionReplacements){
					if (bd.block == block && (bd.meta == WILDCARD_VALUE || bd.meta == meta)){
						valid = true;
						break;
					}
				}
				if (!valid) return false;
			}
			return true;
		}

		HashMap<BlockCoord, ArrayList<BlockDec>> getStructureLayer(int layer){
			HashMap<BlockCoord, ArrayList<BlockDec>> toReturn = new HashMap<>();

			if (layer > getMaxLayer() || layer < getMinLayer()){
				return toReturn;
			}

			for (BlockCoord bc : allowedBlocks.keySet()){
				if (bc.y == layer){
					toReturn.put(bc, allowedBlocks.get(bc));
				}
			}
			return toReturn;
		}

		public void replaceAllBlocksOfType(Block originalBlock, Block newBlock){
			replaceAllBlocksOfType(originalBlock, WILDCARD_VALUE, newBlock, WILDCARD_VALUE);
		}

		public void replaceAllBlocksOfType(Block originalBlock, int originalMeta, Block newBlock, int newMeta){
			for (ArrayList<BlockDec> list : allowedBlocks.values()){
				for (BlockDec bd : list){
					if (bd.block == originalBlock && bd.meta == originalMeta || originalMeta == WILDCARD_VALUE){
						bd.block = newBlock;
						if (newMeta != WILDCARD_VALUE){
							bd.meta = newMeta;
						}
					}
				}
			}
		}

		public HashMap<BlockCoord, ArrayList<BlockDec>> getAllowedBlocks(){
			return (HashMap<BlockCoord, ArrayList<BlockDec>>)allowedBlocks.clone();
		}

		public void deleteBlocksFromWorld(World world, int x, int y, int z){
			for (BlockCoord offset : allowedBlocks.keySet()){
				world.setBlockToAir(x + offset.x, y + offset.y, z + offset.z);
			}
		}
	}
}
