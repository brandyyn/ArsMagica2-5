//
//package am2.api.power;
//
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import cpw.mods.fml.common.FMLLog;
//import cpw.mods.fml.common.eventhandler.Event;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraftforge.common.MinecraftForge;
//
//
//import java.util.Map;
//import java.util.Set;
//
///**
// * Register simple items that contain manas here. Useful for buckets, bottles, and things that have
// * ID/metadata mappings.
// *
// * For more complex items, use {@link IManaContainerItem} instead.
// *
// * @author King Lemming
// *
// */
//public abstract class ManaContainerRegistry
//{
//    // Holder object that implements HashCode for an ItemStack,
//    // the local maps are not guaranteed to have the same internal generic structure,
//    // but the external interface for checking ItemStacks will still exist.
//    private static class ContainerKey
//    {
//        ItemStack container;
//        ManaStack stack;
//        private ContainerKey(ItemStack container)
//        {
//            this.container = container;
//        }
//        private ContainerKey(ItemStack container, ManaStack stack)
//        {
//            this(container);
//            this.stack = stack;
//        }
//        @Override
//        public int hashCode()
//        {
//            int code = 1;
//            code = 31*code + container.getItem().hashCode();
//            code = 31*code + container.getItemDamage();
//            if (stack != null)
//                code = 31*code + stack.getMana().hashCode();
//            return code;
//        }
//        @Override
//        public boolean equals(Object o)
//        {
//            if (!(o instanceof ContainerKey)) return false;
//            ContainerKey ck = (ContainerKey)o;
//            if (container.getItem() != ck.container.getItem()) return false;
//            if (container.getItemDamage() != ck.container.getItemDamage()) return false;
//            if (stack == null && ck.stack != null) return false;
//            if (stack != null && ck.stack == null) return false;
//            if (stack == null && ck.stack == null) return true;
//            if (stack.getMana() != ck.stack.getMana()) return false;
//            return true;
//        }
//    }
//
//    private static Map<ContainerKey, ManaContainerData> containerManaMap = Maps.newHashMap();
//    private static Map<ContainerKey, ManaContainerData> filledContainerMap = Maps.newHashMap();
//    private static Set<ContainerKey> emptyContainers = Sets.newHashSet();
//
//    public static final int BUCKET_VOLUME = 1000;
//    public static final ItemStack EMPTY_BUCKET = new ItemStack(Items.bucket);
//    public static final ItemStack EMPTY_BOTTLE = new ItemStack(Items.glass_bottle);
//    private static final ItemStack NULL_EMPTYCONTAINER = new ItemStack(Items.bucket);
//
//    static
//    {
//        registerManaContainer(ManaRegistry.WATER, new ItemStack(Items.water_bucket), EMPTY_BUCKET);
//        registerManaContainer(ManaRegistry.LAVA,  new ItemStack(Items.lava_bucket),  EMPTY_BUCKET);
//        registerManaContainer(ManaRegistry.WATER, new ItemStack(Items.potionitem),   EMPTY_BOTTLE);
//    }
//
//    private ManaContainerRegistry(){}
//
//    /**
//     * Register a new mana containing item.
//     *
//     * @param stack
//     *            ManaStack containing the type and amount of the mana stored in the item.
//     * @param filledContainer
//     *            ItemStack representing the container when it is full.
//     * @param emptyContainer
//     *            ItemStack representing the container when it is empty.
//     * @return True if container was successfully registered; false if it already is.
//     */
//    public static boolean registerManaContainer(ManaStack stack, ItemStack filledContainer, ItemStack emptyContainer)
//    {
//        return registerManaContainer(new ManaContainerData(stack, filledContainer, emptyContainer));
//    }
//
//    /**
//     * Register a new mana containing item. The item is assumed to hold 1000 mB of mana. Also
//     * registers the Mana if possible.
//     *
//     * @param mana
//     *            Mana type that is stored in the item.
//     * @param filledContainer
//     *            ItemStack representing the container when it is full.
//     * @param emptyContainer
//     *            ItemStack representing the container when it is empty.
//     * @return True if container was successfully registered; false if it already is, or an invalid parameter was passed.
//     */
//    public static boolean registerManaContainer(Mana mana, ItemStack filledContainer, ItemStack emptyContainer)
//    {
//        if (!ManaRegistry.isManaRegistered(mana))
//        {
//            ManaRegistry.registerMana(mana);
//        }
//        return registerManaContainer(new ManaStack(mana, BUCKET_VOLUME), filledContainer, emptyContainer);
//    }
//
//    /**
//     * Register a new mana containing item that does not have an empty container.
//     *
//     * @param stack
//     *            ManaStack containing the type and amount of the mana stored in the item.
//     * @param filledContainer
//     *            ItemStack representing the container when it is full.
//     * @return True if container was successfully registered; false if it already is, or an invalid parameter was passed.
//     */
//    public static boolean registerManaContainer(ManaStack stack, ItemStack filledContainer)
//    {
//        return registerManaContainer(new ManaContainerData(stack, filledContainer, null, true));
//    }
//
//    /**
//     * Register a new mana containing item that does not have an empty container. The item is
//     * assumed to hold 1000 mB of mana. Also registers the Mana if possible.
//     *
//     * @param mana
//     *            Mana type that is stored in the item.
//     * @param filledContainer
//     *            ItemStack representing the container when it is full.
//     * @return True if container was successfully registered; false if it already is, or an invalid parameter was passed.
//     */
//    public static boolean registerManaContainer(Mana mana, ItemStack filledContainer)
//    {
//        if (!ManaRegistry.isManaRegistered(mana))
//        {
//            ManaRegistry.registerMana(mana);
//        }
//        return registerManaContainer(new ManaStack(mana, BUCKET_VOLUME), filledContainer);
//    }
//
//    /**
//     * Register a new mana containing item.
//     *
//     * @param data
//     *            See {@link ManaContainerData}.
//     * @return True if container was successfully registered; false if it already is, or an invalid parameter was passed.
//     */
//    public static boolean registerManaContainer(ManaContainerData data)
//    {
//        if (isFilledContainer(data.filledContainer) || data.filledContainer == null)
//        {
//            return false;
//        }
//        if (data.mana == null || data.mana.getMana() == null)
//        {
//        	FMLLog.bigWarning("Invalid registration attempt for a mana container item %s has occurred. The registration has been denied to prevent crashes. The mod responsible for the registration needs to correct this.", data.filledContainer.getItem().getUnlocalizedName(data.filledContainer));
//        	return false;
//        }
//        containerManaMap.put(new ContainerKey(data.filledContainer), data);
//
//        if (data.emptyContainer != null && data.emptyContainer != NULL_EMPTYCONTAINER)
//        {
//            filledContainerMap.put(new ContainerKey(data.emptyContainer, data.mana), data);
//            emptyContainers.add(new ContainerKey(data.emptyContainer));
//        }
//
//        MinecraftForge.EVENT_BUS.post(new ManaContainerRegisterEvent(data));
//        return true;
//    }
//
//    /**
//     * Determines the mana type and amount inside a container.
//     *
//     * @param container
//     *            The mana container.
//     * @return ManaStack representing stored mana.
//     */
//    public static ManaStack getManaForFilledItem(ItemStack container)
//    {
//        if (container == null)
//        {
//            return null;
//        }
//
//        ManaContainerData data = containerManaMap.get(new ContainerKey(container));
//        return data == null ? null : data.mana.copy();
//    }
//
//    /**
//     * Attempts to fill an empty container with a mana.
//     *
//     * NOTE: Returns null on fail, NOT the empty container.
//     *
//     * @param mana
//     *            ManaStack containing the type and amount of mana to fill.
//     * @param container
//     *            ItemStack representing the empty container.
//     * @return Filled container if successful, otherwise null.
//     */
//    public static ItemStack fillManaContainer(ManaStack mana, ItemStack container)
//    {
//        if (container == null || mana == null)
//        {
//            return null;
//        }
//
//        ManaContainerData data = filledContainerMap.get(new ContainerKey(container, mana));
//        if (data != null && mana.amount >= data.mana.amount)
//        {
//            return data.filledContainer.copy();
//        }
//        return null;
//    }
//
//    /**
//     * Attempts to empty a full container.
//     *
//     * @param container
//     *            ItemStack representing the full container.
//     * @return Empty container if successful, otherwise null.
//     */
//    public static ItemStack drainManaContainer(ItemStack container)
//    {
//        if (container == null)
//        {
//            return null;
//        }
//
//        ManaContainerData data = containerManaMap.get(new ContainerKey(container));
//        if (data != null)
//        {
//            return data.emptyContainer.copy();
//        }
//
//        return null;
//    }
//
//    /**
//     * Determines the capacity of a full container.
//     *
//     * @param container
//     *            The full container.
//     * @return The containers capacity, or 0 if the ItemStack does not represent
//     *         a registered container.
//     */
//    public static int getContainerCapacity(ItemStack container)
//    {
//        return getContainerCapacity(null, container);
//    }
//
//    /**
//     * Determines the capacity of a container.
//     *
//     * @param mana
//     *            ManaStack containing the type of mana the capacity should be
//     *            determined for (ignored for full containers).
//     * @param container
//     *            The container (full or empty).
//     * @return The containers capacity, or 0 if the ItemStack does not represent
//     *         a registered container or the ManaStack is not registered with
//     *         the empty container.
//     */
//    public static int getContainerCapacity(ManaStack mana, ItemStack container)
//    {
//        if (container == null)
//        {
//            return 0;
//        }
//
//        ManaContainerData data = containerManaMap.get(new ContainerKey(container));
//
//        if (data != null)
//        {
//            return data.mana.amount;
//        }
//
//        if (mana != null)
//        {
//            data = filledContainerMap.get(new ContainerKey(container, mana));
//
//            if (data != null)
//            {
//                return data.mana.amount;
//            }
//        }
//
//        return 0;
//    }
//
//    /**
//     * Determines if a container holds a specific mana.
//     */
//    public static boolean containsMana(ItemStack container, ManaStack mana)
//    {
//        if (container == null || mana == null)
//        {
//            return false;
//        }
//
//        ManaContainerData data = containerManaMap.get(new ContainerKey(container));
//        return data == null ? false : data.mana.containsMana(mana);
//    }
//
//    public static boolean isBucket(ItemStack container)
//    {
//        if (container == null)
//        {
//            return false;
//        }
//
//        if (container.isItemEqual(EMPTY_BUCKET))
//        {
//            return true;
//        }
//
//        ManaContainerData data = containerManaMap.get(new ContainerKey(container));
//        return data != null && data.emptyContainer.isItemEqual(EMPTY_BUCKET);
//    }
//
//    public static boolean isContainer(ItemStack container)
//    {
//        return isEmptyContainer(container) || isFilledContainer(container);
//    }
//
//    public static boolean isEmptyContainer(ItemStack container)
//    {
//        return container != null && emptyContainers.contains(new ContainerKey(container));
//    }
//
//    public static boolean isFilledContainer(ItemStack container)
//    {
//        return container != null && getManaForFilledItem(container) != null;
//    }
//
//    public static ManaContainerData[] getRegisteredManaContainerData()
//    {
//        return containerManaMap.values().toArray(new ManaContainerData[containerManaMap.size()]);
//    }
//
//    /**
//     * Wrapper class for the registry entries. Ensures that none of the attempted registrations
//     * contain null references unless permitted.
//     */
//    public static class ManaContainerData
//    {
//        public final ManaStack mana;
//        public final ItemStack filledContainer;
//        public final ItemStack emptyContainer;
//
//        public ManaContainerData(ManaStack stack, ItemStack filledContainer, ItemStack emptyContainer)
//        {
//            this(stack, filledContainer, emptyContainer, false);
//        }
//
//        public ManaContainerData(ManaStack stack, ItemStack filledContainer, ItemStack emptyContainer, boolean nullEmpty)
//        {
//            this.mana = stack;
//            this.filledContainer = filledContainer;
//            this.emptyContainer = emptyContainer == null ? NULL_EMPTYCONTAINER : emptyContainer;
//
//            if (stack == null || filledContainer == null || emptyContainer == null && !nullEmpty)
//            {
//                throw new RuntimeException("Invalid ManaContainerData - a parameter was null.");
//            }
//        }
//
//        public ManaContainerData copy()
//        {
//            return new ManaContainerData(mana, filledContainer, emptyContainer, true);
//        }
//    }
//
//    public static class ManaContainerRegisterEvent extends Event
//    {
//        public final ManaContainerData data;
//
//        public ManaContainerRegisterEvent(ManaContainerData data)
//        {
//            this.data = data.copy();
//        }
//    }
//}