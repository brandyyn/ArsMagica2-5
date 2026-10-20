package am2.spell.components;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.ArsMagicaApi;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffList;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;

public class Deafen implements ISpellComponent, IRitualInteraction {

    @Override
    public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
        return false;
    }

    @Override
    public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
        if (target instanceof EntityPlayer){
            int duration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, target, world, 0, SpellModifiers.DURATION);
            duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
            int x = (int)Math.floor(target.posX);
            int y = (int)Math.floor(target.posY);
            int z = (int)Math.floor(target.posZ);
            if (RitualShapeHelper.instance.checkForRitual(this, world, x, y, z) != null){
                duration += (3600 * (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 1));
                RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
            }

            target.worldObj.playSoundAtEntity(target, "ambient.weather.thunder",3F, 1F);
            AMDataWriter writer1 = new AMDataWriter();
            writer1.add(duration);
            if (!world.isRemote) AMNetHandler.INSTANCE.sendPacketToAllClientsNear(world.provider.dimensionId, x, y, z, 3, AMPacketIDs.DEAFEN, writer1.generate());
            return true;
        }
        return false;
    }

    @Override
    public float manaCost(EntityLivingBase caster){
        return 60;
    }

    @Override
    public float burnout(EntityLivingBase caster){
        return ArsMagicaApi.getBurnoutFromMana(manaCost(caster));
    }

	@Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
        for (int i = 0; i < 15; ++i){
            AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "lens_flare", x, y, z);
            if (particle != null){
                particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f, 1, false).SetTargetDistance(rand.nextDouble() + 0.5));
                particle.setMaxAge(25 + rand.nextInt(10));
                particle.setRGBColorF(0, 0, 0);
                if (colorModifier > -1){
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public EnumSet<Affinity> getAffinity(){
        return EnumSet.of(Affinity.LIGHTNING, Affinity.LIFE);
    }

    @Override
    public int getID(){
        return 98;
    }

    @Override
    public Object[] getRecipeItems(){
        return new Object[]{
                new ItemStack(ItemsCommonProxy.rune, 1, ItemRune.META_BROWN),
                new ItemStack(ItemsCommonProxy.rune, 1, ItemRune.META_WHITE),
                new ItemStack(ItemsCommonProxy.itemOre, 1, ItemOre.META_ANIMALFAT),
                new ItemStack(Items.record_11)
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity){
        return 0.05f;
    }

    @Override
    public MultiblockStructureDefinition getRitualShape(){
        return RitualShapeHelper.instance.hourglass;
    }

    @Override
    public ItemStack[] getReagents(){
        return new ItemStack[]{
                new ItemStack(Items.blaze_powder),
                new ItemStack(Items.bone),
                new ItemStack(Items.poisonous_potato)
        };
    }

    @Override
    public int getReagentSearchRadius(){
        return 3;
    }
}