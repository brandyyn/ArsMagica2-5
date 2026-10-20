
package am2.api.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Reference Tile Entity implementation of {@link IManaHandler}. Use/extend this or write your own.
 *
 * 
 */
public class TileManaHandler extends TileEntity implements IManaHandler
{
    protected ManaContainer tank;

    public TileManaHandler(int capacity){
        this.tank = new ManaContainer(capacity);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tank.writeToNBT(tag);
    }

    /* IManaHandler */
    @Override
    public int fill(ForgeDirection from, ManaStack resource, boolean doFill)
    {
        return tank.fill(resource, doFill);
    }

    @Override
    public ManaStack drain(ForgeDirection from, ManaStack resource, boolean doDrain)
    {
        if (resource == null || !resource.isManaEqual(tank.getMana()))
        {
            return null;
        }
        return tank.drain(resource.amount, doDrain);
    }

    @Override
    public ManaStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Mana mana)
    {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Mana mana)
    {
        return true;
    }

    @Override
    public ManaContainerInfo[] getTankInfo(ForgeDirection from)
    {
        return new ManaContainerInfo[] { tank.getInfo() };
    }
}