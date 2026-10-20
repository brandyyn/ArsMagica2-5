package am2.mixin.early;

import am2.utility.AMHooks;
import am2.utility.EntityUtilities;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld{

	/** Ethereal players are invisible to entity queries (AI targeting, collisions, area spells). */
	@ModifyReturnValue(method = "selectEntitiesWithinAABB", at = @At("RETURN"))
	private List am2$hideEtherealSelected(List entities){
		return AMHooks.withoutEtherealPlayers(entities);
	}

	@ModifyReturnValue(method = "getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/command/IEntitySelector;)Ljava/util/List;", at = @At("RETURN"))
	private List am2$hideEtherealExcluding(List entities){
		return AMHooks.withoutEtherealPlayers(entities);
	}

	@ModifyReturnValue(method = "getClosestPlayer", at = @At("RETURN"))
	private EntityPlayer am2$hideEtherealClosest(EntityPlayer player){
		return AMHooks.isPlayerEthereal(player) ? null : player;
	}

	/** Redstone Signal spell; redstone queries this constantly, so no callback object is allocated. */
	@ModifyReturnValue(method = "isBlockIndirectlyGettingPowered", at = @At("RETURN"))
	private boolean am2$spellRedstone(boolean powered, @Local(ordinal = 0, argsOnly = true) int x, @Local(ordinal = 1, argsOnly = true) int y, @Local(ordinal = 2, argsOnly = true) int z){
		return powered || AMHooks.consumeProvidedRedstone((World)(Object)this, x, y, z);
	}

	/** Shrunk entities sound higher pitched. */
	@Redirect(method = "playSoundAtEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorldAccess;playSound(Ljava/lang/String;DDDFF)V"))
	private void am2$shrinkPitch(IWorldAccess access, String sound, double x, double y, double z, float volume, float pitch, Entity entity, String name, float originalVolume, float originalPitch){
		access.playSound(sound, x, y, z, volume, EntityUtilities.modifySoundPitch(entity, pitch));
	}
}
