package am2.proxy.tick;

import am2.AMCore;
import am2.TimeManipulation;
import am2.EntityItemWatcher;
import am2.MeteorSpawnHelper;
import am2.bosses.BossSpawnHelper;
import am2.items.ItemsCommonProxy;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.network.AMPacketIDs;
import am2.spell.SpellHelper;
import am2.utility.DimensionUtilities;
import am2.worldgen.RetroactiveWorldgenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static am2.AMEventHandler.tempCurseMap;
import static am2.spell.SpellHelper.lingeringSpellList;

public class ServerTickHandler{

	public static HashMap<EntityLiving, EntityLivingBase> targetsToSet = new HashMap<EntityLiving, EntityLivingBase>();

	public static String lastWorldName;

	private void gameTick_Start(){
		// synced to joining players for per-save compendium data
		String folder = MinecraftServer.getServer().getFolderName();
		if (!folder.equals(lastWorldName))
			lastWorldName = folder;
		AMCore.proxy.itemFrameWatcher.checkWatchedFrames();
	}

	private void gameTick_End(){
		BossSpawnHelper.instance.tick();
		MeteorSpawnHelper.instance.tick();
		EntityItemWatcher.instance.tick();
		TimeManipulation.onServerTickEnd();
		tickTemporaryCurses();
		AMCore.proxy.applyDeferredPotionEffects();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event){
		if (event.phase == TickEvent.Phase.START){
			gameTick_Start();
		}else if (event.phase == TickEvent.Phase.END){
			gameTick_End();
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event){
		// world tick events fire twice per tick for every dimension; per-tick work runs once, at END
		if (event.phase != TickEvent.Phase.END)
			return;
		if (AMCore.config.retroactiveWorldgen())
			RetroactiveWorldgenerator.instance.continueRetrogen(event.world);

		AMCore.proxy.applyDeferredDimensionTransfers();

		if (!lingeringSpellList.isEmpty())
			SpellHelper.tickLingeringSpells(lingeringSpellList, event.world);
	}

	/** Summoned temporary curse creatures; each entry counts down once per server tick. */
	private void tickTemporaryCurses(){
		if (tempCurseMap.isEmpty())
			return;
		for (ObjectIterator<Reference2IntMap.Entry<EntityCreature>> it = Reference2IntMaps.fastIterator(tempCurseMap); it.hasNext(); ){
			Reference2IntMap.Entry<EntityCreature> entry = it.next();
			int left = entry.getIntValue();
			if (left <= 3 || entry.getKey().isDead){
				EntityCreature creature = entry.getKey();
				it.remove();
				creature.setDead();
			}else{
				entry.setValue(left - 1);
			}
		}
	}


	private void applyDeferredTargetSets(){
		Iterator<Entry<EntityLiving, EntityLivingBase>> it = targetsToSet.entrySet().iterator();
		while (it.hasNext()){
			Entry<EntityLiving, EntityLivingBase> entry = it.next();
			if (entry.getKey() != null && !entry.getKey().isDead)
				entry.getKey().setAttackTarget(entry.getValue());
			it.remove();
		}
	}

	public void addDeferredTarget(EntityLiving ent, EntityLivingBase target){
		targetsToSet.put(ent, target);
	}

	public void blackoutArmorPiece(EntityPlayerMP player, int slot, int cooldown){
		AMNetHandler.INSTANCE.sendPacketToClientPlayer(player, AMPacketIDs.FLASH_ARMOR_PIECE, new AMDataWriter().add(slot).add(cooldown).generate());
	}

}
