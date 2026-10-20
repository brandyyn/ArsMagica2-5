package am2.network;

import am2.AMCore;
import am2.LogHelper;
import am2.utility.WebRequestUtils;
import cpw.mods.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Random spell name suggestions from Seventh Sanctum. Names are fetched on a background thread the first time the spell
 * naming GUI asks for one (and again when they run out), so neither game start nor the GUI waits on the network.
 * This used to be a blocking request without timeouts in postInit, on dedicated servers too.
 */
public class SeventhSanctum{
	private static final String webURL = "http://www.seventhsanctum.com/generate.php?Genname=spell";

	private static final HashMap<String, String> postOptions = new HashMap<String, String>();

	public static final SeventhSanctum instance = new SeventhSanctum();

	private final ConcurrentLinkedQueue<String> suggestions = new ConcurrentLinkedQueue<String>();
	private volatile boolean failed = false;
	private volatile boolean fetching = false;

	private SeventhSanctum(){
	}

	public void init(){
		postOptions.clear();
		postOptions.put("selGenCount", "25");
		postOptions.put("selGenType", "SEEDALL");
		if (!AMCore.config.suggestSpellNames())
			failed = true;
		else if (FMLCommonHandler.instance().getSide().isClient())
			fetchAsync(); // background prefetch; only clients name spells
	}

	/** @return a suggestion, or "" when none has arrived yet (a fetch is then started in the background) */
	public String getNextSuggestion(){
		if (failed) return "";

		String suggestion = suggestions.poll();
		if (suggestions.isEmpty())
			fetchAsync();
		return suggestion == null ? "" : suggestion;
	}

	private synchronized void fetchAsync(){
		if (fetching || failed) return;
		fetching = true;
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					getSuggestions();
				}finally{
					fetching = false;
				}
			}
		}, "AM2 spell name suggestions");
		thread.setDaemon(true);
		thread.start();
	}

	private void getSuggestions(){
		try{
			String s = WebRequestUtils.sendPost(webURL, postOptions);
			int startIndex = s.lastIndexOf("SubSubContentTitle");
			if (startIndex == -1) return;
			startIndex = s.indexOf("<!--Title -->", startIndex) + 13;
			int endIndex = s.indexOf("&nbsp;", startIndex);
			if (endIndex == -1) return;

			s = s.substring(startIndex, endIndex);
			s = s.replace("\t", "");
			String[] suggestions = s.split("<div class=\"GeneratorResult");
			for (String suggestion : suggestions)
				parseAndAddSuggestion(suggestion);
		}catch (Throwable t){
			LogHelper.warn("Spell name suggestions are unavailable: %s", t);
			failed = true;
		}
	}

	private void parseAndAddSuggestion(String s){
		if (!s.endsWith("</div>")) return;

		int startIndex = s.indexOf(">");
		if (startIndex == -1) return;
		int endIndex = s.indexOf("<", startIndex);
		if (endIndex == -1) return;

		s = s.substring(startIndex + 1, endIndex);
		if (s.length() <= 20)
			suggestions.add(s);
	}
}
