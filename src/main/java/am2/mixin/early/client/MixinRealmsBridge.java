package am2.mixin.early.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.realms.RealmsBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsBridge.class)
public abstract class MixinRealmsBridge{

	/** Realms crashes the 1.7.10 client; the button does nothing instead. */
	@Inject(method = "switchToRealms", at = @At("HEAD"), cancellable = true, remap = false)
	private void am2$noRealms(GuiScreen screen, CallbackInfo ci){
		ci.cancel();
	}
}
