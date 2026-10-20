package am2.items;

import am2.api.items.IManaContainerItem;
import am2.api.items.ManaItemHandler;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemManaStone extends ArsMagicaItem implements IManaContainerItem{

	private static final String KEY_NBT_MANA = "Stored_Mana";

	public ItemManaStone(){
		super();
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass){
		return true;
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4){
		String[] strings = StatCollector.translateToLocal("am2.tooltip.stoneUse").split(" ");
		String firstHalf = "";
		String secondHalf = "";
		for (int i = 0; i < strings.length; i++) {
			if (i < strings.length / 2) firstHalf += strings[i] + " ";
			else secondHalf += strings[i] + " ";
		}
		list.add(String.format(StatCollector.translateToLocal("am2.tooltip.containedMana"), getMana(stack)));
		list.add(firstHalf);
		list.add(secondHalf);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stone, World world, EntityPlayer player){

		if (player.isSneaking() && !ManaItemHandler.isFull(stone)){
			if (ExtendedProperties.For(player).getCurrentMana() > 50){
				ExtendedProperties.For(player).setCurrentMana(ExtendedProperties.For(player).getCurrentMana() - 50);
				if (!player.worldObj.isRemote) ManaItemHandler.AddMana(stone, 50);
			}
		}else{
			float amtp = Math.min(getMana(stone), 50);
			float amt = Math.min(ExtendedProperties.For(player).getMaxMana() - ExtendedProperties.For(player).getCurrentMana(), amtp);
			if (amt > 0){
				ExtendedProperties.For(player).setCurrentMana(ExtendedProperties.For(player).getCurrentMana()+amt);
				if (!player.worldObj.isRemote) ManaItemHandler.RemoveMana(stone, (int)amt);
			}
		}

		return super.onItemRightClick(stone, world, player);
	}
	public float getMana(ItemStack item){
		if (!item.hasTagCompound())
			return 0;
		return item.stackTagCompound.getFloat(KEY_NBT_MANA);
	}
	public float getMaxMana(){
		return 1000;
	}


}
