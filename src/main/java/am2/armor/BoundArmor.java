package am2.armor;

import am2.api.spell.enums.ContingencyTypes;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class BoundArmor extends AMArmor{

	private static final String NBT_SPELL = "spell_to_cast";
	private static final String NBT_SPELL_NAME = "spell_name";

	public BoundArmor(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, int par4){
		super(inheritFrom, enumarmormaterial, par3, par4);
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack){
		if (player.ticksExisted % 100 == 0){
			ExtendedProperties p = ExtendedProperties.For(player);
			if (getSpellStack(itemStack) != null && p.getContingencyEffect(
					itemStack.getUnlocalizedName().contains("helm") ? 1 :
						itemStack.getUnlocalizedName().contains("chest") ? 4 :
						itemStack.getUnlocalizedName().contains("leg") ? 0 :
						itemStack.getUnlocalizedName().contains("boot") ? 2 : 3
			).getItem() instanceof ItemSnowball) {
				p.setContingency(itemStack.getUnlocalizedName().contains("helm") ? ContingencyTypes.DEATH :
						itemStack.getUnlocalizedName().contains("chest") ? ContingencyTypes.HEALTH_LOW :
						itemStack.getUnlocalizedName().contains("leg") ? ContingencyTypes.DAMAGE_TAKEN :
						itemStack.getUnlocalizedName().contains("boot") ? ContingencyTypes.FALL : ContingencyTypes.NONE, getSpellStack(itemStack));
			}
		}
	}

	public static ItemStack setSpell(ItemStack stack, ItemStack spell){
		if (stack.stackTagCompound == null){
			stack.stackTagCompound = new NBTTagCompound();
		}
		NBTTagCompound compound = stack.stackTagCompound;
		NBTTagCompound spellCompound = spell.writeToNBT(new NBTTagCompound());
		compound.setTag(NBT_SPELL, spellCompound);
		compound.setString(NBT_SPELL_NAME, spell.getDisplayName());
		return stack;
	}

	private ItemStack getSpellStack(ItemStack staffStack){
		if (!staffStack.hasTagCompound() || !staffStack.stackTagCompound.hasKey(NBT_SPELL))
			return null;
		ItemStack stack = new ItemStack(ItemsCommonProxy.spell);
		stack.readFromNBT(staffStack.getTagCompound().getCompoundTag(NBT_SPELL));
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		String name = super.getItemStackDisplayName(par1ItemStack);
		if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey(NBT_SPELL_NAME))
			name += " (\2479" + par1ItemStack.getTagCompound().getString(NBT_SPELL_NAME) + "\2477)";
		return name;
	}
}
