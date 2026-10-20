package am2.blocks;

import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.ArrayList;



public class AMCosmicRock extends AMBlock{
	public AMCosmicRock(Material par2Material){
		super(par2Material);
	}


	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune){
		ArrayList<ItemStack> drops = new ArrayList<>();
		if(world.rand.nextInt(2) % 2 == 0)
			drops.add(new ItemStack(ItemsCommonProxy.itemOre,world.rand.nextInt(4) + 1,ItemOre.META_COSMICDUST));
		else
			drops.add(new ItemStack(ItemsCommonProxy.itemOre,world.rand.nextInt(3)+ 1,ItemOre.META_MOONSTONEFRAGMENT));
		return drops;
	}
}
