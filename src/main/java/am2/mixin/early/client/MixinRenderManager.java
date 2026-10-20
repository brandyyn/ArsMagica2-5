package am2.mixin.early.client;

import am2.entities.renderers.RenderPlayerSpecial;
import am2.utility.AMHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager{

	/** One shared renderer; it used to be constructed for every render call. */
	@Unique
	private RenderPlayerSpecial am2$maidRenderer;

	/** Players disguised by the Box of Illusions with a maid model. */
	@ModifyReturnValue(method = "getEntityRenderObject", at = @At("RETURN"))
	private Render am2$maidModel(Render render, @Local(argsOnly = true) Entity entity){
		if (!AMHooks.hasMaidModel(entity))
			return render;
		if (am2$maidRenderer == null){
			am2$maidRenderer = new RenderPlayerSpecial();
			am2$maidRenderer.setRenderManager((RenderManager)(Object)this);
		}
		return am2$maidRenderer;
	}
}
