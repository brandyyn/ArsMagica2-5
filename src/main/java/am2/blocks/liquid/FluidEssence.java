package am2.blocks.liquid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidEssence extends Fluid{

	public FluidEssence(){
		super("liquidEssence");
		setDensity(2000);
		setViscosity(700);

		FluidRegistry.registerFluid(this);
	}

}
