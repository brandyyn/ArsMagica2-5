package am2.api.power;

import net.minecraftforge.common.util.ForgeDirection;


public interface IManaHandler
{
    /**
     * Fills mana into internal tanks, distribution is left entirely to the IManaHandler.
     * 
     * @param from
     *            Orientation the Mana is pumped in from.
     * @param resource
     *            ManaStack representing the Mana and maximum amount of mana to be filled.
     * @param doFill
     *            If false, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    int fill(ForgeDirection from, ManaStack resource, boolean doFill);

    /**
     * Drains mana out of internal tanks, distribution is left entirely to the IManaHandler.
     * 
     * @param from
     *            Orientation the Mana is drained to.
     * @param resource
     *            ManaStack representing the Mana and maximum amount of mana to be drained.
     * @param doDrain
     *            If false, drain will only be simulated.
     * @return ManaStack representing the Mana and amount that was (or would have been, if
     *         simulated) drained.
     */
    ManaStack drain(ForgeDirection from, ManaStack resource, boolean doDrain);

    /**
     * Drains mana out of internal tanks, distribution is left entirely to the IManaHandler.
     * 
     * This method is not Mana-sensitive.
     * 
     * @param from
     *            Orientation the mana is drained to.
     * @param maxDrain
     *            Maximum amount of mana to drain.
     * @param doDrain
     *            If false, drain will only be simulated.
     * @return ManaStack representing the Mana and amount that was (or would have been, if
     *         simulated) drained.
     */
    ManaStack drain(ForgeDirection from, int maxDrain, boolean doDrain);

    /**
     * Returns true if the given mana can be inserted into the given direction.
     * 
     * More formally, this should return true if mana is able to enter from the given direction.
     */
    boolean canFill(ForgeDirection from, Mana mana);

    /**
     * Returns true if the given mana can be extracted from the given direction.
     * 
     * More formally, this should return true if mana is able to leave from the given direction.
     */
    boolean canDrain(ForgeDirection from, Mana mana);

    /**
     * Returns an array of objects which represent the internal tanks. These objects cannot be used
     * to manipulate the internal tanks. See {@link ManaContainerInfo}.
     * 
     * @param from
     *            Orientation determining which tanks should be queried.
     * @return Info for the relevant internal tanks.
     */
    ManaContainerInfo[] getTankInfo(ForgeDirection from);
}