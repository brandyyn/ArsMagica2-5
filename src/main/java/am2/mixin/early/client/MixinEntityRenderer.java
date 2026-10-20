package am2.mixin.early.client;

import am2.guis.AMGuiHelper;
import am2.utility.AMHooks;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer{

	@Shadow
	private Minecraft mc;

	/** Camera shift for shrunk and flipped players. */
	@Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V", shift = At.Shift.AFTER))
	private void am2$shiftView(float partialTicks, int pass, CallbackInfo ci){
		AMGuiHelper.shiftView(partialTicks);
	}

	/** Camera roll for players flipped by ender boots. */
	@Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", ordinal = 0, shift = At.Shift.AFTER, remap = false))
	private void am2$flipView(float partialTicks, int pass, CallbackInfo ci){
		AMGuiHelper.flipView(partialTicks);
	}

	/** Reversed mouse input (scramble synapses, flipped view). */
	@ModifyVariable(method = "updateCameraAndRender", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", args = "ldc=mouse", shift = At.Shift.AFTER), ordinal = 0)
	private boolean am2$overrideMouse(boolean displayActive, float partialTicks){
		return AMGuiHelper.overrideMouseInput((EntityRenderer)(Object)this, partialTicks, displayActive);
	}

	/** The third person camera of an ethereal player is not pulled in by walls. */
	@ModifyExpressionValue(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
	private double am2$etherealCamera(double distance){
		return mc.renderViewEntity == mc.thePlayer && AMHooks.isPlayerEthereal(mc.thePlayer) ? Double.MAX_VALUE : distance;
	}
}
