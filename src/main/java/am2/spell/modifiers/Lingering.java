package am2.spell.modifiers;

import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemsCommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Lingering implements ISpellModifier {

	@Override
	public int getID(){
		return 25;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				Items.slime_ball,
				Items.magma_cream,
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_CHIMERITE),
				new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_STORMSAWTOOTH),
				Items.compass
		};
	}

	@Override
	public EnumSet<SpellModifiers> getAspectsModified(){
		return EnumSet.of(SpellModifiers.LINGERING);
	}

	@Override
	public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, byte[] metadata){
		return 1;
	}

	@Override
	public float getManaCostMultiplier(ItemStack spellStack, int stage, int quantity, EntityLivingBase caster){
		return 2F * quantity;
	}

	@Override
	public byte[] getModifierMetadata(ItemStack[] matchedRecipe){
		return null;
	}
}
