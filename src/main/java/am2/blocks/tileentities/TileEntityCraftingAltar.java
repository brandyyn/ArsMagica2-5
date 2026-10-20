package am2.blocks.tileentities;

import am2.AMCore;
import am2.api.blocks.BlockDec;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.blocks.BlockCoord;
import am2.api.blocks.MultiblockStructureDefinition.StructureGroup;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.blocks.BlockWizardsChalk;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.liquid.BlockLiquidEssence;
import am2.damage.DamageSources;
import am2.entities.EntityEarthElemental;
import am2.entities.EntityFireElemental;
import am2.entities.EntityManaElemental;
import am2.entities.EntityWaterElemental;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.multiblock.IMultiblockStructureController;
import am2.network.AMDataReader;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.playerextensions.ExtendedProperties;
import am2.power.PowerNodeRegistry;
import am2.spell.SkillManager;
import am2.spell.SpellRecipeManager;
import am2.spell.SpellUtils;
import am2.spell.components.Summon;
import am2.spell.shapes.Binding;
import am2.utility.KeyValuePair;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

import static am2.blocks.BlockAMOre.META_MOONSTONE_BLOCK;
import static am2.blocks.BlockAMOre.META_SUNSTONE_BLOCK;
import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

public class TileEntityCraftingAltar extends TileEntityAMPower implements IMultiblockStructureController{

	private MultiblockStructureDefinition primary;

	private boolean isCrafting;
	private final ArrayList<ItemStack> allAddedItems;
	private final ArrayList<ItemStack> currentAddedItems;

	private final ArrayList<KeyValuePair<ISpellPart, byte[]>> spellDef;
	private final ArrayList<ArrayList<KeyValuePair<ISpellPart, byte[]>>> shapeGroups;
	private final boolean allShapeGroupsAdded = false;

	private int currentKey = -1;
	private int checkCounter;
	private boolean structureValid;
	private BlockCoord podiumLocation;
	private BlockCoord switchLocation;
	private int maxEffects;
	private float stability;
	private ItemStack addedPhylactery = null;
	private ItemStack addedBindingCatalyst = null;

	private int[] spellGuide;
	private int[] outputCombo;
	private int[][] shapeGroupGuide;

	private int currentConsumedPower = 0;
	private int ticksExisted = 0;
	private PowerTypes currentMainPowerTypes = PowerTypes.NONE;

	private static final byte CRAFTING_CHANGED = 1;
	private static final byte COMPONENT_ADDED = 2;
	private static final byte FULL_UPDATE = 3;

	private static final int augmatl_mutex = 2;
	private static final int lectern_mutex = 4;
	private StructureGroup[] augMatl_primary;
	private StructureGroup tier1_primary;
	private StructureGroup tier2_primary;
	private StructureGroup tier3_primary;

	private String currentSpellName = "";
	final BlockDec[] augMaterials = new BlockDec[]{
			new BlockDec(Blocks.glass,0),
			new BlockDec(Blocks.coal_block,0),
			new BlockDec(Blocks.redstone_block,0),
			new BlockDec(Blocks.iron_block,0),
			new BlockDec(Blocks.lapis_block,0),
			new BlockDec(Blocks.gold_block,0),
			new BlockDec(Blocks.diamond_block,0),
			new BlockDec(Blocks.emerald_block,0),
			new BlockDec(BlocksCommonProxy.AMOres,META_MOONSTONE_BLOCK),
			new BlockDec(BlocksCommonProxy.AMOres,META_SUNSTONE_BLOCK),
	};
	public TileEntityCraftingAltar(){
		super(500);
		setupMultiblock();

		allAddedItems = new ArrayList<>();
		currentAddedItems = new ArrayList<>();
		isCrafting = false;
		structureValid = false;
		checkCounter = 0;
		setNoPowerRequests();
		maxEffects = 2;
		stability = 1F;

		spellDef = new ArrayList<>();
		shapeGroups = new ArrayList<>();

		for (int i = 0; i < 5; ++i){
			shapeGroups.add(new ArrayList<>());
		}
	}

	private void setupMultiblock(){

		primary = new MultiblockStructureDefinition("craftingAltar");
		primary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.craftingAltar);
		for (int z : new int[]{-2, 2}) {
			primary.addAllowedBlock(0, -1, z, BlocksCommonProxy.magicWall);
		}
		for (int y = -2; y >= -3; y--) {
			primary.addAllowedBlock(0, y, -2, BlocksCommonProxy.magicWall);
			primary.addAllowedBlock( 0, y, 2, BlocksCommonProxy.magicWall);

		}
		generateDefaultLectern(primary);
		generateDefaultAugments(primary);

		//wood, sandstone, cobble,
		tier1_primary = primary.copyGroup("main","tier1");
		List<BlockDec> tier1_decs = new ArrayList<>();
		List<BlockDec> tier1_decstairs = new ArrayList<>();
		for(ItemStack stack : OreDictionary.getOres("plankWood")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier1_decs.add(dec);
		}
		for(ItemStack stack : OreDictionary.getOres("sandstone")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier1_decs.add(dec);
		}
		for(ItemStack stack : OreDictionary.getOres("cobblestone")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier1_decs.add(dec);
		}
		for(ItemStack stack : OreDictionary.getOres("stairWood")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier1_decstairs.add(dec);
		}
		tier1_decstairs.add(new BlockDec(Blocks.sandstone_stairs ,WILDCARD_VALUE));
		tier1_decstairs.add(new BlockDec(Blocks.stone_stairs ,WILDCARD_VALUE));
		generateStructure(primary,tier1_primary, tier1_decs);
		generateStairs(primary,tier1_primary,tier1_decstairs);


		//brick & magical wood & stonebrick
		List<BlockDec> tier2_decs = new ArrayList<>();
		List<BlockDec> tier2_decstairs = new ArrayList<>();
		tier2_primary = primary.copyGroup("main","tier2");
		for(ItemStack stack : OreDictionary.getOres("stairMagical")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier2_decstairs.add(dec);
		}
		for(ItemStack stack : OreDictionary.getOres("plankMagical")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier2_decs.add(dec);
		}
		tier2_decstairs.add(new BlockDec(Blocks.brick_stairs, WILDCARD_VALUE));
		tier2_decstairs.add(new BlockDec(Blocks.stone_brick_stairs, WILDCARD_VALUE));
		tier2_decs.add(new BlockDec(Blocks.stonebrick,WILDCARD_VALUE));
		tier2_decs.add(new BlockDec(Blocks.brick_block,WILDCARD_VALUE));
		generateStructure(primary,tier2_primary,tier2_decs);
		generateStairs(primary,tier2_primary,tier2_decstairs);

		//quartz & nether bricks

		List<BlockDec> tier3_decs = new ArrayList<>();
		List<BlockDec> tier3_decstairs = new ArrayList<>();
		tier3_primary = primary.copyGroup("main","tier3");
		for(ItemStack stack : OreDictionary.getOres("blockQuartz")){
			BlockDec dec = new BlockDec(Block.getBlockFromItem(stack.getItem()) ,WILDCARD_VALUE);
			tier3_decs.add(dec);
		}
		tier3_decs.add(new BlockDec(Blocks.nether_brick,WILDCARD_VALUE));
		tier3_decstairs.add(new BlockDec(Blocks.quartz_stairs,WILDCARD_VALUE));
		tier3_decstairs.add(new BlockDec(Blocks.nether_brick_stairs,WILDCARD_VALUE));

		generateStructure(primary, tier3_primary, tier3_decs);
		generateStairs(primary, tier3_primary, tier3_decstairs);

	}
	void generateStructure(MultiblockStructureDefinition definition, StructureGroup group, List<BlockDec> block){
		// center column
		definition.addAllowedBlock(group,0, 0, -1, block);
		definition.addAllowedBlock(group,0, 0, 1, block);

		// ===== Row 1 (y = -1) =====
		for (int y = -1; y >= -3; y--){
			for (int x : new int[]{-1, 1}){
				definition.addAllowedBlock(group, x, y, -2, block);
				definition.addAllowedBlock(group, x, y, 2, block);
			}
		}
		// ===== Row 4 (y = -4) =====
		for (int i = -2; i <= 2; ++i){
			for (int j = -2; j <= 2; ++j){
				if (!(i == 0 && j == 0)){
					definition.addAllowedBlock(group,i, -4, j, block);
				}
			}
		}
	}
	void generateStairs(MultiblockStructureDefinition definition, StructureGroup group, List<BlockDec> block){
		/* 0,1  2,3  7,6 */
		// side stairs
		for (int z = -1; z <= 1; z++) {
			definition.addAllowedBlock(group,-1, 0, z, block);
			definition.addAllowedBlock(group, 1, 0, z, block);
		}
		definition.addAllowedBlock(group,0, 0, -2, block);
		definition.addAllowedBlock(group,0, 0, 2, block);
		for (int x : new int[]{-1, 1}){
			definition.addAllowedBlock(group,x, -1, -1, block);
			definition.addAllowedBlock(group,x, -1, 1, block);
		}
	}
	void generateDefaultAugments(MultiblockStructureDefinition definition){
		augMatl_primary = new StructureGroup[augMaterials.length];
		for (int i = 0; i < augMaterials.length; ++i)
			augMatl_primary[i] = definition.createGroup("augmatl" + i, augmatl_mutex);
		// aug materials at z = -2 and z = 2 for x = -1 and x = 1
		for (int i = 0; i < augMaterials.length; i++) {
			definition.addAllowedBlock(augMatl_primary[i], -1, 0, -2, augMaterials[i].getBlock(),augMaterials[i].getMeta());
			definition.addAllowedBlock(augMatl_primary[i], -1, 0, 2,  augMaterials[i].getBlock(), augMaterials[i].getMeta());
			definition.addAllowedBlock(augMatl_primary[i],  1, 0, -2, augMaterials[i].getBlock(), augMaterials[i].getMeta());
			definition.addAllowedBlock(augMatl_primary[i],  1, 0, 2,  augMaterials[i].getBlock(), augMaterials[i].getMeta());
			definition.addAllowedBlock(augMatl_primary[i], 0, -4, 0, augMaterials[i].getBlock(), augMaterials[i].getMeta());
		}

	}
	void generateDefaultLectern(MultiblockStructureDefinition definition){
		StructureGroup[] lecternGroup_primary = new StructureGroup[4];
		for (int i = 0; i < lecternGroup_primary.length; ++i){
			lecternGroup_primary[i] = definition.createGroup("lectern" + i, lectern_mutex);
		}
		int count = 0;
		for (int i = -2; i <= 2; i += 4){
			definition.addAllowedBlock(lecternGroup_primary[count], i, -3, i, BlocksCommonProxy.blockLectern);
			definition.addAllowedBlock(lecternGroup_primary[count], i, -2, -i,  Blocks.lever);
			definition.addAllowedBlock(lecternGroup_primary[count], i, -2, -i, Blocks.lever);
			count++;
			definition.addAllowedBlock(lecternGroup_primary[count], i, -3, -i, BlocksCommonProxy.blockLectern);
			definition.addAllowedBlock(lecternGroup_primary[count], i, -2, i, Blocks.lever);
			definition.addAllowedBlock(lecternGroup_primary[count], i, -2, i, Blocks.lever);
			count++;
		}
	}

	@Override
	public MultiblockStructureDefinition getDefinition(){
		return getFakeMultiblock();
	}

	public ItemStack getNextPlannedItem(){
		if (spellGuide != null){
			if ((this.allAddedItems.size()) * 3 < spellGuide.length){
				int guide_id = spellGuide[(this.allAddedItems.size()) * 3];
				int guide_qty = spellGuide[((this.allAddedItems.size()) * 3) + 1];
				int guide_meta = spellGuide[((this.allAddedItems.size()) * 3) + 2];
				return new ItemStack(Item.getItemById(guide_id), guide_qty, guide_meta);
			}else{
				return new ItemStack(ItemsCommonProxy.spellParchment);
			}
		}
		return null;
	}

	private int getNumPartsInSpell(){
		int parts = 0;
		if (outputCombo != null)
			parts = outputCombo.length;

		if (shapeGroupGuide != null){
			for (int[] ints : shapeGroupGuide){
				if (ints != null)
					parts += ints.length;
			}
		}
		return parts;
	}

	private boolean spellGuideIsWithinStructurePower(){
		return getNumPartsInSpell() <= maxEffects;
	}

	private boolean currentDefinitionIsWithinStructurePower(){
		int count = this.spellDef.size();
		for (ArrayList<KeyValuePair<ISpellPart, byte[]>> part : shapeGroups)
			count += part.size();

		return count <= this.maxEffects;
	}

	public float getStability(){
		return this.stability;
	}
	public int getDirection(){
		Block block = worldObj.getBlock(xCoord,yCoord, zCoord + 2);
		Block block1 = worldObj.getBlock(xCoord+2,yCoord, zCoord);
		return block != Blocks.air ? 1 : block1 != Blocks.air ? 0 : -1;
	}

	public boolean structureValid(){
		return this.structureValid;
	}

	public boolean isCrafting(){
		return this.isCrafting;
	}

	@Override
	public void updateEntity(){
		super.updateEntity();
		this.ticksExisted++;

		checkStructure();
		checkForStartCondition();
		updateLecternInformation();
		if (podiumLocation == null) return;
		if (!(worldObj.getTileEntity(xCoord + podiumLocation.getX(), yCoord + podiumLocation.getY(), zCoord + podiumLocation.getZ()) instanceof TileEntityLectern)) return; // crash fix
		if (isCrafting){
			checkForEndCondition();
			updatePowerRequestData();
			if (!worldObj.isRemote && !currentDefinitionIsWithinStructurePower() && this.ticksExisted > 100){
				worldObj.newExplosion(null, xCoord + 0.5, yCoord - 1.5, zCoord + 0.5, this.maxEffects, false, true); // the better the altar, the mode devastating the explosion in case of overload
				setCrafting(false);
				return;
			}
			if (worldObj.isRemote && checkCounter == 1){
				AMCore.proxy.particleManager.RibbonFromPointToPoint(worldObj, xCoord + 0.5, yCoord - 2, zCoord + 0.5, xCoord + 0.5, yCoord - 3, zCoord + 0.5);
			}
			List<EntityItem> components = lookForValidItems();
			ItemStack stack = getNextPlannedItem();
			for (EntityItem item : components){
				if (item.isDead) continue;
				ItemStack entityItemStack = item.getEntityItem();
				if (stack != null && compareItemStacks(stack, entityItemStack)){
					if (!worldObj.isRemote){
						updateCurrentRecipe(item);
						item.setDead();
					}else{
						worldObj.playSound(xCoord, yCoord, zCoord, "arsmagica2:misc.craftingaltar.component_added", 1.0f, 0.4f + worldObj.rand.nextFloat() * 0.6f, false);
						for (int i = 0; i < 5 * AMCore.config.getGFXLevel(); ++i){
							AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(worldObj, "radiant", item.posX, item.posY, item.posZ);
							if (particle != null){
								particle.setMaxAge(40);
								particle.AddParticleController(new ParticleMoveOnHeading(particle, worldObj.rand.nextFloat() * 360, worldObj.rand.nextFloat() * 360, 0.01f, 1, false));
								particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
								particle.setParticleScale(0.02f);
								particle.setRGBColorF(worldObj.rand.nextFloat(), worldObj.rand.nextFloat(), worldObj.rand.nextFloat());
							}
						}
					}
				}
			}
		}
	}

	private void updateLecternInformation(){
		if (podiumLocation == null) return;
		if (!(worldObj.getTileEntity(xCoord + podiumLocation.getX(), yCoord + podiumLocation.getY(), zCoord + podiumLocation.getZ()) instanceof TileEntityLectern)) return; // crash fix
		TileEntityLectern lectern = (TileEntityLectern)worldObj.getTileEntity(xCoord + podiumLocation.getX(), yCoord + podiumLocation.getY(), zCoord + podiumLocation.getZ());
		if (lectern != null){
			if (lectern.hasStack()){
				ItemStack lecternStack = lectern.getStack();
				if (lecternStack.hasTagCompound()){
					spellGuide = lecternStack.getTagCompound().getIntArray("spell_combo");
					outputCombo = lecternStack.getTagCompound().getIntArray("output_combo");
					currentSpellName = lecternStack.getDisplayName();

					int numShapeGroups = lecternStack.getTagCompound().getInteger("numShapeGroups");
					shapeGroupGuide = new int[numShapeGroups][];

					for (int i = 0; i < numShapeGroups; ++i){
						shapeGroupGuide[i] = lecternStack.getTagCompound().getIntArray("shapeGroupCombo_" + i);
					}
				}

				if (isCrafting){
					if (spellGuide != null){
						lectern.setNeedsBook(false);
						lectern.setTooltipStack(getNextPlannedItem());
					}else{
						lectern.setNeedsBook(true);
					}
				}else{
					lectern.setTooltipStack(null);
				}
				lectern.setOverpowered(!spellGuideIsWithinStructurePower());
			}else{
				if (isCrafting){
					lectern.setNeedsBook(true);
				}
				lectern.setTooltipStack(null);
			}
		}
	}

	public BlockCoord getSwitchLocation(){
		return this.switchLocation;
	}

	public boolean switchIsOn(){
		if (switchLocation == null) return false;
		Block block = worldObj.getBlock(xCoord + switchLocation.getX(), yCoord + switchLocation.getY(), zCoord + switchLocation.getZ());
		boolean b = false;
		if (block == Blocks.lever){
			for (int i = 0; i < 6; ++i){
				b = (Blocks.lever.isProvidingStrongPower(worldObj, xCoord + switchLocation.getX(), yCoord + switchLocation.getY(), zCoord + switchLocation.getZ(), i) > 0);
				if (b) break;
			}
		}
		return b;
	}

	public void flipSwitch(){
		if (switchLocation == null) return;
		Block block = worldObj.getBlock(xCoord + switchLocation.getX(), yCoord + switchLocation.getY(), zCoord + switchLocation.getZ());
		if (block == Blocks.lever){
			Blocks.lever.onBlockActivated(worldObj, xCoord + switchLocation.getX(), yCoord + switchLocation.getY(), zCoord + switchLocation.getZ(), null, 0, 0, 0, 0);
		}
	}

	private void updatePowerRequestData(){
		ItemStack stack = getNextPlannedItem();
		if (stack != null && stack.getItem() instanceof ItemEssence && stack.getItemDamage() > ItemEssence.META_MAX){
			if (switchIsOn()){
				int flags = stack.getItemDamage() - ItemEssence.META_MAX;
				setPowerRequests();
				pickPowerType(stack);
				if (this.currentMainPowerTypes != PowerTypes.NONE && PowerNodeRegistry.For(this.worldObj).checkPower(this, this.currentMainPowerTypes, 100)){
					currentConsumedPower += (int)PowerNodeRegistry.For(worldObj).consumePower(this, this.currentMainPowerTypes, Math.min(100, stack.stackSize - currentConsumedPower));
				}
				if (currentConsumedPower >= stack.stackSize){
					PowerNodeRegistry.For(this.worldObj).setPower(this, this.currentMainPowerTypes, 0);
					if (!worldObj.isRemote)
						addItemToRecipe(new ItemStack(ItemsCommonProxy.essence, stack.stackSize, ItemEssence.META_MAX + flags));
					currentConsumedPower = 0;
					currentMainPowerTypes = PowerTypes.NONE;
					setNoPowerRequests();
					flipSwitch();
				}
			}else{
				setNoPowerRequests();
			}
		}else{
			setNoPowerRequests();
		}
	}

	@Override
	protected void setNoPowerRequests(){
		currentConsumedPower = 0;
		currentMainPowerTypes = PowerTypes.NONE;

		super.setNoPowerRequests();
	}

	private void pickPowerType(ItemStack stack){
		if (this.currentMainPowerTypes != PowerTypes.NONE)
			return;
		int flags = stack.getItemDamage() - ItemEssence.META_MAX;
		PowerTypes highestValid = PowerTypes.NONE;
		float amt = 0;
		for (PowerTypes type : PowerTypes.all()){
			float tmpAmt = PowerNodeRegistry.For(worldObj).getPower(this, type);
			if (tmpAmt > amt)
				highestValid = type;
		}

		this.currentMainPowerTypes = highestValid;
	}

	private void updateCurrentRecipe(EntityItem item){
		ItemStack stack = item.getEntityItem();
		addItemToRecipe(stack);
	}

	private void addItemToRecipe(ItemStack stack){
		int stability = stabilityCheckFail(); // no need to scan twice
		if (stability > 0) {
			if (!worldObj.isRemote){
				randomInstabilityEffect(stability);
			}
			return;
		}

		allAddedItems.add(stack);
		currentAddedItems.add(stack);

		if (!worldObj.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(xCoord);
			writer.add(yCoord);
			writer.add(zCoord);
			writer.add(COMPONENT_ADDED);
			writer.add(stack);
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 32, AMPacketIDs.CRAFTING_ALTAR_DATA, writer.generate());
		}

		if (matchCurrentRecipe()){
			currentAddedItems.clear();
		}
	}

	private double getDistanceSqXZ(Entity entity, double x, double z) {
		double dx = entity.posX - x;
		double dz = entity.posZ - z;
		return dx * dx + dz * dz;
	}

	private int getInstability() {
		float instability = 1F;

		// ADD INSTABILITY

		if (worldObj.getWorldInfo().isThundering()) instability += 1F;

		instability += ((float)this.getNumPartsInSpell() / 2); // half of number of spell components
		if (outputCombo != null){
			Set<Integer> unique = new HashSet<>();
			Set<Integer> duplicate = new HashSet<>(); // Stacking 3 solar? bad boy
			for (int i : outputCombo){
				if (duplicate.contains(i)){ // second time being duplicated
					instability += 1.5F;
				} else if (unique.contains(i)){ // first time being duplicated
					instability += 0.5F;
					duplicate.add(i);
				} else{
					unique.add(i);
				}
			}
		}

		if (shapeGroupGuide != null){
			for (int[] shapeGroup : shapeGroupGuide){
				Set<Integer> unique = new HashSet<>();
				Set<Integer> duplicate = new HashSet<>();
				for (int i : shapeGroup){
					if (duplicate.contains(i)){
						instability += 0.75F;
					} else if (unique.contains(i)){
						instability += 0.25F;
						duplicate.add(i);
					} else{
						unique.add(i);
					}
				}
			}
		}

		int countPlayers = -1;
		// 20 sounds magic number a lot... maybe fix it in the future
		final double range = 20D;
		for (Object objectPlayer : worldObj.playerEntities)
		{
			if (getDistanceSqXZ((EntityPlayer)objectPlayer, xCoord, zCoord) < range * range) // only XZ check to prevent stupidest altair hack
			{
				countPlayers++;
			}
		}
		instability += countPlayers * 2.0F;

		// SUBTRACT INSTABILITY (mostly, except for black aurem)

		if (worldObj.canBlockSeeTheSky(this.xCoord, this.yCoord, this.zCoord)) instability -= 0.5F;
		if (!worldObj.isDaytime()) instability -= 0.5F;

		ArrayList<Block> blockList = new ArrayList<>();
		for (int x = -5; x <= 5; x++) {
			for (int z = -5; z <= 5; z++) {
				for (int y = -6; y < -2; y++) {
					blockList.add(worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z));
				}
			}
		}

		boolean celestialPrismCounted = false, darkAuremCounted = false;
		for (Block block : blockList) {
			if (block instanceof BlockWizardsChalk) instability -= 0.2F;
			if (block instanceof BlockLiquidEssence) instability -= 0.25F;
			if (block == BlocksCommonProxy.celestialPrism && !celestialPrismCounted){
				instability -= 2.6F;
				celestialPrismCounted = true;
			}
			if (block == BlocksCommonProxy.blackAurem && !darkAuremCounted) {
				instability += 2.6F;
				darkAuremCounted = true;
			}
		}

		return instability > 1F ? (int) instability : 1;
	}

	private int stabilityCheckFail(){
		return (int)(worldObj.rand.nextInt(getInstability()) - this.stability);
	}

	private void randomInstabilityEffect(int fail) {
		// search for player in range
		final double range = 50.0D;
		double distance = Double.MAX_VALUE;
		EntityPlayer player = null;
		for (Object objectPlayer : worldObj.playerEntities){
			EntityPlayer entityplayer1 = (EntityPlayer)objectPlayer;
			double newDistance = getDistanceSqXZ(entityplayer1, xCoord, zCoord); // only XZ check to prevent stupidest altair hack
			if ((newDistance < range * range) && (newDistance < distance)){
				distance = newDistance;
				player = entityplayer1;
			}
		}
		if (player == null) {
			return; // No player? What on earth could be going on?
			// altair hack is going on.
		}
		
		Random random = worldObj.rand;
		int effect = random.nextInt(6);
		if (fail > 2) { // attact elementals if > 2 instability
			for (int i = 0; i <= fail; i++){
				int elementalType = random.nextInt(4);
				Entity elemental;
				switch (elementalType) {
					case 0:
						elemental = new EntityEarthElemental(this.worldObj);
						break;
					case 1:
						elemental = new EntityWaterElemental(this.worldObj);
						break;
					case 2:
						elemental = new EntityFireElemental(this.worldObj);
						break;
					default:
						elemental = new EntityManaElemental(this.worldObj);
						break;
				}
				elemental.setPositionAndRotation(
						player.posX + ((random.nextDouble() - random.nextDouble()) * 7),
						player.posY + (random.nextDouble() * 5),
						player.posZ + ((random.nextDouble() - random.nextDouble()) * 7),
						random.nextFloat() * 360,
						random.nextFloat() * 360);
				this.worldObj.spawnEntityInWorld(elemental);
			}
		}
		if (effect == 5 && fail > 7) { // teleport to an arbitrary point in spacetime, also resetting crafting
			player.setWorld(DimensionManager.getWorlds()[random.nextInt(DimensionManager.getWorlds().length)]);
			player.setPosition(
					random.nextInt(20000000) - 10000000,
					random.nextInt(1000) - 500,
					random.nextInt(20000000) - 10000000
			);
		}
		if (random.nextBoolean()) { // drain half of mana
			ExtendedProperties.For(player).deductMana(ExtendedProperties.For(player).getMaxMana() / 2);
		}

		if (random.nextInt(10) >= 7){
			// explosion
			if (!worldObj.isRemote) worldObj.newExplosion(null, xCoord + 0.5, yCoord - 1.5, zCoord + 0.5, fail * 2, false, true);
		}
		if (random.nextBoolean()){
			// set on fire
			player.setFire(fail * 4);
			for (int x = -7; x <= 7; x++){
				for (int z = -7; z <= 7; z++){
					for (int y = -3; y <= 3; y++){
						Block blockBelow = worldObj.getBlock((int)player.posX + x, (int)player.posY + y - 1, (int)player.posZ + z);
						if (blockBelow.isNormalCube() && worldObj.isAirBlock((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z)){
							if (random.nextBoolean())
								worldObj.setBlock((int)player.posX + x, (int)player.posY + y, (int)player.posZ + z, Blocks.fire);
						}
					}
				}
			}
		}
		if (random.nextBoolean()){
			// lightning strike
			worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, player.posX, player.posY, player.posZ));
		}
		if (random.nextBoolean()){
			// damage
			player.attackEntityFrom(DamageSources.darkNexus, fail * 5);
		}
		if (random.nextBoolean()){
			// simply reset crafting
			this.deactivate();
		}
	}

	private boolean matchCurrentRecipe(){
		ISpellPart part = SpellRecipeManager.instance.getPartByRecipe(currentAddedItems);
		if (part == null) return false;

		ArrayList<KeyValuePair<ISpellPart, byte[]>> currentShapeGroupList = getShapeGroupToAddTo();

		if (part instanceof Summon)
			handleSummonShape();
		if (part instanceof Binding)
			handleBindingShape();

		byte[] metaData = new byte[0];
		if (part instanceof ISpellModifier){
			metaData = ((ISpellModifier)part).getModifierMetadata(currentAddedItems.toArray(new ItemStack[0]));
			if (metaData == null){
				metaData = new byte[0];
			}
		}

		//if this is null, then we have already completed all of the shape groups that the book identifies
		//we're now creating the body of the spell
		if (currentShapeGroupList == null){
			spellDef.add(new KeyValuePair<>(part, metaData));
		}else{
			currentShapeGroupList.add(new KeyValuePair<>(part, metaData));
		}
		return true;
	}

	private ArrayList<KeyValuePair<ISpellPart, byte[]>> getShapeGroupToAddTo(){
		for (int i = 0; i < shapeGroupGuide.length; ++i){
			int guideLength = shapeGroupGuide[i].length;
			int addedLength = shapeGroups.get(i).size();
			if (addedLength < guideLength)
				return shapeGroups.get(i);
		}

		return null;
	}

	private void handleSummonShape(){
		if (currentAddedItems.size() > 2)
			addedPhylactery = currentAddedItems.get(currentAddedItems.size() - 2);
	}

	private void handleBindingShape(){
		if (currentAddedItems.size() == 8)
			addedBindingCatalyst = currentAddedItems.get(currentAddedItems.size() - 1);
	}

	private List<EntityItem> lookForValidItems(){
		if (!isCrafting) return new ArrayList<>();
		double radius = worldObj.isRemote ? 2.1 : 2;
		return (List<EntityItem>)this.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xCoord - radius, yCoord - 3, zCoord - radius, xCoord + radius, yCoord, zCoord + radius));
	}

	private void checkStructure(){

		if ((isCrafting && checkCounter++ < 50) || (!isCrafting && checkCounter++ < 200)){
			return;
		}
		checkCounter = 0;

		boolean valid = primary.checkStructure(worldObj, xCoord, yCoord, zCoord);

		if (!valid){
			if (isCrafting) setCrafting(false);
		}

		//locate lectern and lever & material groups
		if (valid){
			maxEffects = 0;
			stability = 1F;
			ArrayList<StructureGroup> lecternGroups;
			ArrayList<StructureGroup> augmatlGroups;
			ArrayList<StructureGroup> mainmatlGroups;
				lecternGroups = primary.getMatchedGroups(lectern_mutex, worldObj, xCoord, yCoord, zCoord);
				augmatlGroups = primary.getMatchedGroups(augmatl_mutex, worldObj, xCoord, yCoord, zCoord);
				mainmatlGroups = primary.getMatchedGroups(MultiblockStructureDefinition.MAINGROUP_MUTEX, worldObj, xCoord, yCoord, zCoord);
			if (lecternGroups != null && !lecternGroups.isEmpty()){
				StructureGroup group = lecternGroups.get(0);
				HashMap<BlockCoord, ArrayList<BlockDec>> blocks = group.getAllowedBlocks();

				for (BlockCoord bc : blocks.keySet()){
					Block block = worldObj.getBlock(xCoord + bc.getX(), yCoord + bc.getY(), zCoord + bc.getZ());
					if (block == BlocksCommonProxy.blockLectern){
						podiumLocation = bc;
					}else if (block == Blocks.lever){
						switchLocation = bc;
					}
				}
			}
			if (augmatlGroups != null && augmatlGroups.size() == 1){
				StructureGroup group = augmatlGroups.get(0);
				int index = -1;
				for (StructureGroup augmatlGroup : augMatl_primary){
					index++;
					stability += 0.2F; // 2F max
					if (augmatlGroup == group){
						break;
					}
				}
				maxEffects = index + 1;
			}
			if (mainmatlGroups != null && mainmatlGroups.size() == 1){
				StructureGroup group = mainmatlGroups.get(0);
				if (group == tier1_primary){
					maxEffects += 1;
				} else if (group == tier2_primary){
					maxEffects += 2;
				} else if (group == tier3_primary){
					maxEffects += 3;
					stability += 1F;
				}
			}
		}else{
			podiumLocation = null;
			switchLocation = null;
			maxEffects = 0;
		}

		//maxEffects = 2;
		setStructureValid(valid);
	}

	private void checkForStartCondition(){
		if (this.worldObj.isRemote || !structureValid || this.isCrafting) return;

		List<EntityItem> items = this.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord - 3, zCoord - 2, xCoord + 2, yCoord, zCoord + 2));
		if (items.size() == 1){
			EntityItem item = items.get(0);
			if (item != null && !item.isDead && item.getEntityItem().getItem() == ItemsCommonProxy.rune && item.getEntityItem().getItemDamage() == ItemRune.META_BLANK){
				item.setDead();
				setCrafting(true);
			}
		}
	}

	private void checkForEndCondition(){
		if (!structureValid || !this.isCrafting || worldObj == null) return;

		double radius = worldObj.isRemote ? 2.2 : 2;

		List<EntityItem> items = this.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xCoord - radius, yCoord - 3, zCoord - radius, xCoord + radius, yCoord, zCoord + radius));
		if (items.size() == 1){
			EntityItem item = items.get(0);
			if (item != null && !item.isDead && item.getEntityItem() != null && checkEndItem(item.getEntityItem())){
				if (!worldObj.isRemote){
					item.setDead();
					setCrafting(false);
					createSpellItem();
					allAddedItems.clear();
					currentAddedItems.clear();
				}else{
					worldObj.playSound(xCoord, yCoord, zCoord, "arsmagica2:misc.craftingaltar.create_spell", 1.0f, 1.0f, true);
				}
			}
		}
	}

	private boolean checkEndItem(ItemStack item){
		return item.getItem() == ItemsCommonProxy.spellParchment;
	}
	private void createSpellItem(){
					EntityItem craftedItem = new EntityItem(worldObj);
					craftedItem.setPosition(xCoord + 0.5, yCoord - 1.5, zCoord + 0.5);

					ItemStack craftStack = SpellUtils.instance.createSpellStack(shapeGroups, spellDef);
					if (!craftStack.hasTagCompound())
						craftStack.stackTagCompound = new NBTTagCompound();
					AddSpecialMetadata(craftStack);

					craftStack.stackTagCompound.setString("suggestedName", currentSpellName != null ? currentSpellName : "");
					craftedItem.setEntityItemStack(craftStack);
					worldObj.spawnEntityInWorld(craftedItem);
	}

	private void AddSpecialMetadata(ItemStack craftStack){
		if (addedPhylactery != null){
			Summon summon = (Summon)SkillManager.instance.getSkill("Summon");
			summon.setSummonType(craftStack, addedPhylactery);
		}
		if (addedBindingCatalyst != null){
			Binding binding = (Binding)SkillManager.instance.getSkill("Binding");
			binding.setBindingType(craftStack, addedBindingCatalyst);
		}


	}

	private void setCrafting(boolean crafting){
		this.isCrafting = crafting;
		if (!worldObj.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(xCoord);
			writer.add(yCoord);
			writer.add(zCoord);
			writer.add(CRAFTING_CHANGED);
			writer.add(crafting);
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 32, AMPacketIDs.CRAFTING_ALTAR_DATA, writer.generate());
		}
		if (crafting){
			allAddedItems.clear();
			currentAddedItems.clear();

			spellDef.clear();
			for (ArrayList<KeyValuePair<ISpellPart, byte[]>> groups : shapeGroups)
				groups.clear();

			//find otherworld auras
			IPowerNode[] nodes = PowerNodeRegistry.For(worldObj).getAllNearbyNodes(worldObj, new AMVector3(this), PowerTypes.DARK);
			for (IPowerNode node : nodes){
				if (node instanceof TileEntityOtherworldAura){
					((TileEntityOtherworldAura)node).setActive(true, this);
					break;
				}
			}
		}
	}

	private void setStructureValid(boolean valid){
		if (this.structureValid == valid) return;
		this.structureValid = valid;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void deactivate(){
		if (!worldObj.isRemote){
			this.setCrafting(false);
			for (ItemStack stack : allAddedItems){
				if (stack.getItem() == ItemsCommonProxy.essence && stack.getItemDamage() > ItemEssence.META_MAX)
					continue;
				EntityItem eItem = new EntityItem(worldObj);
				eItem.setPosition(xCoord, yCoord - 1, zCoord);
				eItem.setEntityItemStack(stack);
				worldObj.spawnEntityInWorld(eItem);
			}
			allAddedItems.clear();
		}
	}

	private boolean compareItemStacks(ItemStack target, ItemStack input){
		if (target.getItem() == Items.potionitem && input.getItem() == Items.potionitem){
			return (target.getItemDamage() & 0xF) == (input.getItemDamage() & 0xF);
		}
		return target.getItem() == input.getItem() && (target.getItemDamage() == input.getItemDamage() || target.getItemDamage() == Short.MAX_VALUE) && target.stackSize == input.stackSize;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);

		NBTTagCompound altarCompound = new NBTTagCompound();
		altarCompound.setBoolean("isCrafting", this.isCrafting);
		altarCompound.setInteger("currentKey", this.currentKey);
		altarCompound.setString("currentSpellName", currentSpellName);

		NBTTagList allAddedItemsList = new NBTTagList();
		for (ItemStack stack : allAddedItems){
			NBTTagCompound addedItem = new NBTTagCompound();
			stack.writeToNBT(addedItem);
			allAddedItemsList.appendTag(addedItem);
		}

		altarCompound.setTag("allAddedItems", allAddedItemsList);

		NBTTagList currentAddedItemsList = new NBTTagList();
		for (ItemStack stack : currentAddedItems){
			NBTTagCompound addedItem = new NBTTagCompound();
			stack.writeToNBT(addedItem);
			currentAddedItemsList.appendTag(addedItem);
		}

		altarCompound.setTag("currentAddedItems", currentAddedItemsList);

		if (addedPhylactery != null){
			NBTTagCompound phylactery = new NBTTagCompound();
			addedPhylactery.writeToNBT(phylactery);
			altarCompound.setTag("phylactery", phylactery);
		}

		if (addedBindingCatalyst != null){
			NBTTagCompound catalyst = new NBTTagCompound();
			addedBindingCatalyst.writeToNBT(catalyst);
			altarCompound.setTag("catalyst", catalyst);
		}

		NBTTagList shapeGroupData = new NBTTagList();
		for (ArrayList<KeyValuePair<ISpellPart, byte[]>> list : shapeGroups){
			shapeGroupData.appendTag(ISpellPartListToNBT(list));
		}
		altarCompound.setTag("shapeGroups", shapeGroupData);

		NBTTagCompound spellDefSave = ISpellPartListToNBT(this.spellDef);
		altarCompound.setTag("spellDef", spellDefSave);

		nbttagcompound.setTag("altarData", altarCompound);
	}

	private NBTTagCompound ISpellPartListToNBT(ArrayList<KeyValuePair<ISpellPart, byte[]>> list){
		NBTTagCompound shapeGroupData = new NBTTagCompound();
		int[] ids = new int[list.size()];
		byte[][] meta = new byte[list.size()][];
		for (int d = 0; d < list.size(); ++d){
			ids[d] = SkillManager.instance.getShiftedPartID(list.get(d).getKey());
			meta[d] = list.get(d).getValue();
		}
		shapeGroupData.setIntArray("group_ids", ids);
		for (int i = 0; i < meta.length; ++i){
			shapeGroupData.setByteArray("meta_" + i, meta[i]);
		}
		return shapeGroupData;
	}

	private ArrayList<KeyValuePair<ISpellPart, byte[]>> NBTToISpellPartList(NBTTagCompound compound){
		int[] ids = compound.getIntArray("group_ids");
		ArrayList<KeyValuePair<ISpellPart, byte[]>> list = new ArrayList<KeyValuePair<ISpellPart, byte[]>>();
		for (int i = 0; i < ids.length; ++i){
			int partID = ids[i];
			ISkillTreeEntry part = SkillManager.instance.getSkill(i);
			byte[] partMeta = compound.getByteArray("meta_" + i);
			if (part instanceof ISpellPart){
				list.add(new KeyValuePair<ISpellPart, byte[]>((ISpellPart)part, partMeta));
			}
		}
		return list;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);

		if (!nbttagcompound.hasKey("altarData"))
			return;

		NBTTagCompound altarCompound = nbttagcompound.getCompoundTag("altarData");

		NBTTagList allAddedItems = altarCompound.getTagList("allAddedItems", Constants.NBT.TAG_COMPOUND);
		NBTTagList currentAddedItems = altarCompound.getTagList("currentAddedItems", Constants.NBT.TAG_COMPOUND);

		this.isCrafting = altarCompound.getBoolean("isCrafting");
		this.currentKey = altarCompound.getInteger("currentKey");
		this.currentSpellName = altarCompound.getString("currentSpellName");

		if (altarCompound.hasKey("phylactery")){
			NBTTagCompound phylactery = altarCompound.getCompoundTag("phylactery");
			if (phylactery != null)
				this.addedPhylactery = ItemStack.loadItemStackFromNBT(phylactery);
		}

		if (altarCompound.hasKey("catalyst")){
			NBTTagCompound catalyst = altarCompound.getCompoundTag("catalyst");
			if (catalyst != null)
				this.addedBindingCatalyst = ItemStack.loadItemStackFromNBT(catalyst);
		}

		this.allAddedItems.clear();
		for (int i = 0; i < allAddedItems.tagCount(); ++i){
			NBTTagCompound addedItem = allAddedItems.getCompoundTagAt(i);
			if (addedItem == null)
				continue;
			ItemStack stack = ItemStack.loadItemStackFromNBT(addedItem);
			if (stack == null)
				continue;
			this.allAddedItems.add(stack);
		}

		this.currentAddedItems.clear();
		for (int i = 0; i < currentAddedItems.tagCount(); ++i){
			NBTTagCompound addedItem = currentAddedItems.getCompoundTagAt(i);
			if (addedItem == null)
				continue;
			ItemStack stack = ItemStack.loadItemStackFromNBT(addedItem);
			if (stack == null)
				continue;
			this.currentAddedItems.add(stack);
		}

		this.spellDef.clear();
		for (ArrayList<KeyValuePair<ISpellPart, byte[]>> groups : shapeGroups)
			groups.clear();

		NBTTagCompound currentSpellDef = altarCompound.getCompoundTag("spellDef");
		this.spellDef.addAll(NBTToISpellPartList(currentSpellDef));

		NBTTagList currentShapeGroups = altarCompound.getTagList("shapeGroups", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < currentShapeGroups.tagCount(); ++i){
			NBTTagCompound compound = currentShapeGroups.getCompoundTagAt(i);
			shapeGroups.get(i).addAll(NBTToISpellPartList(compound));
		}
	}

	@Override
	public int getChargeRate(){
		return 250;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}


	public void HandleUpdatePacket(byte[] remainingBytes){
		AMDataReader rdr = new AMDataReader(remainingBytes, false);
		byte subID = rdr.getByte();
		switch (subID){
		case FULL_UPDATE:
			this.isCrafting = rdr.getBoolean();
			this.currentKey = rdr.getInt();

			this.allAddedItems.clear();
			this.currentAddedItems.clear();

			int itemCount = rdr.getInt();
			for (int i = 0; i < itemCount; ++i)
				this.allAddedItems.add(rdr.getItemStack());
			break;
		case CRAFTING_CHANGED:
			this.setCrafting(rdr.getBoolean());
			break;
		case COMPONENT_ADDED:
			this.allAddedItems.add(rdr.getItemStack());
			break;
		}
	}

	@Override
	public Packet getDescriptionPacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, worldObj.getBlockMetadata(xCoord, yCoord, zCoord), compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.func_148857_g());
	}
	private MultiblockStructureDefinition getFakeMultiblock(){
		MultiblockStructureDefinition fakemulti = new MultiblockStructureDefinition("fakeAltar");
		//augments
		StructureGroup augMatl_secondary = fakemulti.createGroup("augmatl", augmatl_mutex);
		fakemulti.addAllowedBlock(augMatl_secondary, -2, 0, -1, BlocksCommonProxy.AMOres, META_MOONSTONE_BLOCK);
		fakemulti.addAllowedBlock(augMatl_secondary, -2, 0,  1, BlocksCommonProxy.AMOres, META_MOONSTONE_BLOCK);
		fakemulti.addAllowedBlock(augMatl_secondary,  2, 0, -1, BlocksCommonProxy.AMOres, META_MOONSTONE_BLOCK);
		fakemulti.addAllowedBlock(augMatl_secondary,  2, 0,  1, BlocksCommonProxy.AMOres, META_MOONSTONE_BLOCK);
		fakemulti.addAllowedBlock(augMatl_secondary, 0, -4, 0, BlocksCommonProxy.AMOres, META_MOONSTONE_BLOCK);
		//lectern
		StructureGroup lecternGroup_secondary = fakemulti.createGroup("lectern" , lectern_mutex);
		fakemulti.addAllowedBlock(lecternGroup_secondary, -2, -3, 2, BlocksCommonProxy.blockLectern);
		fakemulti.addAllowedBlock(lecternGroup_secondary, 2, -2, 2, Blocks.lever,  3);
		// Stairs at z = -1 and z = 1 for x = -1,0,1
		for (int x = -1; x <= 1; x++) {
			fakemulti.addAllowedBlock(x, 0, -1, Blocks.stone_brick_stairs, 2);
			fakemulti.addAllowedBlock(x, 0,  1, Blocks.stone_brick_stairs, 3);
		}
		// Center row y=0
		fakemulti.addAllowedBlock(-2, 0, 0, Blocks.stone_brick_stairs, 0);
		fakemulti.addAllowedBlock(-1, 0, 0, Blocks.stonebrick);
		fakemulti.addAllowedBlock(0, 0, 0, BlocksCommonProxy.craftingAltar);
		fakemulti.addAllowedBlock(1, 0, 0, Blocks.stonebrick);
		fakemulti.addAllowedBlock(2, 0, 0, Blocks.stone_brick_stairs, 1);
		// ===== Row 1 (y = -1) =====
		for (int z : new int[]{-1, 1}) {
			fakemulti.addAllowedBlock(-2, -1, z, Blocks.stonebrick);
			fakemulti.addAllowedBlock(-1, -1, z, Blocks.stone_brick_stairs, 5);
			fakemulti.addAllowedBlock(1, -1,  z, Blocks.stone_brick_stairs, 4);
			fakemulti.addAllowedBlock(2, -1,  z, Blocks.stonebrick);
		}
		// Magic walls at x=-2 and x=2, z=0
		for (int x : new int[]{-2, 2}) {
			fakemulti.addAllowedBlock(x, -1, 0, BlocksCommonProxy.magicWall, 0);
		}
		// ===== Rows 2 & 3 (y = -2, -3) =====
		for (int y = -2; y >= -3; y--) {
			for (int z : new int[]{-1, 0, 1}) {
				fakemulti.addAllowedBlock(-2, y, z, z == 0 ? BlocksCommonProxy.magicWall : Blocks.stonebrick);
				fakemulti.addAllowedBlock( 2, y, z, z == 0 ? BlocksCommonProxy.magicWall : Blocks.stonebrick);
			}
		}
		// ===== Row 4 (y = -4) =====
		for (int i = -2; i <= 2; ++i){
			for (int j = -2; j <= 2; ++j){
				if (!(i == 0 && j == 0)){
					fakemulti.addAllowedBlock(i, -4, j, Blocks.stonebrick);
				}
			}
		}
		return  fakemulti;
	}
}
