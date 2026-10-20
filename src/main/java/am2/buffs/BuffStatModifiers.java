package am2.buffs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.UUID;

public class BuffStatModifiers{
	public static final BuffStatModifiers instance = new BuffStatModifiers();

	/**
	 * Keeps the AM2 attribute modifiers in step with the entity's buffs. This runs for every living entity every tick,
	 * so an entity with no active potion and no modifier left to clean up returns before any attribute lookup
	 * (server attribute maps lower-case the attribute name on every lookup).
	 *
	 * @param mayHaveModifiers false only when the previous call returned false, i.e. no AM2 buff modifier can be applied
	 * @return whether an AM2 buff modifier is still applied
	 */
	public boolean applyStatModifiersBasedOnBuffs(EntityLivingBase entity, boolean mayHaveModifiers){
		if (!mayHaveModifiers && entity.getActivePotionEffects().isEmpty())
			return false;
		boolean applied = applyOrRemoveModifier(entity, BuffList.entangled.id, SharedMonsterAttributes.movementSpeed, entangled);
		applied |= applyOrRemoveScalingModifier(entity, BuffList.frostSlowed.id, SharedMonsterAttributes.movementSpeed, frostSlow);
		applied |= applyOrRemoveModifier(entity, BuffList.fury.id, SharedMonsterAttributes.movementSpeed, furyMoveMod);
		applied |= applyOrRemoveModifier(entity, BuffList.fury.id, SharedMonsterAttributes.attackDamage, furyDmgMod);
		applied |= applyOrRemoveScalingModifier(entity, BuffList.haste.id, SharedMonsterAttributes.movementSpeed, hasteSpeedBoost);
		return applied;
	}

	public void applyStatModifiersBasedOnBuffs(EntityLivingBase entity){
		applyStatModifiersBasedOnBuffs(entity, true);
	}

	private boolean applyOrRemoveModifier(EntityLivingBase entity, int buffID, IAttribute attribute, AttributeModifier modifier){
		IAttributeInstance inst = entity.getEntityAttribute(attribute);
		if (inst == null)
			return false;
		AttributeModifier currentModifier = inst.getModifier(modifier.getID());
		if (entity.isPotionActive(buffID)){
			if (currentModifier != modifier){
				if (currentModifier != null)
					inst.removeModifier(currentModifier);
				inst.applyModifier(modifier);
			}
			return true;
		}
		if (currentModifier == modifier)
			inst.removeModifier(currentModifier);
		return false;
	}

	private boolean applyOrRemoveScalingModifier(EntityLivingBase entity, int potionID, IAttribute attribute, AttributeModifier[] modifiers){
		IAttributeInstance inst = entity.getEntityAttribute(attribute);
		if (inst == null)
			return false;
		AttributeModifier currentModifier = inst.getModifier(modifiers[0].getID());
		PotionEffect effect = entity.getActivePotionEffect(Potion.potionTypes[potionID]);
		if (effect != null){
			AttributeModifier modifier = modifiers[Math.min(effect.getAmplifier(), modifiers.length - 1)];
			if (currentModifier != modifier){
				if (currentModifier != null)
					inst.removeModifier(currentModifier);
				inst.applyModifier(modifier);
			}
			return true;
		}
		if (currentModifier != null)
			inst.removeModifier(currentModifier);
		return false;
	}

	//*  Fury  *//
	private static final UUID furyMoveID = UUID.fromString("03B0A79B-9769-43AE-BFE3-830D993D4A69");
	private static final AttributeModifier furyMoveMod = (new AttributeModifier(furyMoveID, "Fury (Movement)", 2, 2));

	private static final UUID furyDmgID = UUID.fromString("03B0A79B-9769-43AE-BFE3-830D993D4A70");
	private static final AttributeModifier furyDmgMod = (new AttributeModifier(furyDmgID, "Fury (Damage)", 5, 2));

	//*  Entangle  *//
	private static final UUID entangledID = UUID.fromString("F5047292-E5F9-4EB1-986E-9A5DFE832203");
	private static final AttributeModifier entangled = (new AttributeModifier(entangledID, "Entangled", -10, 2));

	//*  Haste *//
	private static final UUID hasteID = UUID.fromString("CA4D2B5D-D509-49C0-9B2C-C0A338883AB1");
	private static final AttributeModifier[] hasteSpeedBoost = {
			new AttributeModifier(hasteID, "Haste Speed Boost (Diminished)", 0.3D, 2),
			new AttributeModifier(hasteID, "Haste Speed Boost (Normal)", 0.6D, 2),
			new AttributeModifier(hasteID, "Haste Speed Boost (Augmented)", 1.2D, 2)};

	//*  Frost Slow *//
	private static final UUID frostSlowID = UUID.fromString("03B0A79B-9569-43AE-BFE3-820D993D4A64");
	private static final AttributeModifier[] frostSlow = {
			new AttributeModifier(frostSlowID, "Frost Slow (Diminished)", -0.2, 2),
			new AttributeModifier(frostSlowID, "Frost Slow (Normal)", -0.5, 2),
			new AttributeModifier(frostSlowID, "Frost Slow (Augmented)", -0.8, 2)};
}
