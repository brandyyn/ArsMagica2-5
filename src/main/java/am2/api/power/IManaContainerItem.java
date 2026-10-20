//package am2.api.power;
//
//import net.minecraft.item.ItemStack;
//
///**
// * Implement this interface on Item classes that support external manipulation of their internal
// * mana storage.
// *
// * A reference implementation is provided {@link ItemManaContainer}.
// *
// * NOTE: Use of NBT data on the containing ItemStack is encouraged.
// *
// * @author King Lemming
// *
// */
//public interface IManaContainerItem
//{
//    /**
//     *
//     * @param container
//     *            ItemStack which is the mana container.
//     * @return ManaStack representing the mana in the container, null if the container is empty.
//     */
//    ManaStack getMana(ItemStack container);
//
//    /**
//     *
//     * @param container
//     *            ItemStack which is the mana container.
//     * @return Capacity of this mana container.
//     */
//    int getCapacity(ItemStack container);
//
//    /**
//     *
//     * @param container
//     *            ItemStack which is the mana container.
//     * @param resource
//     *            ManaStack attempting to fill the container.
//     * @param doFill
//     *            If false, the fill will only be simulated.
//     * @return Amount of mana that was (or would have been, if simulated) filled into the
//     *         container.
//     */
//    int fill(ItemStack container, ManaStack resource, boolean doFill);
//
//    /**
//     *
//     * @param container
//     *            ItemStack which is the mana container.
//     * @param maxDrain
//     *            Maximum amount of mana to be removed from the container.
//     * @param doDrain
//     *            If false, the drain will only be simulated.
//     * @return Amount of mana that was (or would have been, if simulated) drained from the
//     *         container.
//     */
//    ManaStack drain(ItemStack container, int maxDrain, boolean doDrain);
//}