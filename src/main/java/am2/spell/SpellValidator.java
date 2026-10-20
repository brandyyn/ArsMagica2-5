package am2.spell;

import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.spell.components.Summon;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;

public class SpellValidator{
	public static final SpellValidator instance = new SpellValidator();

	private enum StageValidations{
		VALID,
		TERMINUS,
		PRINCIPUM,
		NOT_VALID
	}

	private SpellValidator(){

	}

	public ValidationResult spellDefIsValid(ArrayList<ArrayList<ISpellPart>> shapeGroups, ArrayList<ArrayList<ISpellPart>> segmented){
		boolean noParts = true;
		for (ArrayList<ISpellPart> shapeGroup : shapeGroups)
			if (!shapeGroup.isEmpty()){
				noParts = false;
				break;
			}
		for (ArrayList<ISpellPart> iSpellParts : segmented)
			if (!iSpellParts.isEmpty()){
				noParts = false;
				break;
			}
		if (noParts) return new ValidationResult(null, "");

		boolean validatedAny = false;
		for (ArrayList<ISpellPart> shapeGroup : shapeGroups){
			if (!shapeGroup.isEmpty()){
				if (segmented.isEmpty()){
					ValidationResult result = internalValidation(splitToStages(shapeGroup));
					if (result != null)
						return result;
				}
				ArrayList<ISpellPart> concatenated = new ArrayList<ISpellPart>(shapeGroup);
				for (ArrayList<ISpellPart> iSpellParts : segmented){
					concatenated.addAll(iSpellParts);
				}
				ValidationResult result = internalValidation(splitToStages(concatenated));
				if (result != null)
					return result;

				validatedAny = true;
			}
		}

		if (!validatedAny){
			ValidationResult result = internalValidation(segmented);
			if (result != null)
				return result;
		}

		return new ValidationResult();
	}

	private ValidationResult internalValidation(ArrayList<ArrayList<ISpellPart>> segmented){
		for (int i = 0; i < segmented.size(); ++i){
			StageValidations result = validateStage(segmented.get(i), i == segmented.size() - 1);

			if (result == StageValidations.NOT_VALID){
				return new ValidationResult(segmented.get(i).get(0), StatCollector.translateToLocal("am2.spell.validate.compMiss"));
			}else if (result == StageValidations.PRINCIPUM && i == segmented.size() - 1){
				return new ValidationResult(segmented.get(i).get(0), String.format("%s %s", SkillManager.instance.getDisplayName(segmented.get(i).get(0)), StatCollector.translateToLocal("am2.spell.validate.principum")));
			}else if (result == StageValidations.TERMINUS && i < segmented.size() - 1){
				return new ValidationResult(segmented.get(i).get(0), String.format("%s %s", SkillManager.instance.getDisplayName(segmented.get(i).get(0)), StatCollector.translateToLocal("am2.spell.validate.terminus")));
			}
		}

		return null;
	}

	private StageValidations validateStage(ArrayList<ISpellPart> stageDefinition, boolean isFinalStage){
		boolean terminus = false;
		boolean principum = false;
		boolean one_component = !isFinalStage;
		boolean one_shape = false;
		for (ISpellPart part : stageDefinition){
			if (part instanceof Summon) return StageValidations.TERMINUS;
			if (part instanceof ISpellShape){
				one_shape = true;
				if (((ISpellShape)part).isTerminusShape())
					terminus = true;
				if (((ISpellShape)part).isPrincipumShape())
					principum = true;
				continue;
			}
			if (part instanceof ISpellComponent){
				one_component = true;
			}
		}

		if (principum)
			return StageValidations.PRINCIPUM;
		if (!one_component || !one_shape)
			return StageValidations.NOT_VALID;
		if (terminus)
			return StageValidations.TERMINUS;
		return StageValidations.VALID;
	}

	public static ArrayList<ArrayList<ISpellPart>> splitToStages(ArrayList<ISpellPart> currentRecipe){
		ArrayList<ArrayList<ISpellPart>> segmented = new ArrayList<ArrayList<ISpellPart>>();
		int idx = (!currentRecipe.isEmpty() && currentRecipe.get(0) instanceof ISpellShape) ? -1 : 0;
		for (ISpellPart part : currentRecipe){
			if (part instanceof ISpellShape)
				idx++;
			if (segmented.size() - 1 < idx) //while loop not necessary as this will keep up
				segmented.add(new ArrayList<ISpellPart>());
			segmented.get(idx).add(part);
		}
		return segmented;
	}

	public boolean modifierCanBeAdded(ISpellModifier modifier){
		return false;
	}

	public static class ValidationResult{
		public final boolean valid;
		public final ISpellPart offendingPart;
		public final String message;

		public ValidationResult(ISpellPart offendingPart, String message){
			valid = false;
			this.offendingPart = offendingPart;
			this.message = message;
		}

		public ValidationResult(){
			valid = true;
			this.offendingPart = null;
			this.message = "";
		}
	}
}
