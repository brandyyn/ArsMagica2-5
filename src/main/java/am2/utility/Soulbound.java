package am2.utility;

import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.enchantments.AMEnchantments;
import am2.playerextensions.ExtendedProperties;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ListIterator;

/**
 * Keeps soulbound items (the Soulbound enchantment, or the soulbound armour imbuement) through death, in the slot they
 * were in.
 * <p>
 * Flow, compatible with grave mods that leave soulbound items in the inventory (GraveStone's Ars Magica compat):
 * <ol>
 * <li>LivingDeathEvent, lowest priority (after graves took the rest): remember the slot of each soulbound stack.</li>
 * <li>PlayerDropsEvent, highest priority: vanilla dropped those same stack objects; each is taken out of the drops and
 * put back into the dead player's slot, so nothing else can pick it up from the drops.</li>
 * <li>Clone (respawn after death): each soulbound stack is moved to the same slot of the new player and removed from the
 * old one, in any dimension.</li>
 * </ol>
 * Every step moves the one stack object and clears where it came from, so no step can leave a copy behind.
 */
public class Soulbound{

	/** Slot index used for armour: mainInventory.length + armour slot. */
	private static final int NO_SLOT = -1;

	public static boolean isSoulbound(ItemStack stack){
		return stack != null && (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound.effectId, stack) > 0
				|| ArmorHelper.isInfusionPreset(stack, GenericImbuement.soulbound));
	}

	private static boolean appliesTo(EntityPlayer player){
		return player != null && !(player instanceof FakePlayer) && !player.worldObj.isRemote
				&& !player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory");
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDeath(LivingDeathEvent event){
		if (!(event.entityLiving instanceof EntityPlayer))
			return;
		EntityPlayer player = (EntityPlayer)event.entityLiving;
		if (!appliesTo(player))
			return;
		ExtendedProperties props = ExtendedProperties.For(player);
		props.soulboundSlots = null;
		InventoryPlayer inventory = player.inventory;
		for (int i = 0; i < inventory.mainInventory.length + inventory.armorInventory.length; i++){
			ItemStack stack = inventory.getStackInSlot(i);
			if (isSoulbound(stack)){
				if (props.soulboundSlots == null){
					props.soulboundSlots = new Reference2IntOpenHashMap<ItemStack>();
					props.soulboundSlots.defaultReturnValue(NO_SLOT);
				}
				props.soulboundSlots.put(stack, i);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDrops(PlayerDropsEvent event){
		EntityPlayer player = event.entityPlayer;
		if (event.isCanceled() || !appliesTo(player))
			return;
		ExtendedProperties props = ExtendedProperties.For(player);
		Reference2IntOpenHashMap<ItemStack> slots = props.soulboundSlots;
		props.soulboundSlots = null;
		for (ListIterator<EntityItem> it = event.drops.listIterator(); it.hasNext(); ){
			ItemStack stack = it.next().getEntityItem();
			if (!isSoulbound(stack))
				continue;
			int slot = slots == null ? NO_SLOT : slots.getInt(stack);
			if (put(player.inventory, stack, slot))
				it.remove();
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.Clone event){
		if (!event.wasDeath || event.original == null || !appliesTo(event.entityPlayer))
			return;
		InventoryPlayer from = event.original.inventory;
		for (int i = 0; i < from.mainInventory.length + from.armorInventory.length; i++){
			ItemStack stack = from.getStackInSlot(i);
			if (isSoulbound(stack) && put(event.entityPlayer.inventory, stack, i))
				from.setInventorySlotContents(i, null);
		}
	}

	/** Puts the stack into its slot if free, else armour into its armour slot, else the first free main slot. */
	private static boolean put(InventoryPlayer inventory, ItemStack stack, int preferredSlot){
		int size = inventory.mainInventory.length + inventory.armorInventory.length;
		if (preferredSlot >= 0 && preferredSlot < size && inventory.getStackInSlot(preferredSlot) == null){
			inventory.setInventorySlotContents(preferredSlot, stack);
			return true;
		}
		if (stack.getItem() instanceof net.minecraft.item.ItemArmor){
			// armorType 0 is the helmet, stored in armour slot 3
			int armorSlot = inventory.mainInventory.length + 3 - ((net.minecraft.item.ItemArmor)stack.getItem()).armorType;
			if (inventory.getStackInSlot(armorSlot) == null){
				inventory.setInventorySlotContents(armorSlot, stack);
				return true;
			}
		}
		for (int i = 0; i < inventory.mainInventory.length; i++){
			if (inventory.mainInventory[i] == null){
				inventory.mainInventory[i] = stack;
				return true;
			}
		}
		return false;
	}
}
