package am2.api.blocks;

import net.minecraft.block.Block;

import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

public class BlockDec{
		Block block;
		int meta;

		public BlockDec(Block block, int meta){
			this.block = block;
			this.meta = meta;
		}

		public Block getBlock(){
			return block;
		}

		public int getMeta(){
			return meta;
		}

		@Override
		public String toString(){
			String blockName;
			if (block != null){
				blockName = block.getLocalizedName();
			}else{
				blockName = "Unknown";
			}
			return String.format("Block: %s, meta: %d", blockName, meta);
		}

		@Override
		public boolean equals(Object obj){
			if (obj instanceof BlockDec){
				BlockDec blockDec = ((BlockDec)obj);
				return this.block == blockDec.block && (this.meta == WILDCARD_VALUE || blockDec.meta == WILDCARD_VALUE || this.meta == blockDec.meta);
			}
			return false;
		}

		@Override
		public int hashCode(){
			return Block.getIdFromBlock(block);
		}

}
