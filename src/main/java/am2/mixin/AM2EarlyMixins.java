package am2.mixin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registers AM2's patches to vanilla classes. Replaces the old coremod (MysteriumASM fixes plus hand-written ASM), which
 * also scanned the game directory to detect other mods. Mod detection now comes from the loaded coremod list FML
 * passes in.
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class AM2EarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader{

	/** DragonAPI extends potion IDs itself; applying both breaks the packets. */
	private static final String DRAGONAPI_PLUGIN = "Reika.DragonAPI.Auxiliary.DragonAPIASMHandler";

	@Override
	public String getMixinConfig(){
		return "mixins.arsmagica2.early.json";
	}

	@Override
	public List<String> getMixins(Set<String> loadedCoreMods){
		List<String> mixins = new ArrayList<String>();
		mixins.add("MixinBlockDynamicLiquid");
		mixins.add("MixinBlockLiquid");
		mixins.add("MixinEntity");
		mixins.add("MixinEntityItem");
		mixins.add("MixinEntityLivingBase");
		mixins.add("MixinEntityPlayer");
		mixins.add("MixinNetHandlerPlayServer");
		mixins.add("MixinTileEntity");
		mixins.add("MixinWorld");

		boolean extendPotionIds = !loadedCoreMods.contains(DRAGONAPI_PLUGIN);
		if (extendPotionIds){
			mixins.add("MixinPotionEffect");
			mixins.add("MixinS1DPacketEntityEffect");
			mixins.add("MixinS1EPacketRemoveEntityEffect");
		}

		if (FMLLaunchHandler.side() == Side.CLIENT){
			mixins.add("client.MixinEffectRenderer");
			mixins.add("client.MixinEntityClient");
			mixins.add("client.MixinEntityPlayerSP");
			mixins.add("client.MixinEntityRenderer");
			mixins.add("client.MixinModelHallucination");
			mixins.add("client.MixinRealmsBridge");
			mixins.add("client.MixinRenderFish");
			mixins.add("client.MixinRendererLivingEntity");
			mixins.add("client.MixinRenderManager");
			if (extendPotionIds)
				mixins.add("client.MixinNetHandlerPlayClient");
		}
		return mixins;
	}

	@Override
	public String[] getASMTransformerClass(){
		return null;
	}

	@Override
	public String getModContainerClass(){
		return null;
	}

	@Override
	public String getSetupClass(){
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data){
	}

	@Override
	public String getAccessTransformerClass(){
		return null;
	}
}
