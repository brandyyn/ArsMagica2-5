package am2.items;

import am2.armor.BoundArmor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/**
 * Shapeless crafting of spell staves and bound armour, checked before every other recipe:
 * <ul>
 * <li>staff + spell: the spell is put on the staff;</li>
 * <li>staff + staff: the first staff takes the charge of the second;</li>
 * <li>bound armour + spell: the spell is bound to the armour.</li>
 * </ul>
 * This used to be a patch on CraftingManager.findMatchingRecipe that kept the matched slots in static fields shared by
 * the client and server threads.
 */
public class SpellAttachmentRecipe implements IRecipe{

	private static final int SPELL_ON_STAFF = 1, COMBINE_STAFFS = 2, SPELL_ON_ARMOR = 3;

	private static final class Match{
		int kind;
		ItemStack target, secondStaff, spell;
	}

	@Override
	public boolean matches(InventoryCrafting grid, World world){
		return match(grid) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting grid){
		Match m = match(grid);
		if (m == null)
			return null;
		switch (m.kind){
		case SPELL_ON_STAFF:
			return ItemSpellStaff.setSpellScroll(m.target.copy(), m.spell);
		case COMBINE_STAFFS:
			return ItemSpellStaff.copyChargeFrom(m.target.copy(), m.secondStaff);
		default:
			return BoundArmor.setSpell(m.target.copy(), m.spell);
		}
	}

	private static Match match(InventoryCrafting grid){
		ItemStack firstStaff = null, secondStaff = null, spell = null, armor = null;
		int staffs = 0, spells = 0, armors = 0;
		for (int i = 0; i < grid.getSizeInventory(); i++){
			ItemStack stack = grid.getStackInSlot(i);
			if (stack == null)
				continue;
			if (stack.getItem() instanceof ItemSpellStaff){
				if (((ItemSpellStaff)stack.getItem()).isMagiTechStaff())
					continue;
				staffs++;
				if (firstStaff == null)
					firstStaff = stack;
				else
					secondStaff = stack;
			}else if (stack.getItem() instanceof SpellBase){
				spells++;
				spell = stack;
			}else if (stack.getItem() instanceof BoundArmor){
				armors++;
				armor = stack;
			}
		}
		Match m = new Match();
		m.secondStaff = secondStaff;
		m.spell = spell;
		if (spells == 1 && staffs == 1){
			m.kind = SPELL_ON_STAFF;
			m.target = firstStaff;
		}else if (staffs == 2){
			m.kind = COMBINE_STAFFS;
			m.target = firstStaff;
		}else if (spells == 1 && armors == 1){
			m.kind = SPELL_ON_ARMOR;
			m.target = armor;
		}else{
			return null;
		}
		return m;
	}

	@Override
	public int getRecipeSize(){
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput(){
		return null;
	}
}
