package am2.containers.slots;

import am2.EnervatorRecipeHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotEnervator extends Slot{

	public SlotEnervator(IInventory par1iInventory, int par2, int par3, int par4){
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack){
		if (EnervatorRecipeHelper.instance.getRecipe(par1ItemStack) != null) return true;
		return false;
	}

	@Override
	public int getSlotStackLimit(){
		return 1;
	}
}
