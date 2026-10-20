package am2.mixin.early;

import am2.utility.AMHooks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer{

	@Shadow
	public EntityPlayerMP playerEntity;

	/** Ethereal players cannot move items; the client is resynced instead. */
	@Inject(method = "processClickWindow", at = @At("HEAD"), cancellable = true)
	private void am2$etherealInventoryLock(C0EPacketClickWindow packet, CallbackInfo ci){
		if (!AMHooks.isPlayerEthereal(playerEntity))
			return;
		playerEntity.func_143004_u();
		if (playerEntity.openContainer.windowId == packet.func_149548_c() && playerEntity.openContainer.isPlayerNotUsingContainer(playerEntity)){
			ArrayList<ItemStack> contents = new ArrayList<ItemStack>();
			for (int i = 0; i < playerEntity.openContainer.inventorySlots.size(); ++i)
				contents.add(((Slot)playerEntity.openContainer.inventorySlots.get(i)).getStack());
			playerEntity.sendContainerAndContentsToPlayer(playerEntity.openContainer, contents);
			ci.cancel();
		}
	}

	/**
	 * Shrunk and flipped players have eye heights outside vanilla's accepted stance range, so the "Illegal stance"
	 * kick is disabled (it used to force the computed stance to 1.5).
	 */
	@ModifyConstant(method = "processPlayer", constant = @Constant(doubleValue = 1.65D))
	private double am2$maxStance(double original){
		return Double.MAX_VALUE;
	}

	@ModifyConstant(method = "processPlayer", constant = @Constant(doubleValue = 0.1D))
	private double am2$minStance(double original){
		return -Double.MAX_VALUE;
	}
}
