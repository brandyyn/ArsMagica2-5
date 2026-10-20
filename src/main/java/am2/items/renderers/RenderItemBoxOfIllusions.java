package am2.items.renderers;

import am2.utility.AMHooks;

import am2.blocks.tileentities.TileEntityCelestialPrism;
import am2.items.ItemsCommonProxy;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.texture.ResourceManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.obj.WavefrontObject;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;

@SideOnly(Side.CLIENT)
public class RenderItemBoxOfIllusions implements IItemRenderer {

    IModelCustom model;
    ResourceLocation texture1 = new ResourceLocation("arsmagica2", "obj/boxOfIllusions.png");
    private ResourceLocation rLoc_celestial;
    private final WavefrontObject model_celestial;

    public RenderItemBoxOfIllusions() {
        model =  AdvancedModelLoader.loadModel(new ResourceLocation("arsmagica2", "obj/makeupboxjoined.obj"));
        model_celestial = (WavefrontObject)AdvancedModelLoader.loadModel(ResourceManager.getOBJFilePath("celestial_prism.obj"));
        rLoc_celestial = new ResourceLocation("arsmagica2", ResourceManager.getCustomBlockTexturePath("fractal.png"));
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    private void renderCelestial(TileEntityCelestialPrism tile, double d, double d1, double d2, float f){
        Minecraft.getMinecraft().renderEngine.bindTexture(rLoc_celestial);
        GL11.glScalef(0.7f, 0.7f, 0.7f);
        GL11.glTranslated(0.1, 0.1, 0.05);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        RenderHelper.disableStandardItemLighting();
        Tessellator tessellator = Tessellator.instance;

        try{
            model_celestial.renderAll();
        }catch (Throwable t){

        }

        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    public static boolean doRotations = false;
    public static int rotationTick = 0;

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture1);

        if (type == EQUIPPED_FIRST_PERSON) { // yes, I know switches exist. Don't want them.
            if (cooldown > 0) cooldown--;
            GL11.glPushMatrix();
            GL11.glScalef(1f, 1f, 1f);
            GL11.glRotated(130, 0, -1 ,0);
            GL11.glTranslated(-0.02, 0.5, -0.6);
            GL11.glPushMatrix();
            if (doRotations) {
                rotationTick++;
                GL11.glRotated(rotationTick*3, 0, 1 ,0);
                if (rotationTick > 120) {
                    doRotations = false;
                    rotationTick = 0;
                    setCustomRenderer();
                }
            }
            model.renderAll();
            renderCelestial(new TileEntityCelestialPrism(), 1, 1,1, 1);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        } else if (type == INVENTORY) {
            GL11.glPushMatrix();
            GL11.glScalef(1.2f, 1.2f, 1.2f);
            GL11.glTranslated(0.02, -0.4, 0.02);
            GL11.glPushMatrix();
            model.renderAll();
            renderCelestial(new TileEntityCelestialPrism(), 1, 1,1, 1);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        } else if (type == EQUIPPED) { // third person
            if (cooldown > 0) cooldown--;
            GL11.glPushMatrix();
            GL11.glScalef(1.1f, 1.1f, 1.1f);
            GL11.glTranslated(0.415, 0.4, 0.615);
            GL11.glPushMatrix();
            if (doRotations) {
                rotationTick++;
                GL11.glRotated(rotationTick*3, 0, 1 ,0);
                if (rotationTick > 120) {
                    doRotations = false;
                    rotationTick = 0;
                    setCustomRenderer();
                }
            }
            model.renderAll();
            renderCelestial(new TileEntityCelestialPrism(), 1, 1,1, 1);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        } else {
            GL11.glPushMatrix();
            GL11.glScalef(1, 1, 1);
            GL11.glPushMatrix();
            model.renderAll();
            renderCelestial(new TileEntityCelestialPrism(), 1, 1,1, 1);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
    }

    private int cooldown = 0; // to prevent glitches in case calling would happen twice

    private void setCustomRenderer() {
        if (cooldown > 0) return;
        EntityClientPlayerMP theplayer = Minecraft.getMinecraft().thePlayer;
        String player = theplayer.getCommandSenderName();

        if (AMHooks.playerModelMap.get(player) != null) {
            AMHooks.playerModelMap.remove(player);
            return;
        }
        for (ItemStack stack : theplayer.inventory.mainInventory){
            if (stack != null) {

                if (stack.getItem() == Items.spider_eye) {
                    AMHooks.playerModelMap.put(player, "spider");
                    return;
                } else if (stack.getItem() == Items.rotten_flesh) {
                    AMHooks.playerModelMap.put(player, "zombie");
                    return;
                } else if (stack.getItem() == Items.glass_bottle) {
                    AMHooks.playerModelMap.put(player, "witch");
                    return;
                } else if (stack.getItem() == Items.snowball) {
                    AMHooks.playerModelMap.put(player, "snowman");
                    return;
                } else if (stack.getItem() == Items.gunpowder) {
                    AMHooks.playerModelMap.put(player, "creeper");
                    return;
                } else if (stack.getItem() == Items.feather) {
                    AMHooks.playerModelMap.put(player, "chicken");
                    return;
                } else if (stack.getItem() == Items.leather) {
                    AMHooks.playerModelMap.put(player, "cow");
                    return;
                } else if (stack.getItem() == Items.ender_pearl) {
                    AMHooks.playerModelMap.put(player, "enderman");
                    return;
                }else if (stack.getItem() == Items.bone) {
                    AMHooks.playerModelMap.put(player, "skeleton");
                    return;
                }else if (stack.getItem() == Items.magma_cream) {
                    AMHooks.playerModelMap.put(player, "magma");
                    return;
                }else if (stack.getItem() == Items.blaze_rod) {
                    AMHooks.playerModelMap.put(player, "blaze");
                    return;
                }else if (stack.getItem() == Items.gold_nugget) {
                    AMHooks.playerModelMap.put(player, "pigman");
                    return;
                }else if (stack.getItem() == Items.saddle) {
                    AMHooks.playerModelMap.put(player, "horse");
                    return;
                }else if (stack.getItem() == ItemsCommonProxy.essence) {
                    int damage = stack.getItemDamage();
                    switch(damage){
                    case 0 : AMHooks.playerModelMap.put(player, "arcane");
                        return;
                    case 1 : AMHooks.playerModelMap.put(player, "earth");
                        return;
                    case 2 : AMHooks.playerModelMap.put(player, "air");
                        return;
                    case 3 : AMHooks.playerModelMap.put(player, "fire");
                        return;
                    case 4 : AMHooks.playerModelMap.put(player, "water");
                        return;
                    case 5 : AMHooks.playerModelMap.put(player, "nature");
                        return;
                    case 6 : AMHooks.playerModelMap.put(player, "ice");
                        return;
                    case 7 : AMHooks.playerModelMap.put(player, "lightning");
                        return;
                    case 8 : AMHooks.playerModelMap.put(player, "life");
                        return;
                    case 9 : AMHooks.playerModelMap.put(player, "end");
                        return;
                    }
                }else if (stack.getItem() == ItemsCommonProxy.evilBook) {
                    AMHooks.playerModelMap.put(player, "maid" + Minecraft.getMinecraft().theWorld.rand.nextInt(8));
                    return;
                }
            }
        }
        cooldown = 80;
        AMDataWriter writer = new AMDataWriter();
        writer.add(AMHooks.playerModelMap.size());
        for (Map.Entry<String, String> entry : AMHooks.playerModelMap.entrySet()) {
            writer.add(entry.getKey());
            writer.add(entry.getValue());
        }
        AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SYNCMAPTOSERVER, writer.generate());
    }

}
