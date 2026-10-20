package am2.buffs;

import am2.AMCore;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public abstract class BuffEffect extends PotionEffect{
	protected boolean InitialApplication;
	protected boolean HasNotified;

	public BuffEffect(int buffID, int duration, int amplifier){
		super(buffID, duration, amplifier > 0 ? amplifier - 1 : amplifier);
		InitialApplication = true;
		HasNotified = (duration / 20) <= 5; //disable notification for effects that last less than 5 seconds
	}

	public boolean shouldNotify(){
		return true;
	}

	//effect that is performed on initial tick
	public abstract void applyEffect(EntityLivingBase entityliving);

	//effect that is performed when the duration ends
	public abstract void stopEffect(EntityLivingBase entityliving);

	private void effectEnding(EntityLivingBase entityliving){
		BuffList.buffEnding(this.getPotionID());
		stopEffect(entityliving);
	}

	//Effect that is performed on intermediate ticks
	@Override
	public void performEffect(EntityLivingBase entityliving){
	}
	@Override
	public boolean onUpdate(EntityLivingBase entityliving){
		//check for if we are for the first time applying the effect
		if (InitialApplication){
			InitialApplication = false;
			applyEffect(entityliving);
		}
		//check if we are for the last time applying the effect
		else if (getDuration() <= 1){
			effectEnding(entityliving);
		}else if ((getDuration() / 20) < 5 && !HasNotified && shouldNotify() && !entityliving.worldObj.isRemote){
			HasNotified = true;
		}
		performEffect(entityliving);
		if (AMCore.proxy != null){
			//run the base
			return super.onUpdate(entityliving);
		}else{
			return true;
		}
	}

	public boolean isReady(int i, int j){
		int k = 25 >> j;
		if (k > 0){
			return i % k == 0;
		}else{
			return true;
		}
	}

	protected abstract String spellBuffName();

	@Override
	public String getEffectName(){
		return String.format("Spell: %s", spellBuffName());
	}
}
