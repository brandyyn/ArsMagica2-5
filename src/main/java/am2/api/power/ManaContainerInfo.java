package am2.api.power;



public final class ManaContainerInfo
{
    public final ManaStack mana;
    public final int capacity;

    public ManaContainerInfo(ManaStack mana, int capacity)
    {
        this.mana = mana;
        this.capacity = capacity;
    }

    public ManaContainerInfo(IManaContainer tank)
    {
        this.mana = tank.getMana();
        this.capacity = tank.getCapacity();
    }
}