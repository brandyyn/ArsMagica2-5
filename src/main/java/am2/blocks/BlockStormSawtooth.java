package am2.blocks;

import am2.items.ItemsCommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlockStormSawtooth extends BlockBush
{
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    protected BlockStormSawtooth()
    {
        this.setTickRandomly(true);
        float f = 0.5F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
        this.setCreativeTab((CreativeTabs)null);
    }

    /**
     * is the block grass, dirt or farmland
     */
    protected boolean canPlaceBlockOn(Block p_149854_1_)
    {
        return p_149854_1_ == Blocks.soul_sand;
    }

    /**
     * Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
     */
    public boolean canBlockStay(World p_149718_1_, int p_149718_2_, int p_149718_3_, int p_149718_4_)
    {
        return super.canBlockStay(p_149718_1_, p_149718_2_, p_149718_3_, p_149718_4_);
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
    {
        int l = p_149674_1_.getBlockMetadata(p_149674_2_, p_149674_3_, p_149674_4_);

        if (l < 3 && p_149674_5_.nextInt(10) == 0)
        {
            ++l;
            p_149674_1_.setBlockMetadataWithNotify(p_149674_2_, p_149674_3_, p_149674_4_, l, 2);
        }

        super.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return p_149691_2_ >= 3 ? this.icons[2] : (p_149691_2_ > 0 ? this.icons[1] : this.icons[0]);
    }

    public BlockStormSawtooth setUnlocalizedNameAndID(String name){
        setBlockName(name);
        setBlockTextureName(name);
        return this;
    }

    /**
     * Drops the block items with a specified chance of dropping the specified items
     */
    @SuppressWarnings("unused")
    public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
    {
        super.dropBlockAsItemWithChance(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_6_, p_149690_7_);
    }

    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return null;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random p_149745_1_)
    {
        return 0;
    }

    /**
     * Gets an item for the block being called on. Args: world, x, y, z
     */
    @SideOnly(Side.CLIENT)
    public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
    {
        return ItemsCommonProxy.stormSawtoothSeeds;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {
        this.icons = new IIcon[3];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = p_149651_1_.registerIcon(this.getTextureName() + "_stage_" + i);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        if (metadata >= 3 && world.getWorldInfo().isThundering())
        {
            ret.add(new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_STORMSAWTOOTH));
        }

        ret.add(new ItemStack(ItemsCommonProxy.stormSawtoothSeeds));

        return ret;
    }
}
