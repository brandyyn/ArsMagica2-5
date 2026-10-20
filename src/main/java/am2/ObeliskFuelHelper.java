package am2;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import am2.api.power.IObeliskFuelHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class ObeliskFuelHelper implements IObeliskFuelHelper{
	private final Reference2IntLinkedOpenHashMap<ItemStack> validFuels;

	public static final ObeliskFuelHelper instance = new ObeliskFuelHelper();

	private ObeliskFuelHelper(){
		validFuels = new Reference2IntLinkedOpenHashMap<ItemStack>();
	}

	@Override
	public void registerFuelType(ItemStack stack, int burnTime){
		stack.stackSize = 0;
		validFuels.put(stack.copy(), burnTime);
	}

	@Override
	public int getFuelBurnTime(ItemStack stack){
		if (stack == null)
			return 0;

		for (Reference2IntMap.Entry<ItemStack> fuel : validFuels.reference2IntEntrySet()){
			ItemStack possibleFuel = fuel.getKey();
			if (stack.getItem() == possibleFuel.getItem() && (possibleFuel.getItemDamage() == Short.MAX_VALUE || possibleFuel.getItemDamage() == stack.getItemDamage()))
				return fuel.getIntValue();
		}
		return 0;
	}
}
