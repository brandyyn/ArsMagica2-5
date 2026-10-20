package am2.items;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import am2.AMCore;
import am2.texture.ResourceManager;
import am2.utility.EntityUtilities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCrystalPhylactery extends ArsMagicaItem{

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	private List<String> spawnableEntities;
	private Object2IntOpenHashMap<String> eggColors;
	static final boolean enabled_summon = AMCore.skillConfig.isSkillEnabled("Summon");

	public static final int META_EMPTY = 0;
	public static final int META_QUARTER = 1;
	public static final int META_HALF = 2;
	public static final int META_FULL = 3;


	public ItemCrystalPhylactery(){
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		if (par1ItemStack.hasTagCompound()){
			String className = par1ItemStack.stackTagCompound.getString("SpawnClassName");
			if (className != null){
				par3List.add(String.format(StatCollector.translateToLocal("am2.tooltip.phyEss"), StatCollector.translateToLocal("entity." + className + ".name")));
				float pct = par1ItemStack.stackTagCompound.getFloat("PercentFilled");
				par3List.add(String.format(StatCollector.translateToLocal("am2.tooltip.pctFull"), pct));
			}else{
				par3List.add(StatCollector.translateToLocal("am2.tooltip.empty"));
			}
		}else{
			par3List.add(StatCollector.translateToLocal("am2.tooltip.empty"));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		icons = new IIcon[4];
		icons[0] = ResourceManager.RegisterTexture("crystal_phylactery", par1IconRegister);
		icons[1] = ResourceManager.RegisterTexture("crystal_phylactery_liquidquarter", par1IconRegister);
		icons[2] = ResourceManager.RegisterTexture("crystal_phylactery_liquidhalf", par1IconRegister);
		icons[3] = ResourceManager.RegisterTexture("crystal_phylactery_liquid", par1IconRegister);
	}

	public void addFill(ItemStack stack){
		if (stack.hasTagCompound()){
			String className = stack.stackTagCompound.getString("SpawnClassName");
			if (className != null){
				float pct = stack.stackTagCompound.getFloat("PercentFilled");
				pct += itemRand.nextFloat() * 5;
				if (pct > 100) pct = 100;
				stack.stackTagCompound.setFloat("PercentFilled", pct);
				if (pct == 100)
					stack.setItemDamage(META_FULL);
				else if (pct > 50)
					stack.setItemDamage(META_HALF);
				else if (pct > 25)
					stack.setItemDamage(META_QUARTER);
				else
					stack.setItemDamage(META_EMPTY);

			}
		}
	}

	public void addFill(ItemStack stack, float amt){
		if (stack.hasTagCompound()){
			String className = stack.stackTagCompound.getString("SpawnClassName");
			if (className != null){
				float pct = stack.stackTagCompound.getFloat("PercentFilled");
				pct += amt;
				if (pct > 100) pct = 100;
				stack.stackTagCompound.setFloat("PercentFilled", pct);
				if (pct == 100)
					stack.setItemDamage(META_FULL);
				else if (pct > 50)
					stack.setItemDamage(META_HALF);
				else if (pct > 25)
					stack.setItemDamage(META_QUARTER);
				else
					stack.setItemDamage(META_EMPTY);

			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack, int pass){
		if (pass == 1) return false;
		return par1ItemStack.getItemDamage() == META_FULL;
	}

	public void setSpawnClass(ItemStack stack, Class clazz){

		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		String s = (String)EntityList.classToStringMapping.get(clazz);
		if (s != null)
			stack.stackTagCompound.setString("SpawnClassName", s);
	}

	public boolean canStore(ItemStack stack, EntityLiving entity){
		if (entity instanceof IBossDisplayData) return false;
		if (stack.getItemDamage() == META_FULL)
			return false;
		if (!stack.hasTagCompound())
			return true;

		String e = stack.stackTagCompound.getString("SpawnClassName");
		String s = (String)EntityList.classToStringMapping.get(entity.getClass());

		return (e != null && s != null) && e.equals(s);
	}

	public boolean isFull(ItemStack stack){
		return stack.getItemDamage() == META_FULL;
	}

	@Override
	public boolean requiresMultipleRenderPasses(){
		return true;
	}

	public String getSpawnClass(ItemStack stack){
		if (!stack.hasTagCompound())
			return "";
		return stack.stackTagCompound.getString("SpawnClassName");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass){
		if (pass == 0){
			if (meta < 4) return icons[meta];
		}
		return icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass){
		if (enabled_summon && pass == 0){
			if (stack.hasTagCompound()){
				String className = stack.stackTagCompound.getString("SpawnClassName");
				if (!className.isEmpty())
					return getEntityColor(className);
			}
			return 0x0000FF;
		}
		return 0xFFFFFF;
	}

	/**
	 * Spawn egg colour for an entity name, or a stable colour derived from the name when the entity has no egg.
	 * Egg colours only depend on registrations that are fixed after init, so they are indexed once on first use.
	 */
	private int getEntityColor(String entityName){
		if (eggColors == null){
			Object2IntOpenHashMap<String> colors = new Object2IntOpenHashMap<String>();
			colors.defaultReturnValue(-1);
			for (Object info : EntityList.entityEggs.values()){
				EntityEggInfo eei = (EntityEggInfo)info;
				String name = EntityList.getStringFromID(eei.spawnedID);
				if (name != null)
					colors.put(name, eei.primaryColor);
			}
			eggColors = colors;
		}
		return eggColors.containsKey(entityName) ? eggColors.getInt(entityName) : entityName.hashCode() & 0xFFFFFF;
	}

	/**
	 * Builds the list of storable creatures shown in the creative tab. Deciding whether a class has AI enabled needs an
	 * instance, so every registered creature class is constructed exactly once, the first time the list is requested
	 * with a world available, instead of every client tick.
	 */
	@SideOnly(Side.CLIENT)
	private List<String> getSpawnableEntities(){
		if (spawnableEntities != null || !enabled_summon)
			return spawnableEntities;
		World world = net.minecraft.client.Minecraft.getMinecraft().theWorld;
		if (world == null)
			return null;
		ArrayList<String> names = new ArrayList<String>();
		for (Object entry : EntityList.classToStringMapping.entrySet()){
			Map.Entry mapping = (Map.Entry)entry;
			Class clazz = (Class)mapping.getKey();
			if (!EntityCreature.class.isAssignableFrom(clazz) || IBossDisplayData.class.isAssignableFrom(clazz))
				continue;
			try{
				EntityCreature temp = (EntityCreature)clazz.getConstructor(World.class).newInstance(world);
				if (EntityUtilities.isAIEnabled(temp))
					names.add((String)mapping.getValue());
			}catch (Throwable e){
				// third-party entity constructors may reject a bare world; such entities are simply not listed
			}
		}
		Collections.sort(names);
		spawnableEntities = names;
		return names;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
		par3List.add(new ItemStack(this));
		List<String> spawnable = getSpawnableEntities();
		if (spawnable == null) return;
		for (String s : spawnable){
			ItemStack stack = new ItemStack(this, 1, META_FULL);
			stack.stackTagCompound = new NBTTagCompound();
			stack.stackTagCompound.setString("SpawnClassName", s);
			stack.stackTagCompound.setFloat("PercentFilled", 100);
			par3List.add(stack);
		}
	}
}
