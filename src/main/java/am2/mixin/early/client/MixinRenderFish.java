package am2.mixin.early.client;

import am2.items.ItemArcaneFishingRod;
import am2.items.ItemInfernalFishingRod;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderFish.class)
public abstract class MixinRenderFish extends Render{

	@Unique
	private static final ResourceLocation am2$arcaneRodTexture = new ResourceLocation("arsmagica2", "textures/items/particles/particleswitharcanerod.png");
	@Unique
	private static final ResourceLocation am2$infernalRodTexture = new ResourceLocation("arsmagica2", "textures/items/particles/particleswithinfernalrod.png");

	/** Fishing hooks of AM2 rods use the texture of the rod being held. */
	@ModifyReturnValue(method = "getEntityTexture(Lnet/minecraft/entity/projectile/EntityFishHook;)Lnet/minecraft/util/ResourceLocation;", at = @At("RETURN"))
	private ResourceLocation am2$rodTexture(ResourceLocation texture){
		ItemStack held = Minecraft.getMinecraft().thePlayer == null ? null : Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (held == null)
			return texture;
		Item item = held.getItem();
		if (item instanceof ItemArcaneFishingRod)
			return am2$arcaneRodTexture;
		if (item instanceof ItemInfernalFishingRod)
			return am2$infernalRodTexture;
		return texture;
	}

	/** Hooks draw no shadow or fire (it flickered). */
	@Override
	public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float yaw, float partialTicks){
	}
}
