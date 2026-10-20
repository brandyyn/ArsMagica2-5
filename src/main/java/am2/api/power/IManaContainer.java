package am2.api.power;


public interface IManaContainer
{
    /**
     * @return ManaStack representing the mana in the tank, null if the tank is empty.
     */
    ManaStack getMana();

    /**
     * @return Current amount of mana in the tank.
     */
    int getManaAmount();

    /**
     * @return Capacity of this mana tank.
     */
    int getCapacity();

    /**
     * Returns a wrapper object {@link ManaContainerInfo } containing the capacity of the tank and the
     * ManaStack it holds.
     * 
     * Should prevent manipulation of the IManaTank. See {@link ManaContainer}.
     * 
     * @return State information for the IManaTank.
     */
    ManaContainerInfo getInfo();

    /**
     * 
     * @param resource
     *            ManaStack attempting to fill the tank.
     * @param doFill
     *            If false, the fill will only be simulated.
     * @return Amount of mana that was accepted by the tank.
     */
    int fill(ManaStack resource, boolean doFill);

    /**
     * 
     * @param maxDrain
     *            Maximum amount of mana to be removed from the container.
     * @param doDrain
     *            If false, the fill will only be simulated.
     * @return Amount of mana that was removed from the tank.
     */
    ManaStack drain(int maxDrain, boolean doDrain);
}