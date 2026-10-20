package am2.mixin.early.client;

import am2.buffs.BuffList;
import am2.utility.AMHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class MixinEntityClient{

	/** True sight and ethereal form see invisible entities. */
	@ModifyReturnValue(method = "isInvisibleToPlayer", at = @At("RETURN"))
	private boolean am2$trueSight(boolean invisible, @Local(argsOnly = true) EntityPlayer viewer){
		return invisible && !(viewer.isPotionActive(BuffList.trueSight.id) || AMHooks.isPlayerEthereal(viewer));
	}
}
