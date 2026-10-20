package am2.blocks.liquid;

import am2.texture.ResourceManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockLiquidEssence extends BlockFluidClassic{

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public static final Fluid liquidEssenceFluid = new FluidEssence();
	public static final Material liquidEssenceMaterial = new MaterialLiquid(MapColor.iceColor);

	public BlockLiquidEssence(){
		super(liquidEssenceFluid, liquidEssenceMaterial);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z){
		return 9;
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister){
		icons = new IIcon[2];

		icons[0] = ResourceManager.RegisterTexture("liquidEssenceStill", par1IconRegister);
		icons[1] = ResourceManager.RegisterTexture("liquidEssenceFlowing", par1IconRegister);

		liquidEssenceFluid.setIcons(icons[0], icons[1]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta){
		if (side <= 1)
			return icons[0]; //still
		else
			return icons[1]; //flowing
	}

	public void onNeighborBlockChange(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_, Block p_149695_5_) {
		this.func_149805_n(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_);
		super.onNeighborBlockChange(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_, p_149695_5_);

	}

	private void func_149805_n(World p_149805_1_, int p_149805_2_, int p_149805_3_, int p_149805_4_) {
		if (p_149805_1_.getBlock(p_149805_2_, p_149805_3_, p_149805_4_) == this) {
			if (this.blockMaterial == liquidEssenceMaterial) {
				boolean flag = false;

				if (flag || p_149805_1_.getBlock(p_149805_2_, p_149805_3_, p_149805_4_ - 1).getMaterial() == Material.water || p_149805_1_.getBlock(p_149805_2_, p_149805_3_, p_149805_4_ - 1).getMaterial() == Material.lava) {
					flag = true;
				}

				if (flag || p_149805_1_.getBlock(p_149805_2_, p_149805_3_, p_149805_4_ + 1).getMaterial() == Material.water || p_149805_1_.getBlock(p_149805_2_, p_149805_3_, p_149805_4_ + 1).getMaterial() == Material.lava) {
					flag = true;
				}

				if (flag || p_149805_1_.getBlock(p_149805_2_ - 1, p_149805_3_, p_149805_4_).getMaterial() == Material.water || p_149805_1_.getBlock(p_149805_2_ - 1, p_149805_3_, p_149805_4_).getMaterial() == Material.lava) {
					flag = true;
				}

				if (flag || p_149805_1_.getBlock(p_149805_2_ + 1, p_149805_3_, p_149805_4_).getMaterial() == Material.water || p_149805_1_.getBlock(p_149805_2_ + 1, p_149805_3_, p_149805_4_).getMaterial() == Material.lava) {
					flag = true;
				}

				if (flag || p_149805_1_.getBlock(p_149805_2_, p_149805_3_ + 1, p_149805_4_).getMaterial() == Material.water || p_149805_1_.getBlock(p_149805_2_, p_149805_3_ + 1, p_149805_4_).getMaterial() == Material.lava) {
					flag = true;
				}

				if (flag) {
					int l = p_149805_1_.getBlockMetadata(p_149805_2_, p_149805_3_, p_149805_4_);

					if (l == 0) {
						p_149805_1_.setBlock(p_149805_2_, p_149805_3_, p_149805_4_, Blocks.stained_glass);
					} else if (l <= 4) {
						p_149805_1_.setBlock(p_149805_2_, p_149805_3_, p_149805_4_, Blocks.stained_glass, 3, 3);
					}

					this.func_149799_m(p_149805_1_, p_149805_2_, p_149805_3_, p_149805_4_);
				}
			}
		}
	}

	protected void func_149799_m(World p_149799_1_, int p_149799_2_, int p_149799_3_, int p_149799_4_) {
		p_149799_1_.playSoundEffect((double)((float)p_149799_2_ + 0.5F), (double)((float)p_149799_3_ + 0.5F), (double)((float)p_149799_4_ + 0.5F), "random.fizz", 0.5F, 2.6F + (p_149799_1_.rand.nextFloat() - p_149799_1_.rand.nextFloat()) * 0.8F);

		for (int l = 0; l < 8; ++l) {
			p_149799_1_.spawnParticle("largesmoke", (double)p_149799_2_ + Math.random(), (double)p_149799_3_ + 1.2D, (double)p_149799_4_ + Math.random(), 0.0D, 0.0D, 0.0D);
		}
	}

}
