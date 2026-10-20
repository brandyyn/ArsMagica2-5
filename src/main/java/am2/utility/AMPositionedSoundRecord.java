package am2.utility;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class AMPositionedSoundRecord extends MovingSound{
	public AMPositionedSoundRecord(ResourceLocation p_i45103_1_,float volume, float pitch, boolean repeat,float posx,float posy,float posz, int repeatdelay, AttenuationType type){
		super(p_i45103_1_);
		this.volume = volume;
		this.field_147663_c = pitch;
		this.xPosF = posx;
		this.yPosF = posy;
		this.zPosF = posz;
		this.repeat = repeat;
		this.field_147665_h = repeatdelay;
		this.field_147666_i = type;
	}

	@Override
	public void update(){
	}
	public void setDonePlaying(){
		this.donePlaying = true;
	}
}
