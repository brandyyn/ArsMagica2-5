
package am2.api.power;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.RegistryDelegate;
import net.minecraft.nbt.NBTTagCompound;

/**
 * ItemStack substitute for Mana.
 *
 * NOTE: Equality is based on the Mana, not the amount. Use
 * {@link #isManaStackIdentical(ManaStack)} to determine if ManaID, Amount and NBT Tag are all
 * equal.
 */
public class ManaStack
{

    private final Mana mana;
    public int amount;
    public NBTTagCompound tag;
    private RegistryDelegate<Mana> manaDelegate;

    public ManaStack(Mana mana, int amount)
    {
        if (mana == null)
        {
            FMLLog.bigWarning("Null mana supplied to manastack. Did you try and create a stack for an unregistered mana?");
            throw new IllegalArgumentException("Cannot create a manastack from a null mana");
        }
        else if (!ManaRegistry.isManaRegistered(mana))
        {
            FMLLog.bigWarning("Failed attempt to create a ManaStack for an unregistered Mana %s (type %s)", mana.getName(), mana.getClass().getName());
            throw new IllegalArgumentException("Cannot create a manastack from an unregistered mana");
        }
    	this.manaDelegate = ManaRegistry.makeDelegate(mana);
        this.amount = amount;
        this.mana = mana;
    }

    public ManaStack(Mana mana, int amount, NBTTagCompound nbt)
    {
        this(mana, amount);

        if (nbt != null)
        {
            tag = (NBTTagCompound) nbt.copy();
        }
    }

    public ManaStack(ManaStack stack, int amount)
    {
        this(stack.getMana(), amount, stack.tag);
    }


    public ManaStack(int manaID, int amount)
    {
    	this(ManaRegistry.getMana(manaID), amount);
    }

    public ManaStack(int manaID, int amount, NBTTagCompound nbt)
    {
    	this(ManaRegistry.getMana(manaID), amount, nbt);
    }

    /**
     * This provides a safe method for retrieving a ManaStack - if the Mana is invalid, the stack
     * will return as null.
     */
    public static ManaStack loadManaStackFromNBT(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return null;
        }
        String manaName = nbt.getString("ManaName");

        if (manaName == null || ManaRegistry.getMana(manaName) == null)
        {
            return null;
        }
        ManaStack stack = new ManaStack(ManaRegistry.getMana(manaName), nbt.getInteger("Amount"));

        if (nbt.hasKey("Tag"))
        {
            stack.tag = nbt.getCompoundTag("Tag");
        }
        return stack;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setString("ManaName", ManaRegistry.getManaName(getMana()));
        nbt.setInteger("Amount", amount);

        if (tag != null)
        {
            nbt.setTag("Tag", tag);
        }
        return nbt;
    }

    public final Mana getMana()
    {
        return manaDelegate.get();
    }

    public final int getManaID()
    {
    	return ManaRegistry.getManaID(getMana());
    }

    /**
     * @return A copy of this ManaStack
     */
    public ManaStack copy()
    {
        return new ManaStack(getMana(), amount, tag);
    }

    /**
     * Determines if the ManaIDs and NBT Tags are equal. This does not check amounts.
     *
     * @param other
     *            The ManaStack for comparison
     * @return true if the Manas (IDs and NBT Tags) are the same
     */
    public boolean isManaEqual(ManaStack other)
    {
        return other != null && getMana() == other.getMana() && isManaStackTagEqual(other);
    }

    private boolean isManaStackTagEqual(ManaStack other)
    {
        return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
    }

    /**
     * Determines if the NBT Tags are equal. Useful if the ManaIDs are known to be equal.
     */
    public static boolean areManaStackTagsEqual(ManaStack stack1, ManaStack stack2)
    {
        return stack1 == null && stack2 == null || (stack1 != null && stack2 != null && stack1.isManaStackTagEqual(stack2));
    }

    /**
     * Determines if the Mana are equal and this stack is larger.
     *
     * @param other
     * @return true if this ManaStack contains the other ManaStack (same mana and >= amount)
     */
    public boolean containsMana(ManaStack other)
    {
        return isManaEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the ManaIDs, Amounts, and NBT Tags are all equal.
     *
     * @param other
     *            - the ManaStack for comparison
     * @return true if the two ManaStacks are exactly the same
     */
    public boolean isManaStackIdentical(ManaStack other)
    {
        return isManaEqual(other) && amount == other.amount;
    }
    @Override
    public final int hashCode()
    {
    	int code = 1;
    	code = 31*code + getMana().hashCode();
    	code = 31*code + amount;
    	if (tag != null)
    		code = 31*code + tag.hashCode();
    	return code;
    }

    /**
     * Default equality comparison for a ManaStack. Same functionality as isManaEqual().
     *
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object o)
    {
        if (!(o instanceof ManaStack))
        {
            return false;
        }

        return isManaEqual((ManaStack) o);
    }
}