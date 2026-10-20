package am2.configuration;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashMap;


public class SkillConfiguration extends Configuration{
	private final Object2BooleanOpenHashMap<String> cache;

	public SkillConfiguration(File file){
		super(file);
		cache = new Object2BooleanOpenHashMap<String>();
		load();
		addCustomCategoryComment(CATEGORY_GENERAL, "Set any of these to false to disable the skill in-game.");
	}

	public boolean isSkillEnabled(String identifier){
		if (cache.containsKey(identifier))
			return cache.getBoolean(identifier);
		boolean value = get(CATEGORY_GENERAL, identifier, true).getBoolean(true);
		cache.put(identifier, value);
		return value;
	}

	public void init(){

	}
}
