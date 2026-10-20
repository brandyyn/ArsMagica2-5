package am2.api.blocks;

public class BlockCoord implements Comparable<BlockCoord>{
	public int x;
	public int y;
	public int z;

	public BlockCoord(int offsetX, int offsetY, int offsetZ){
		this.x = offsetX;
		this.y = offsetY;
		this.z = offsetZ;
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof BlockCoord){
			return this.x == ((BlockCoord)obj).x && this.y == ((BlockCoord)obj).y && this.z == ((BlockCoord)obj).z;
		}
		return false;
	}

	@Override
	public int hashCode(){
		return this.x + this.y + this.z;
	}


	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public int getZ(){
		return this.z;
	}

	public int getX(int dir){
		switch (dir) {
		case 1:
			return -this.getZ();
		case 2:
			return -this.getX();
		case 3:
			return this.getZ();
		default:
			return this.getX();
		}
	}

	public int getZ(int dir){
		switch (dir) {
		case 1:
			return this.getX();
		case 2:
			return -this.getZ();
		case 3:
			return -this.getX();
		default:
			return this.getZ();
		}
	}

	@Override
	public String toString(){
		return String.format("BlockCoord: %d, %d, %d", x, y, z);
	}

	@Override
	public int compareTo(BlockCoord o){
		return this.z > o.z ? 1 : this.z < o.z ? -1 : this.x > o.x ? 1 : this.x < o.x ? -1 : Integer.compare(this.y, o.y);
	}
}
