package am2.customdata;

import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// per-world and per-dimension data. Syncing individual variables sucks.
public class CustomWorldData {

    private static Map<Integer, HashMap<String, String>> worldDataArray = new HashMap<Integer, HashMap<String, String>>();

    private static final HashMap<String, String> WorldVarsFor(World world){
        if (world == null)
            return new HashMap<String, String>();

        if (worldDataArray.containsKey(world.provider.dimensionId)){
            return worldDataArray.get(world.provider.dimensionId);
        }else{
            HashMap<String, String> reg = new HashMap<String, String>();
            worldDataArray.put(world.provider.dimensionId, reg);
            return reg;
        }
    }

    public static boolean worldHasVar(World world, String varName) {
        return WorldVarsFor(world).containsKey(varName);
    }

    public static String getWorldVar(World world, String varName) {
        return WorldVarsFor(world).get(varName);
    }

    public static void setWorldVar(World world, String varName, String varValue) {
        if (world.isRemote) return; // Only server can set variables, client can request
        WorldVarsFor(world).put(varName, varValue);
        syncWorldVarsToClients(world, null);
    }

    public static void setWorldVarNoSync(World world, String varName, String varValue) { // doesn't sync anywhere. Used for easing network load and for client updating
        WorldVarsFor(world).put(varName, varValue);
    }

    public static void processRequest(World world, String varName, String varValue, EntityPlayer requester) {
        boolean approved = false; // custom logic will be added here in future for special cases
        if (approved) setWorldVar(world, varName, varValue);
    }

    public static void requestWorldVar(World world, String varName, String varValue) { // the clientside method
        if (world == null) return;
        AMDataWriter writer = new AMDataWriter();
        writer.add(world.provider.dimensionId);
        writer.add(varName);
        writer.add(varValue);
        AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.REQUESTWORLDDATACHANGE, writer.generate());
    }

    private static void syncWorldVarsToClients(World world, EntityPlayer p) { // Yes, this method should be abstracted away. No, I'm not going to do it.
        AMDataWriter writer = new AMDataWriter();
        NBTTagCompound world_data = new NBTTagCompound();
        int c = 0;
        HashMap<String, String> data = WorldVarsFor(world);
        for (Object o : data.keySet()) {
            String iS = (String)o;
            String iValue = data.get(iS);
            world_data.setString("dataentry" + c, iValue);
            world_data.setString("dataentryname" + c, iS);
            c++;
        }
        world_data.setInteger("datasize", data.size());
        world_data.setInteger("dimensionid", world.provider.dimensionId);
        writer.add(world_data);

        // if null is passed, syncs to all clients
        if (p == null) AMNetHandler.INSTANCE.sendPacketToAllClients(AMPacketIDs.SYNCWORLDDATATOCLIENTS, writer.generate());
        else if (p instanceof EntityPlayerMP) AMNetHandler.INSTANCE.sendPacketToClientPlayer((EntityPlayerMP) p, AMPacketIDs.SYNCWORLDDATATOCLIENTS, writer.generate());
    }

    public static void saveAllWorldData() {
        try {
            if (DimensionManager.getWorld(0) != null) {
                if (DimensionManager.getWorld(0).isRemote) return;
                ISaveHandler handler = DimensionManager.getWorld(0).getSaveHandler();
                if (handler != null && handler instanceof SaveHandler) {
                    File saveFile = new File(((SaveHandler) handler).getWorldDirectory(), "AM2WorldData.txt");
                    saveFile.createNewFile();
                    ArrayList<String> lines = new ArrayList<String>();
                    for (Map.Entry<Integer, HashMap<String, String>> perDimension : worldDataArray.entrySet()) {
                        for (Map.Entry<String, String> perDimensionEntries : perDimension.getValue().entrySet()) {
                            lines.add(perDimension.getKey() + ":::" + perDimensionEntries.getKey() + ":::" + perDimensionEntries.getValue());
                        }
                    }
                    PrintWriter pw = new PrintWriter(saveFile);
                    for (String str : lines) pw.println(str);
                    pw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAllWorldData() {
        try {
            if (DimensionManager.getWorld(0) != null) {
                if (DimensionManager.getWorld(0).isRemote) return;
                ISaveHandler handler = DimensionManager.getWorld(0).getSaveHandler();
                if (handler != null && handler instanceof SaveHandler) {
                    File saveFile = new File(((SaveHandler) handler).getWorldDirectory(), "AM2WorldData.txt");
                    if (saveFile.exists()) {
                        ArrayList<String> lines = new ArrayList<String>();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(saveFile));
                            String line = br.readLine();
                            while (line != null) {
                                lines.add(line);
                                line = br.readLine();
                            }
                            br.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // this avoids syncing the same dimension multiple times clogging up the network
                        for (String entry : lines) {
                            setWorldVarNoSync(DimensionManager.getWorld(Integer.valueOf(entry.split(":::")[0])), entry.split(":::")[1], entry.split(":::")[2]);
                        }
                        syncAllWorldVarsToClients(null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // if null is passed, syncs to all clients
    public static void syncAllWorldVarsToClients(EntityPlayer p) {
        for (WorldServer ws : DimensionManager.getWorlds()) {
            syncWorldVarsToClients(ws, p);
        }
    }
}
