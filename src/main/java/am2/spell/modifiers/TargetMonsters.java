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

public class TargetMonsters implements ISpellModifier{
	@Override
	public int getID(){
		return 23;
	}

	@Override
	public Object[] getRecipeItems(){
		return new Object[]{
				ItemsCommonProxy.mobFocus,
				Items.arrow,
				Items.bone,
				Items.rotten_flesh,
				Items.gold_nugget
		};
	}

	@Override
	public EnumSet<SpellModifiers> getAspectsModified(){
		return EnumSet.of(SpellModifiers.TARGET_MONSTERS);
	}

	@Override
	public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, byte[] metadata){
		return 1;
	}

	@Override
	public float getManaCostMultiplier(ItemStack spellStack, int stage, int quantity, EntityLivingBase caster){
		return 1.05f * quantity;
	}

	@Override
	public byte[] getModifierMetadata(ItemStack[] matchedRecipe){
		return null;
	}
}
