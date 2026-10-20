package am2.containers;

import am2.api.spell.ItemSpellBase;
import am2.blocks.tileentities.TileEntityBlockCaster;
import am2.containers.slots.SlotFocusOnly;
import am2.containers.slots.SlotSpell;
import am2.items.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerCaster extends Container {
   private TileEntityBlockCaster caster;

   public ContainerCaster(InventoryPlayer inventoryplayer, TileEntityBlockCaster caster) {
      this.caster = caster;
      this.addSlotToContainer(new SlotFocusOnly(caster, 0, 133, 31));
      this.addSlotToContainer(new SlotFocusOnly(caster, 1, 15, 70));
      this.addSlotToContainer(new SlotFocusOnly(caster, 2, 111, 142));
      this.addSlotToContainer(new SlotSpell(caster, 3, 81, 76));

      int j1;
      for(j1 = 0; j1 < 3; ++j1) {
         for(int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(inventoryplayer, k + j1 * 9 + 9, 8 + k * 18, 174 + j1 * 18));
         }
      }

      for(j1 = 0; j1 < 9; ++j1) {
         this.addSlotToContainer(new Slot(inventoryplayer, j1, 8 + j1 * 18, 232));
      }

   }

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i < 4) {
            if (!this.mergeItemStack(itemstack1, 4, 40, true)) {
               return null;
            }
         } else {
            Slot scrollSlot;
            ItemStack castStack;
            Slot focusSlot;
            int availableSlots;
            int b;
            if (i >= 4 && i < 31) {
               if (itemstack1.getItem() instanceof ItemFocus) {
                  if (!(itemstack1.getItem() instanceof ItemFocusCharge) && !(itemstack1.getItem() instanceof ItemFocusMana)) {
                     if (!this.hasCastingFocus()) {
                        for(availableSlots = 0; availableSlots < 3; ++availableSlots) {
                           focusSlot = (Slot)this.inventorySlots.get(availableSlots);
                           if (!focusSlot.getHasStack()) {
                              focusSlot.putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage()));
                              focusSlot.onSlotChanged();
                              --itemstack1.stackSize;
                              if (itemstack1.stackSize == 0) {
                                 slot.putStack((ItemStack)null);
                                 slot.onSlotChanged();
                              }

                              return null;
                           }
                        }
                     }
                  } else {
                     availableSlots = this.numFreeSlots();

                     for(b = 0; b < 3; ++b) {
                        focusSlot = (Slot)this.inventorySlots.get(b);
                        if (!focusSlot.getHasStack()) {
                           if (!this.hasCastingFocus() && availableSlots == 1) {
                              break;
                           }

                           focusSlot.putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage()));
                           focusSlot.onSlotChanged();
                           --itemstack1.stackSize;
                           if (itemstack1.stackSize == 0) {
                              slot.putStack((ItemStack)null);
                              slot.onSlotChanged();
                           }

                           return null;
                        }
                     }
                  }
               } else if (itemstack1.getItem() instanceof ItemSpellBase) {
                  scrollSlot = (Slot)this.inventorySlots.get(3);
                  if (scrollSlot.getHasStack()) {
                     return null;
                  }

                  castStack = new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage());
                  if (itemstack1.hasTagCompound()) {
                     castStack.setTagCompound((NBTTagCompound)itemstack1.stackTagCompound.copy());
                  }

                  scrollSlot.putStack(castStack);
                  scrollSlot.onSlotChanged();
                  --itemstack1.stackSize;
                  if (itemstack1.stackSize == 0) {
                     slot.putStack((ItemStack)null);
                     slot.onSlotChanged();
                  }

                  return null;
               }

               if (!this.mergeItemStack(itemstack1, 31, 40, false)) {
                  return null;
               }
            } else if (i >= 31 && i < 40) {
               if (itemstack1.getItem() instanceof ItemFocus) {
                  if (!(itemstack1.getItem() instanceof ItemFocusCharge) && !(itemstack1.getItem() instanceof ItemFocusMana)) {
                     if (!this.hasCastingFocus()) {
                        for(availableSlots = 0; availableSlots < 3; ++availableSlots) {
                           focusSlot = (Slot)this.inventorySlots.get(availableSlots);
                           if (!focusSlot.getHasStack()) {
                              focusSlot.putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage()));
                              focusSlot.onSlotChanged();
                              --itemstack1.stackSize;
                              if (itemstack1.stackSize == 0) {
                                 slot.putStack((ItemStack)null);
                                 slot.onSlotChanged();
                              }

                              return null;
                           }
                        }
                     }
                  } else {
                     availableSlots = this.numFreeSlots();

                     for(b = 0; b < 3; ++b) {
                        focusSlot = (Slot)this.inventorySlots.get(b);
                        if (!focusSlot.getHasStack()) {
                           if (!this.hasCastingFocus() && availableSlots == 1) {
                              break;
                           }

                           focusSlot.putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage()));
                           focusSlot.onSlotChanged();
                           --itemstack1.stackSize;
                           if (itemstack1.stackSize == 0) {
                              slot.putStack((ItemStack)null);
                              slot.onSlotChanged();
                           }

                           return null;
                        }
                     }
                  }
               } else if (itemstack1.getItem() instanceof ItemSpellBase) {
                  scrollSlot = (Slot)this.inventorySlots.get(3);
                  if (scrollSlot.getHasStack()) {
                     return null;
                  }

                  castStack = new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage());
                  if (itemstack1.hasTagCompound()) {
                     castStack.setTagCompound((NBTTagCompound)itemstack1.stackTagCompound.copy());
                  }

                  scrollSlot.putStack(castStack);
                  scrollSlot.onSlotChanged();
                  --itemstack1.stackSize;
                  if (itemstack1.stackSize == 0) {
                     slot.putStack((ItemStack)null);
                     slot.onSlotChanged();
                  }

                  return null;
               }

               if (!this.mergeItemStack(itemstack1, 4, 30, false)) {
                  return null;
               }
            } else if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
               return null;
            }
         }

         if (itemstack1.stackSize == 0) {
            slot.putStack((ItemStack)null);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.stackSize == itemstack.stackSize) {
            return null;
         }

         slot.onSlotChanged();
      }

      return itemstack;
   }

   private boolean hasCastingFocus() {
      for(int b = 0; b < 3; ++b) {
         Slot focusSlot = (Slot)this.inventorySlots.get(b);
         if (focusSlot.getHasStack()) {
            Item s = focusSlot.getStack().getItem();
            if (s instanceof ItemFocusLesser || s instanceof ItemFocusStandard || s instanceof ItemFocusGreater) {
               return true;
            }
         }
      }

      return false;
   }

   private int numFreeSlots() {
      int freeSlots = 0;

      for(int b = 0; b < 3; ++b) {
         Slot focusSlot = (Slot)this.inventorySlots.get(b);
         if (!focusSlot.getHasStack()) {
            ++freeSlots;
         }
      }

      return freeSlots;
   }

   public boolean canInteractWith(EntityPlayer entityplayer) {
      return this.caster.isUseableByPlayer(entityplayer);
   }
}
