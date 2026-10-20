package am2.api.power;


import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;


/**
 * Reference implementation of {@link IManaContainer}. Use/extend this or implement your own.
 *
 * @author King Lemming, cpw (LiquidTank)
 *
 */
public class ManaContainer implements IManaContainer
{
	protected ManaStack mana;
	protected int capacity;
	protected TileEntity tile;

	public ManaContainer(int capacity)
	{
		this(null, capacity);
	}

	public ManaContainer(ManaStack stack, int capacity)
	{
		this.mana = stack;
		this.capacity = capacity;
	}

	public ManaContainer(Mana mana, int amount, int capacity)
	{
		this(new ManaStack(mana, amount), capacity);
	}

	public ManaContainer readFromNBT(NBTTagCompound nbt)
	{
		if (!nbt.hasKey("Empty"))
		{
			ManaStack mana = ManaStack.loadManaStackFromNBT(nbt);
			setMana(mana);
		}
		else
		{
			setMana(null);
		}
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if (mana != null)
		{
			mana.writeToNBT(nbt);
		}
		else
		{
			nbt.setString("Empty", "");
		}
		return nbt;
	}

	public void setMana(ManaStack mana)
	{
		this.mana = mana;
	}

	/* IManaContainer */
	@Override
	public ManaStack getMana()
	{
		return mana;
	}

	@Override
	public int getManaAmount()
	{
		if (mana == null)
		{
			return 0;
		}
		return mana.amount;
	}

	@Override
	public int getCapacity()
	{
		return capacity;
	}

	@Override
	public ManaContainerInfo getInfo()
	{
		return new ManaContainerInfo(this);
	}

	@Override
	public int fill(ManaStack resource, boolean doFill)
	{
		if (resource == null)
		{
			return 0;
		}

		if (!doFill)
		{
			if (mana == null)
			{
				return Math.min(capacity, resource.amount);
			}

			if (!mana.isManaEqual(resource))
			{
				return 0;
			}

			return Math.min(capacity - mana.amount, resource.amount);
		}

		if (mana == null)
		{
			mana = new ManaStack(resource, Math.min(capacity, resource.amount));

			return mana.amount;
		}

		if (!mana.isManaEqual(resource))
		{
			return 0;
		}
		int filled = capacity - mana.amount;

		if (resource.amount < filled)
		{
			mana.amount += resource.amount;
			filled = resource.amount;
		}
		else
		{
			mana.amount = capacity;
		}

		return filled;
	}

	@Override
	public ManaStack drain(int maxDrain, boolean doDrain)
	{
		if (mana == null)
		{
			return null;
		}

		int drained = maxDrain;
		if (mana.amount < drained)
		{
			drained = mana.amount;
		}

		ManaStack stack = new ManaStack(mana, drained);
		if (doDrain)
		{
			mana.amount -= drained;
			if (mana.amount <= 0)
			{
				mana = null;
			}

		}
		return stack;
	}
}
