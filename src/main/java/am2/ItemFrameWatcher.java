package am2;

import am2.blocks.BlocksCommonProxy;
import am2.items.ItemsCommonProxy;
import am2.network.CompendiumProgressMessage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.WorldEvent;

import java.util.ArrayList;

/**
 * Turns a book in an item frame into an Arcane Compendium after it spends {@link #processTime} progress next to liquid
 * essence (witchwood with the alternative start). Server side only: frames are queued from EntityConstructing and
 * player interaction (AMEventHandler) and ticked from ServerTickHandler.
 */
public class ItemFrameWatcher{

	private static final int processTime = 800;
	private static final int radius = 2;

	/**
	 * Frame -> progress. Identity keys: there is one entity object per frame per side, which is what the old
	 * entity-id-and-side equality compared, without a wrapper object per frame.
	 */
	private final Reference2IntOpenHashMap<EntityItemFrame> watchedFrames = new Reference2IntOpenHashMap<EntityItemFrame>();
	private final ArrayList<EntityItemFrame> queuedAddFrames = new ArrayList<EntityItemFrame>();
	private final ArrayList<EntityItemFrame> queuedRemoveFrames = new ArrayList<EntityItemFrame>();
	/** Reused every tick. */
	private final ArrayList<EntityItemFrame> toRemove = new ArrayList<EntityItemFrame>();
	/** The (at most 2x2) chunks around the frame being checked, so the 125 block reads skip the chunk lookup. */
	private final Chunk[] area = new Chunk[4];

	public void checkWatchedFrames(){
		if (watchedFrames.isEmpty() && queuedAddFrames.isEmpty() && queuedRemoveFrames.isEmpty())
			return; // runs every server tick

		updateQueuedChanges();

		for (ObjectIterator<Reference2IntMap.Entry<EntityItemFrame>> it = Reference2IntMaps.fastIterator(watchedFrames); it.hasNext(); ){
			Reference2IntMap.Entry<EntityItemFrame> entry = it.next();
			EntityItemFrame frame = entry.getKey();
			if (frame.worldObj == null)
				continue;

			boolean remove = !frame.worldObj.isRemote || entry.getIntValue() >= processTime;
			if (frameIsValid(frame)){
				if (!checkFrameRadius(entry))
					remove = false;
			}else{
				entry.setValue(entry.getIntValue() + 1);
			}
			if (remove)
				toRemove.add(frame);
		}

		for (int i = 0; i < toRemove.size(); i++)
			queuedRemoveFrames.add(toRemove.get(i));
		toRemove.clear();
	}

	/** Advances the frame's progress for every target block around it; true when the frame should stop being watched. */
	private boolean checkFrameRadius(Reference2IntMap.Entry<EntityItemFrame> entry){
		EntityItemFrame frame = entry.getKey();
		World world = frame.worldObj;
		int fx = (int)frame.posX, fy = (int)frame.posY, fz = (int)frame.posZ;

		// a frame whose surroundings are unloaded stops being watched: reading its 5x5x5 area would load the chunks again
		// (every tick), and keeping it would keep the unloaded entity. When its chunk loads, the new frame entity is
		// watched again through EntityConstructing, with a fresh timer as before (it gets a new entity id).
		if (!world.checkChunksExist(fx - radius, fy - radius, fz - radius, fx + radius, fy + radius, fz + radius))
			return true;

		int cx0 = (fx - radius) >> 4, cz0 = (fz - radius) >> 4;
		for (int dx = 0; dx < 2; dx++){
			for (int dz = 0; dz < 2; dz++){
				int cx = cx0 + dx, cz = cz0 + dz;
				area[dx * 2 + dz] = cx <= (fx + radius) >> 4 && cz <= (fz + radius) >> 4 ? world.getChunkFromChunkCoords(cx, cz) : null;
			}
		}

		Block first, second;
		if (AMCore.config.isAlternativeStart()){
			first = BlocksCommonProxy.witchwoodLeaves;
			second = BlocksCommonProxy.witchwoodLog;
		}else{
			first = second = BlocksCommonProxy.liquidEssence;
		}

		boolean shouldRemove = true;
		// same x, y, z order as before: progress and the particle packets of the last tick are unchanged
		for (int i = -radius; i <= radius; ++i){
			int x = fx + i;
			for (int j = -radius; j <= radius; ++j){
				int y = fy + j;
				for (int k = -radius; k <= radius; ++k){
					int z = fz + k;
					Block block = blockAt(x, y, z, cx0, cz0);
					if (block != first && block != second)
						continue;

					int time = entry.getIntValue() + 1;
					entry.setValue(time);
					if (time >= processTime){
						if (!world.isRemote){
							frame.setDisplayedItem(new ItemStack(ItemsCommonProxy.arcaneCompendium));
							clearArea();
							return true;
						}
					}else{
						shouldRemove = false;
						AMCore.ITEMFRAMENETWORK.sendToAllAround(new CompendiumProgressMessage(false, frame.getEntityId(), x, y, z), new NetworkRegistry.TargetPoint(frame.dimension, x, y, z, 32));
					}
				}
			}
		}
		clearArea();
		return shouldRemove;
	}

	/** World.getBlock for the loaded area around the frame, without its per-call chunk lookup. */
	private Block blockAt(int x, int y, int z, int cx0, int cz0){
		if (y < 0 || y >= 256)
			return Blocks.air;
		Chunk chunk = area[((x >> 4) - cx0) * 2 + ((z >> 4) - cz0)];
		return chunk.getBlock(x & 15, y, z & 15);
	}

	private void clearArea(){
		area[0] = area[1] = area[2] = area[3] = null; // no chunk kept past the check
	}

	private boolean frameIsValid(EntityItemFrame frame){
		return !frame.isDead && frame.getDisplayedItem() != null && frame.getDisplayedItem().getItem() instanceof ItemBook;
	}

	private void updateQueuedChanges(){
		if (!queuedAddFrames.isEmpty()){
			for (int i = 0; i < queuedAddFrames.size(); i++){
				EntityItemFrame frame = queuedAddFrames.get(i);
				if (frame.getDisplayedItem() == null || frame.getDisplayedItem().getItem() != ItemsCommonProxy.arcaneCompendium)
					watchedFrames.put(frame, 0);
			}
			queuedAddFrames.clear();
		}

		if (!queuedRemoveFrames.isEmpty()){
			for (int i = 0; i < queuedRemoveFrames.size(); i++){
				EntityItemFrame frame = queuedRemoveFrames.get(i);
				// absent frames read as 0 progress, so a single lookup covers both checks
				int time = watchedFrames.removeInt(frame);
				if (time >= processTime && !frame.isDead && !frame.worldObj.isRemote && frame.getDisplayedItem() != null &&
						(frame.getDisplayedItem().getItem() == Items.book || frame.getDisplayedItem().getItem() == ItemsCommonProxy.arcaneCompendium)){
					AMCore.ITEMFRAMENETWORK.sendToAllAround(new CompendiumProgressMessage(true, frame.getEntityId(), (int)frame.posX, (int)frame.posY, (int)frame.posZ), new NetworkRegistry.TargetPoint(frame.dimension, frame.posX, frame.posY, frame.posZ, 32));
				}
			}
			queuedRemoveFrames.clear();
		}
	}

	public void startWatchingFrame(EntityItemFrame frame){
		if (frame != null)
			queuedAddFrames.add(frame);
	}

	public void stopWatchingFrame(EntityItemFrame frame){
		if (frame != null)
			queuedRemoveFrames.add(frame);
	}

	/** Frames of an unloaded world are dropped with it (AGENTS.md: caches holding entities are cleared on unload). */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event){
		World world = event.world;
		for (ObjectIterator<Reference2IntMap.Entry<EntityItemFrame>> it = Reference2IntMaps.fastIterator(watchedFrames); it.hasNext(); ){
			if (it.next().getKey().worldObj == world)
				it.remove();
		}
		removeFramesOf(queuedAddFrames, world);
		removeFramesOf(queuedRemoveFrames, world);
	}

	private static void removeFramesOf(ArrayList<EntityItemFrame> frames, World world){
		for (int i = frames.size() - 1; i >= 0; i--){
			if (frames.get(i).worldObj == world)
				frames.remove(i);
		}
	}
}
