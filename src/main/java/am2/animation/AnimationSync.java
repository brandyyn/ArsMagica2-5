package am2.animation;

import am2.AMCore;
import am2.network.AMDataReader;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import net.minecraft.entity.Entity;

/** Server-driven animation state: the server sets the animation and nearby clients follow. */
public final class AnimationSync{

	/** Animations are only visible to players that can see the entity. */
	private static final double SYNC_RANGE = 96;

	/** Render partial ticks of the current frame, set by the client tick handler. */
	public static float partialTicks;

	private AnimationSync(){
	}

	public static void setAnimation(IAnimatedEntity animated, int animID){
		Entity entity = (Entity)animated;
		if (entity.worldObj.isRemote)
			return;
		animated.setAnimID(animID);
		byte[] data = new AMDataWriter().add(entity.getEntityId()).add((byte)animID).generate();
		AMNetHandler.INSTANCE.sendPacketToAllClientsNear(entity.dimension, entity.posX, entity.posY, entity.posZ, SYNC_RANGE, AMPacketIDs.ANIMATION, data);
	}

	/** Client side of {@link #setAnimation}. */
	public static void handlePacket(byte[] data){
		AMDataReader reader = new AMDataReader(data, false);
		Entity entity = AMCore.proxy.getEntityByID(reader.getInt());
		int animID = reader.getByte();
		if (entity instanceof IAnimatedEntity && animID != -1){
			((IAnimatedEntity)entity).setAnimID(animID);
			if (animID == 0)
				((IAnimatedEntity)entity).setAnimTick(0);
		}
	}
}
