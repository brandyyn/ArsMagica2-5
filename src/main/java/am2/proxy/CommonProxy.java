package am2.proxy;

import am2.utility.DimensionUtilities;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import am2.*;
import am2.affinity.AffinityHelper;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.armor.ArmorEventHandler;
import am2.armor.infusions.*;
import am2.blocks.BlockAMOre;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.tileentities.TileEntityParticleEmitter;
import am2.buffs.BuffList;
import am2.enchantments.AMEnchantments;
import am2.entities.EntityManager;
import am2.entities.ai.MageVillagerTrade;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.network.AMPacketProcessorServer;
import am2.particles.ParticleManagerServer;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeCache;
import am2.power.PowerNodeEntry;
import am2.proxy.gui.ServerGuiManager;
import am2.proxy.tick.ServerTickHandler;
import am2.spell.SkillManager;
import am2.spell.SkillTreeManager;
import am2.spell.SpellUnlockManager;
import am2.utility.ProxyUtilitiesCommon;
import am2.worldgen.AM2WorldDecorator;
import am2.worldgen.RetroactiveWorldgenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static am2.blocks.BlocksCommonProxy.AMOres;

public class CommonProxy{

	private ArrayList<AMVector3> pendingFlickerLinks;
	public BlocksCommonProxy blocks;
	public ItemsCommonProxy items;
	public static HashMap<String, String> teamHostility;
	public ParticleManagerServer particleManager;
	public NBTTagCompound cwCopyLoc;
	public AM2WorldDecorator worldGen;
	public AMEnchantments enchantments;
	public ItemFrameWatcher itemFrameWatcher;
	protected ProxyUtilitiesCommon utils;
	public static EntityManager entities;
	public static ServerGuiManager guiManager;

	private ServerTickHandler serverTickHandler;

	private final HashMap<EntityLivingBase, ArrayList<PotionEffect>> deferredPotionEffects = new HashMap<EntityLivingBase, ArrayList<PotionEffect>>();
	private Reference2IntOpenHashMap<EntityLivingBase> deferredDimensionTransfers = new Reference2IntOpenHashMap<EntityLivingBase>();

	private int totalFlickerCount = 0;

	public CommonProxy(){
		teamHostility = new HashMap<>();
		particleManager = new ParticleManagerServer();
		itemFrameWatcher = new ItemFrameWatcher();
		pendingFlickerLinks = new ArrayList<>();
		cwCopyLoc = null;
	}

	public void postinit(){

		blocks.setupSpellConstraints();
		items.postInit();
		
		BuffList.postInit();

		MinecraftForge.EVENT_BUS.register(new AMEventHandler());
		MinecraftForge.EVENT_BUS.register(new am2.utility.Soulbound());
		FMLCommonHandler.instance().bus().register(new AMEventHandler());
		MinecraftForge.EVENT_BUS.register(PowerNodeCache.instance);
		MinecraftForge.EVENT_BUS.register(itemFrameWatcher);
		MinecraftForge.EVENT_BUS.register(new AffinityHelper());
		MinecraftForge.EVENT_BUS.register(new SpellUnlockManager());
		MinecraftForge.EVENT_BUS.register(new ArmorEventHandler());
		FMLCommonHandler.instance().bus().register(new ArmorEventHandler());
		MinecraftForge.EVENT_BUS.register(EntityManager.instance);
		MinecraftForge.EVENT_BUS.register(new AMWorldEventHandler());
		serverTickHandler = new ServerTickHandler();
		FMLCommonHandler.instance().bus().register(serverTickHandler);

		if (!SkillTreeManager.instance.isSkillDisabled(SkillManager.instance.getSkill("Shrink")))
			MinecraftForge.EVENT_BUS.register(new ShrinkHandler());

		MinecraftForge.EVENT_BUS.register(particleManager);
		FMLCommonHandler.instance().bus().register(particleManager);

		FMLCommonHandler.instance().bus().register(new PlayerTracker());
	}

	public void preinit(){
		AMCore.config.init();
		AMCore.skillConfig.init();
		utils = new ProxyUtilitiesCommon();

		blocks = new BlocksCommonProxy();
		items = new ItemsCommonProxy();
		entities = EntityManager.instance;

		BuffList.Init();
		BuffList.Instantiate();

		blocks.InstantiateBlocks();
		items.InstantiateItems();
		blocks.RegisterBlocks();
		blocks.RegisterTileEntities();

		ObeliskFuelHelper.instance.registerFuelType(new ItemStack(ItemsCommonProxy.itemOre, 0, ItemOre.META_VINTEUMDUST), 200);
		ObeliskFuelHelper.instance.registerFuelType(new ItemStack(ItemsCommonProxy.itemAMBucket, 0, Short.MAX_VALUE), 2000);
		ObeliskFuelHelper.instance.registerFuelType(new ItemStack(AMOres, 0, BlockAMOre.META_VINTEUM_BLOCK), 2700);

		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Blocks.stonebrick), new ItemStack(Blocks.stonebrick, 1, 2));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.bone), new ItemStack(Items.dye, 1, 15));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Blocks.stone), new ItemStack(Blocks.cobblestone));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.gravel));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Blocks.sandstone), new ItemStack(Blocks.sand));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Blocks.web), new ItemStack(Items.string));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.nether_wart), new ItemStack(ItemsCommonProxy.stormSawtoothSeeds));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.wheat_seeds), new ItemStack(ItemsCommonProxy.rainRockroseSeeds));

		// broken records
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_13), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_blocks), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_cat), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_chirp), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_far), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_mall), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_mellohi), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_stal), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_strad), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_wait), new ItemStack(Items.record_11));
		EnervatorRecipeHelper.instance.registerRecipe(new ItemStack(Items.record_ward), new ItemStack(Items.record_11));

		registerInfusions();


	}

	public void init(){
		blocks.InitRecipes();
		items.InitRecipes();

		entities.registerEntities();
		entities.initializeSpawns();

		SkillManager.instance.init();

		SkillTreeManager.instance.init();
		worldGen = new AM2WorldDecorator();
		GameRegistry.registerWorldGenerator(worldGen, 0);

		EntityItemWatcher.instance.init();

		enchantments = new AMEnchantments();
		enchantments.Init();
		VillagerRegistry.instance().registerVillagerId(AMCore.config.getVillagerProfessionID());
		VillagerRegistry.instance().registerVillageTradeHandler(AMCore.config.getVillagerProfessionID(), new MageVillagerTrade());
	}

	public AM2WorldDecorator getWorldGen(){
		return worldGen;
	}

	public void InitializeAndRegisterHandlers(){
		guiManager = new ServerGuiManager();
		NetworkRegistry.INSTANCE.registerGuiHandler(AMCore.instance, guiManager);
		AMNetHandler.INSTANCE.registerChannels(new AMPacketProcessorServer());
	}

	public void addQueuedRetrogen(int dimensionID, ChunkCoordIntPair pair){
		ArrayList<ChunkCoordIntPair> chunks;
		if (RetroactiveWorldgenerator.deferredChunkGeneration.containsKey(dimensionID)){
			chunks = RetroactiveWorldgenerator.deferredChunkGeneration.get(dimensionID);
		}else{
			chunks = new ArrayList<ChunkCoordIntPair>();
		}

		chunks.add(pair);
		RetroactiveWorldgenerator.deferredChunkGeneration.put(dimensionID, chunks);
	}

	public void flashManaBar(){
	}

	public void blackoutArmorPiece(EntityPlayerMP player, int slot, int cooldown){
		serverTickHandler.blackoutArmorPiece(player, slot, cooldown);
	}

	public Entity getEntityByID(World world, int ID){
		// the world's id map instead of a scan of every loaded entity
		Entity ent = world.getEntityByID(ID);
		return ent instanceof EntityLivingBase ? ent : null;
	}

	public WorldServer[] getWorldServers(){
		return FMLServerHandler.instance().getServer().worldServers;
	}

	public EntityLivingBase getEntityByID(int entityID){
		Entity ent = null;
		for (WorldServer ws : getWorldServers()){
			ent = ws.getEntityByID(entityID);
			if (ent != null){
				if (!(ent instanceof EntityLivingBase)) return null;
				else break;
			}
		}
		return (EntityLivingBase)ent;
	}

	public EntityPlayer getLocalPlayer(){
		return null;
	}

	public ProxyUtilitiesCommon getProxyUtils(){
		return utils;
	}

	public int getArmorRenderIndex(String prefix){
		return 0;
	}

	public void openSkillTreeUI(World world, EntityPlayer player){
	}

	public void openParticleBlockGUI(World world, EntityPlayer player, TileEntityParticleEmitter te){
	}

	public boolean setMouseDWheel(int dwheel){
		return false;
	}

	public void renderGameOverlay(){
	}

	public String getItemStackDisplayName(ItemStack newStack){
		return "";
	}

	public String getCurrentLanguage(){
		return null;
	}

	public void sendLocalMovementData(EntityLivingBase ent){

	}

	public void setCompendiumSaveBase(String compendiumBase){

	}

	public void addDeferredPotionEffect(EntityLivingBase ent, PotionEffect pe){
		if (!deferredPotionEffects.containsKey(ent))
			deferredPotionEffects.put(ent, new ArrayList<PotionEffect>());

		ArrayList<PotionEffect> effects = deferredPotionEffects.get(ent);
		effects.add(pe);
	}

	public void addDeferredDimensionTransfer(EntityLivingBase ent, int dimension){
		deferredDimensionTransfers.put(ent, dimension);
	}

	/**
	 * Applies the queued potion effects. Called every server tick, so it returns before copying anything while the
	 * queue is empty (the queue used to be cloned twice per entity, every world tick).
	 */
	public void applyDeferredPotionEffects(){
		if (deferredPotionEffects.isEmpty())
			return;
		ArrayList<Map.Entry<EntityLivingBase, ArrayList<PotionEffect>>> pending = new ArrayList<Map.Entry<EntityLivingBase, ArrayList<PotionEffect>>>(deferredPotionEffects.entrySet());
		deferredPotionEffects.clear();
		for (Map.Entry<EntityLivingBase, ArrayList<PotionEffect>> entry : pending)
			for (PotionEffect effect : entry.getValue())
				entry.getKey().addPotionEffect(effect);
	}

	/** Performs the queued dimension transfers; see {@link #applyDeferredPotionEffects()}. */
	public void applyDeferredDimensionTransfers(){
		if (deferredDimensionTransfers.isEmpty())
			return;
		Reference2IntOpenHashMap<EntityLivingBase> pending = deferredDimensionTransfers;
		deferredDimensionTransfers = new Reference2IntOpenHashMap<EntityLivingBase>();
		for (ObjectIterator<Reference2IntMap.Entry<EntityLivingBase>> it = Reference2IntMaps.fastIterator(pending); it.hasNext(); ){
			Reference2IntMap.Entry<EntityLivingBase> entry = it.next();
			DimensionUtilities.doDimensionTransfer(entry.getKey(), entry.getIntValue());
		}
	}

	public void requestPowerPathVisuals(IPowerNode node, EntityPlayerMP player){

	}

	public void receivePowerPathVisuals(HashMap<PowerTypes, ArrayList<LinkedList<AMVector3>>> paths){

	}

	public HashMap<PowerTypes, ArrayList<LinkedList<AMVector3>>> getPowerPathVisuals(){
		return null;
	}

	public boolean isClientPlayer(EntityLivingBase ent){
		return false;
	}

	public void setViewSettings(){
		if (ExtendedProperties.For(getLocalPlayer()).getFlipRotation() > 0)
			Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
	}

	public void registerInfusions(){
		DamageReductionImbuement.registerAll();
		GenericImbuement.registerAll();
		ImbuementRegistry.instance.registerImbuement(new Dispelling());
		ImbuementRegistry.instance.registerImbuement(new FallProtection());
		ImbuementRegistry.instance.registerImbuement(new FireProtection());
		ImbuementRegistry.instance.registerImbuement(new Freedom());
		ImbuementRegistry.instance.registerImbuement(new Healing());
		ImbuementRegistry.instance.registerImbuement(new HungerBoost());
		ImbuementRegistry.instance.registerImbuement(new JumpBoost());
		ImbuementRegistry.instance.registerImbuement(new LifeSaving());
		ImbuementRegistry.instance.registerImbuement(new Lightstep());
		ImbuementRegistry.instance.registerImbuement(new MiningSpeed());
		ImbuementRegistry.instance.registerImbuement(new Recoil());
		ImbuementRegistry.instance.registerImbuement(new SwimSpeed());
		ImbuementRegistry.instance.registerImbuement(new WaterBreathing());
		ImbuementRegistry.instance.registerImbuement(new WaterWalking());
	}

	public void setTrackedPowerCompound(NBTTagCompound compound){
	}

	public void setTrackedLocation(AMVector3 location){
	}

	public boolean hasTrackedLocationSynced(){
		return false;
	}

	public PowerNodeEntry getTrackedData(){
		return null;
	}

	public void addDeferredTargetSet(EntityLiving ent, EntityLivingBase target){
		serverTickHandler.addDeferredTarget(ent, target);
	}

	/**
	 * Proxied compendium unlocks.  Do not call directly - use the CompendiumUnlockHandler instead.
	 */
	public void unlockCompendiumEntry(String id){

	}

	/**
	 * Proxied compendium unlocks.  Do not call directly - use the CompendiumUnlockHandler instead.
	 */
	public void unlockCompendiumCategory(String id){

	}

	public void drawPowerOnBlockHighlight(EntityPlayer player, MovingObjectPosition target, float partialTicks){

	}

	public void incrementFlickerCount(){
		this.totalFlickerCount++;
	}

	public void decrementFlickerCount(){
		this.totalFlickerCount--;
		if (this.totalFlickerCount < 0)
			this.totalFlickerCount = 0;
	}

	public int getTotalFlickerCount(){
		return this.totalFlickerCount;
	}

	public void addDigParticle(World worldObj, int xCoord, int yCoord, int zCoord, Block block, int meta){
	}
}
