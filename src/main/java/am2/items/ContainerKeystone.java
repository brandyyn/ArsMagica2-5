package am2.items;

import am2.containers.slots.SlotLock;
import am2.containers.slots.SlotRuneOnly;
import am2.items.ItemKeystone.KeystoneCombination;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.HashMap;

public class ContainerKeystone extends Container{
	private final ItemStack keystoneStack;
	private final ItemStack runeBagStack;
	private final InventoryKeyStone keyStoneInventory;
	private final InventoryRuneBag runeBag;
	public final int runebagSlot;
	private final EntityPlayer player;
	public int specialSlotIndex;
	private boolean hasrunebag = false;
	private int PLAYER_INVENTORY_START = 3;
	private int PLAYER_ACTION_BAR_END = 39;

	public ContainerKeystone(InventoryPlayer inventoryplayer, ItemStack bookStack, ItemStack runeBagStack, InventoryKeyStone inventoryKeystone, InventoryRuneBag runeBag, int runeBagSlot){
		this.runeBagStack = runeBagStack;
		this.keyStoneInventory = inventoryKeystone;
		this.keystoneStack = bookStack;
		this.runeBag = runeBag;
		hasrunebag = !(runeBag == null) ;
		this.runebagSlot = runeBagSlot;
		player = inventoryplayer.player;

		int slotIndex = 0;

		//rune slots (clockwise)

		addSlotToContainer(new SlotRuneOnly(keyStoneInventory, slotIndex++, 80, 18));
		addSlotToContainer(new SlotRuneOnly(keyStoneInventory, slotIndex++, 91, 36));
		addSlotToContainer(new SlotRuneOnly(keyStoneInventory, slotIndex, 69, 36));

		//storage slots
		if (hasrunebag){
			int runeSlotIndex = 0;
			for (int i = 0; i < 8; i++){
				for (int j = 0; j < 2; j++){
					addSlotToContainer(new SlotRuneOnly(this.runeBag, runeSlotIndex++, 8 + i * 18, 109 + j * 17));
				}
			}
			PLAYER_INVENTORY_START += 16;
			PLAYER_ACTION_BAR_END += 16;
		}

		int playerInventoryCounter=9;

		int y = runebagSlot > -1 ? 158 : 121;
		//display player inventory
		for (int i = 0; i < 3; i++){
			for (int k = 0; k < 9; k++){
				if (playerInventoryCounter++ == runeBagSlot){
					addSlotToContainer(new SlotLock(inventoryplayer,k + i * 9 + 9, 8 + k * 18, y + i * 18));
					continue;
				}
				addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, y + i * 18));
			}
		}
		playerInventoryCounter =0;
		y = runebagSlot > -1 ? 216 : 179;
		//display player action bar
		for (int j1 = 0; j1 < 9; j1++){
			if (playerInventoryCounter++ == runeBagSlot){
				addSlotToContainer(new SlotLock(inventoryplayer, j1, 8 + j1 * 18, y));
				continue;
			}
			if (inventoryplayer.getStackInSlot(j1) == bookStack){
				addSlotToContainer(new SlotLock(inventoryplayer,j1,8 + j1 * 18, y));
				continue;
			}
			addSlotToContainer(new Slot(inventoryplayer, j1, 8 + j1 * 18, y));
		}

	}

	public ItemStack[] getFullInventory(){
		ItemStack[] stack = new ItemStack[InventoryKeyStone.inventorySize];
		for (int i = 0; i < InventoryKeyStone.inventorySize; ++i){
			stack[i] = ((Slot)inventorySlots.get(i)).getStack();
		}

		return stack;
	}
	public ItemStack[] getRuneBagInventory(){
		ItemStack[] stack = new ItemStack[InventoryRuneBag.inventorySize];
			for (int i = InventoryKeyStone.inventorySize; i < InventoryKeyStone.inventorySize + InventoryRuneBag.inventorySize; ++i){
				stack[i - InventoryKeyStone.inventorySize] = ((Slot)inventorySlots.get(i)).getStack();
			}
		return stack;
	}


	@Override
	public void onContainerClosed(EntityPlayer entityplayer){
		World world = entityplayer.worldObj;
		if (!world.isRemote){
			ItemStack keyStoneItemStack = keystoneStack;
			ItemStack[] items = getFullInventory();
			ItemsCommonProxy.keystone.UpdateStackTagCompound(keyStoneItemStack, items);
			if(hasrunebag){
				ItemStack runeBagInventoryStack = runeBagStack;
				ItemStack[] runeBagItems = getRuneBagInventory();
				ItemsCommonProxy.runeBag.UpdateStackTagCompound(runeBagInventoryStack, runeBagItems);
				entityplayer.inventory.setInventorySlotContents(runebagSlot, runeBagInventoryStack);
			}
			entityplayer.setCurrentItemOrArmor(0,keyStoneItemStack);
		}

		super.onContainerClosed(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return keyStoneInventory.isUseableByPlayer(entityplayer);
	}
	/**
	 * Override slotClick behavior to correctly handle quick-swap access security
	 */
	@Override
	public ItemStack slotClick(int slotId, int keyOrdinal, int clickType, EntityPlayer player) {
		if(clickType == 2 && keyOrdinal >= 0 && keyOrdinal < 9) {
			int hotbarSlotIndex = this.inventorySlots.size() - ( 9 - keyOrdinal);
			Slot hotbarTargetSlot = getSlot(hotbarSlotIndex);
			Slot hoverSlot = getSlot(slotId);
			if(hotbarTargetSlot instanceof SlotLock || hoverSlot instanceof SlotLock) {
				return null;
			}
		}
		return super.slotClick(slotId, keyOrdinal, clickType, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
		ItemStack itemstack = null;
		Slot slot = (Slot)inventorySlots.get(i);
		if (slot != null && slot.getHasStack()){
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < PLAYER_INVENTORY_START)
			{
				if (!mergeItemStack(itemstack1,PLAYER_INVENTORY_START,PLAYER_ACTION_BAR_END,true)){
					return null;
				}
			}
			else if (!mergeItemStack(itemstack1, 3, PLAYER_INVENTORY_START, false)){
				return null;
			}
			if (itemstack1.stackSize == 0){
				slot.putStack(null);
			}else{
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

	public KeystoneCombination getCurrentMatchedCombination(){
		int savedCombos = ItemsCommonProxy.keystone.numCombinations(keystoneStack);
		int[] curMeta = new int[InventoryKeyStone.inventorySize];

		for (int c = 0; c < InventoryKeyStone.inventorySize; ++c){
			ItemStack stack = keyStoneInventory.getStackInSlot(c);
			curMeta[c] = stack != null ? stack.getItemDamage() : -1;
		}

		for (int i = 0; i < savedCombos; ++i){
			KeystoneCombination currentCombo = ItemsCommonProxy.keystone.getCombinationAt(keystoneStack, i);
			if (currentCombo.metas.length < InventoryKeyStone.inventorySize) continue;
			boolean match = true;
			for (int c = 0; c < InventoryKeyStone.inventorySize; ++c){
				match &= curMeta[c] == currentCombo.metas[c];
			}
			if (match)
				return currentCombo;
		}

		return null;
	}

	public ItemStack getKeystoneStack(){
		return keystoneStack;
	}

	public boolean setInventoryToCombination(int comboIndex){

		KeystoneCombination combo = ItemsCommonProxy.keystone.getCombinationAt(keystoneStack, comboIndex);
		if (combo == null) return false;

		if (!inventoryContainsAllMetas(combo.metas)) return false;

		if (player.worldObj.isRemote){
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SET_KEYSTONE_COMBO, new AMDataWriter().add(comboIndex).generate());
			return true;
		}

		int matchIndex = 0;
		int searchIndex = 0;

		while (matchIndex < combo.metas.length && searchIndex < InventoryKeyStone.inventorySize + (hasrunebag ? InventoryRuneBag.inventorySize : 0)){
			IInventory searchInventory = searchIndex >= InventoryKeyStone.inventorySize ? runeBag : keyStoneInventory;
			int inventoryIndex = searchIndex >= InventoryKeyStone.inventorySize ? searchIndex - InventoryKeyStone.inventorySize : searchIndex;

			ItemStack stack = searchInventory.getStackInSlot(inventoryIndex);

			if (stack != null && stack.getItemDamage() == combo.metas[matchIndex]){
				swapInventorySlots(keyStoneInventory, searchInventory, matchIndex, inventoryIndex);
				matchIndex++;
				searchIndex = matchIndex;
				continue;
			}else if (stack == null && combo.metas[matchIndex] == -1){
				swapInventorySlots(keyStoneInventory, searchInventory, matchIndex, inventoryIndex);
				matchIndex++;
				searchIndex = matchIndex;
				continue;
			}
			searchIndex++;
		}

		this.detectAndSendChanges();
		return true;
	}

	private void swapInventorySlots(IInventory firstInventory, IInventory secondInventory, int slot1, int slot2){

		if (firstInventory == secondInventory && slot1 == slot2)
			return;

		ItemStack stack1 = firstInventory.getStackInSlot(slot1);
		ItemStack stack2 = secondInventory.getStackInSlot(slot2);

		firstInventory.setInventorySlotContents(slot1, stack2);
		secondInventory.setInventorySlotContents(slot2, stack1);
	}

	private boolean inventoryContainsAllMetas(int[] metas){
		HashMap<Integer, Integer> metaQuantities = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> invQuantities = new HashMap<Integer, Integer>();

		for (int i : metas){
			if (i == -1) continue;
			if (metaQuantities.containsKey(i)){
				int qty = metaQuantities.get(i);
				metaQuantities.put(i, ++qty);
			}else{
				metaQuantities.put(i, 1);
			}
		}

		for (int i = 0; i < keyStoneInventory.getSizeInventory(); ++i){
			ItemStack stack = keyStoneInventory.getStackInSlot(i);
			if (stack == null) continue;
			int meta = stack.getItemDamage();
			if (invQuantities.containsKey(meta)){
				int qty = invQuantities.get(meta);
				invQuantities.put(meta, ++qty);
			}else{
				invQuantities.put(meta, 1);
			}
		}

		if (runeBag != null){
			for (int i = 0; i < runeBag.getSizeInventory(); ++i){
				ItemStack stack = runeBag.getStackInSlot(i);
				if (stack == null) continue;
				int meta = stack.getItemDamage();
				if (invQuantities.containsKey(meta)){
					int qty = invQuantities.get(meta);
					invQuantities.put(meta, ++qty);
				}else{
					invQuantities.put(meta, 1);
				}
			}
		}

		for (int i : metaQuantities.keySet()){
			if (!invQuantities.containsKey(i)) return false;
			if (invQuantities.get(i) < metaQuantities.get(i)) return false;
		}

		return true;
	}

}
