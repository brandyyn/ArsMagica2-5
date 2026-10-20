package am2.api.power;


import net.minecraft.world.World;

import java.util.Locale;

/**
 * Ars Magica 2 Mana Implementation
 *
 * This class is a mana equivalent to "Item." It describes the nature of a mana
 * and contains its general properties.
 */
public class Mana
{

    /** The unique identification name for this mana. */
    protected final String manaName;

    protected String color;

    public Mana(String manaName, String color)
    {
        this.manaName = manaName.toLowerCase(Locale.ENGLISH);
        this.color = color;
    }
    public final String getName()
    {
        return this.manaName;
    }
    public final int getID()
    {
        return ManaRegistry.getManaID(this.manaName);
    }
    /* used for text formatting */
    public String getColor()
    {
        return color;
    }
    /* Stack-based Accessors */
    public String getColor(ManaStack stack){ return getColor(); }
    /* World-based Accessors */
    public String getColor(World world, int x, int y, int z){ return getColor(); }


}