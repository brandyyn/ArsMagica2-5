package am2.api.power;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.registry.RegistryDelegate;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;



// Handles Mana registrations. Mana MUST be registered in order to function.

public class ManaRegistry
{

    static int maxID = 0;

    static BiMap<String, Mana> manas = HashBiMap.create();
    static BiMap<Mana, Integer> manaIDs = HashBiMap.create();
    static BiMap<Integer, String> manaNames = HashBiMap.create();
    static Map<Mana,ManaDelegate> delegates = Maps.newHashMap();
    public static Mana DARK = new Mana("Dark","§4");
    public static Mana LIGHT = new Mana("Light", "§b");
    public static Mana NEUTRAL = new Mana("Neutral", "§1");
    public static Mana NONE = new Mana("None", "§f");
    static {
        ManaRegistry.registerMana(DARK);
        ManaRegistry.registerMana(LIGHT);
        ManaRegistry.registerMana(NEUTRAL);
        ManaRegistry.registerMana(NONE);
    }
    public static boolean registerMana(String key,Mana mana){
        if (manas.containsKey(mana.getName()))
        {
            return false;
        }
        manas.put(mana.getName(), mana);
        maxID++;
        manaIDs.put(mana, maxID);
        manaNames.put(maxID, mana.getName());
        MinecraftForge.EVENT_BUS.post(new ManaRegisterEvent(mana.getName(), maxID));
        return true;
    }

    /**
     * Register a new Mana. If a mana with the same name already exists, registration the alternative mana is tracked
     * in case it is the default in another place
     *
     * @param mana
     *            The mana to register.
     * @return True if the mana was registered as the current default mana, false if it was only registered as an alternative
     */
    public static boolean registerMana(Mana mana)
    {
        delegates.put(mana, new ManaDelegate(mana, mana.getName()));
        if (manas.containsKey(mana.getName()))
        {
            return false;
        }
        manas.put(mana.getName(), mana);
        maxID++;
        manaIDs.put(mana, maxID);
        manaNames.put(maxID, mana.getName());

        MinecraftForge.EVENT_BUS.post(new ManaRegisterEvent(mana.getName(), maxID));
        return true;
    }

    /**
     * Does the supplied mana have an entry for it's name.
     * @param mana the mana we're testing
     * @return if the mana's name has a registration entry
     */
    public static boolean isManaRegistered(Mana mana)
    {
        return mana != null && manas.containsKey(mana.getName());
    }

    public static boolean isManaRegistered(String manaName)
    {
        return manas.containsKey(manaName);
    }

    public static Mana getMana(String manaName)
    {
        return manas.get(manaName);
    }

    public static Mana getMana(int manaID)
    {
        return manaIDs.inverse().get(manaID);
    }

    public static int getManaID(Mana mana)
    {
        return manaIDs.get(mana);
    }

    public static int getManaID(String manaName)
    {
        return manaIDs.get(getMana(manaName));
    }

    public static String getManaName(int manaID)
    {
        return manaNames.get(manaID);
    }

    public static String getManaName(Mana mana)
    {
        return manas.inverse().get(mana);
    }

    public static String getManaName(ManaStack stack)
    {
        return getManaName(stack.getMana());
    }

    public static ManaStack getManaStack(String manaName, int amount)
    {
        if (!manas.containsKey(manaName))
        {
            return null;
        }
        return new  ManaStack(getMana(manaName), amount);
    }

    /**
     * Returns a read-only map containing Mana Names and their associated Manas.
     */
    public static Map<String, Mana> getRegisteredManas()
    {
        return ImmutableMap.copyOf(manas);
    }

    /**
     * Returns a read-only map containing Mana Names and their associated IDs.
     */
    public static Map<String, Integer> getRegisteredManaIDs()
    {
        return ImmutableMap.copyOf(manaNames.inverse());
    }

    /**
     * Returns a read-only map containing Mana IDs and their associated Manas.
     * In 1.8.3, this will change to just 'getRegisteredManaIDs'
     */
    public static Map<Mana, Integer> getRegisteredManaIDsByMana()
    {
        return ImmutableMap.copyOf(manaIDs);
    }

    public static class ManaRegisterEvent extends Event
    {
        public final String manaName;
        public final int manaID;

        public ManaRegisterEvent(String manaName, int manaID)
        {
            this.manaName = manaName;
            this.manaID = manaID;
        }
    }

    public static int getMaxID()
    {
        return maxID;
    }


    static RegistryDelegate<Mana> makeDelegate(Mana mana)
    {
        return delegates.get(mana);
    }


    private static class ManaDelegate implements RegistryDelegate<Mana>
    {
        private String name;
        private Mana mana;

        ManaDelegate(Mana mana, String name)
        {
            this.mana = mana;
            this.name = name;
        }

        @Override
        public Mana get()
        {
            return mana;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public Class<Mana> type()
        {
            return Mana.class;
        }
    }
}