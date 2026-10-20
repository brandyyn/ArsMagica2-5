package am2.animation;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * AI task that plays an animation on an {@link IAnimatedEntity}. Automatic tasks run while the entity's animation ID
 * matches theirs; the others start the animation themselves when {@link #shouldAnimate()} says so.
 */
public abstract class AIAnimation extends EntityAIBase{

	private final IAnimatedEntity animatedEntity;

	public AIAnimation(IAnimatedEntity entity){
		animatedEntity = entity;
		setMutexBits(7);
	}

	public abstract int getAnimID();

	@SuppressWarnings("unchecked")
	public <T extends EntityLiving> T getEntity(){
		return (T)animatedEntity;
	}

	public abstract boolean isAutomatic();

	public abstract int getDuration();

	public boolean shouldAnimate(){
		return false;
	}

	@Override
	public boolean shouldExecute(){
		if (isAutomatic())
			return animatedEntity.getAnimID() == getAnimID();
		return shouldAnimate();
	}

	@Override
	public void startExecuting(){
		if (!isAutomatic())
			AnimationSync.setAnimation(animatedEntity, getAnimID());
		animatedEntity.setAnimTick(0);
	}

	@Override
	public boolean continueExecuting(){
		return animatedEntity.getAnimTick() < getDuration();
	}

	@Override
	public void resetTask(){
		AnimationSync.setAnimation(animatedEntity, 0);
	}
}
