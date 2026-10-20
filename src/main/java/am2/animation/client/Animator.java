package am2.animation.client;

import am2.animation.AnimationSync;
import am2.animation.IAnimatedEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;

/**
 * Keyframe animator for a model: phases add rotations and offsets to model boxes and are eased between with a sine
 * curve. Port of the AnimationAPI animator.
 */
@SideOnly(Side.CLIENT)
public class Animator{

	public static final float PI = (float)Math.PI;

	private final ModelBase mainModel;
	private final Reference2ObjectOpenHashMap<ModelRenderer, Transform> transformMap = new Reference2ObjectOpenHashMap<ModelRenderer, Transform>();
	private final Reference2ObjectOpenHashMap<ModelRenderer, Transform> prevTransformMap = new Reference2ObjectOpenHashMap<ModelRenderer, Transform>();
	private int tempTick, prevTempTick;
	private boolean correctAnim;
	private IAnimatedEntity animEntity;

	public Animator(ModelBase model){
		mainModel = model;
	}

	public IAnimatedEntity getEntity(){
		return animEntity;
	}

	public void update(IAnimatedEntity entity){
		tempTick = prevTempTick = 0;
		correctAnim = false;
		animEntity = entity;
		transformMap.clear();
		prevTransformMap.clear();
		for (int i = 0; i < mainModel.boxList.size(); i++){
			ModelRenderer box = (ModelRenderer)mainModel.boxList.get(i);
			box.rotateAngleX = 0F;
			box.rotateAngleY = 0F;
			box.rotateAngleZ = 0F;
		}
	}

	public boolean setAnim(int animID){
		tempTick = prevTempTick = 0;
		correctAnim = animEntity.getAnimID() == animID;
		return correctAnim;
	}

	public void startPhase(int duration){
		if (!correctAnim) return;
		prevTempTick = tempTick;
		tempTick += duration;
	}

	public void setStationaryPhase(int duration){
		startPhase(duration);
		endPhase(true);
	}

	public void resetPhase(int duration){
		startPhase(duration);
		endPhase();
	}

	public void rotate(ModelRenderer box, float x, float y, float z){
		if (!correctAnim) return;
		Transform transform = transformMap.get(box);
		if (transform == null)
			transformMap.put(box, new Transform(x, y, z));
		else
			transform.addRot(x, y, z);
	}

	public void move(ModelRenderer box, float x, float y, float z){
		if (!correctAnim) return;
		Transform transform = transformMap.get(box);
		if (transform == null)
			transformMap.put(box, new Transform(x, y, z, 0F, 0F, 0F));
		else
			transform.addOffset(x, y, z);
	}

	public void endPhase(){
		endPhase(false);
	}

	private void endPhase(boolean stationary){
		if (!correctAnim) return;
		int animTick = animEntity.getAnimTick();
		if (animTick >= prevTempTick && animTick < tempTick){
			if (stationary){
				apply(prevTransformMap, 1F);
			}else{
				float tick = (animTick - prevTempTick + AnimationSync.partialTicks) / (tempTick - prevTempTick);
				float inc = MathHelper.sin(tick * PI / 2F), dec = 1F - inc;
				apply(prevTransformMap, dec);
				apply(transformMap, inc);
			}
		}
		if (!stationary){
			prevTransformMap.clear();
			prevTransformMap.putAll(transformMap);
			transformMap.clear();
		}
	}

	private static void apply(Reference2ObjectOpenHashMap<ModelRenderer, Transform> transforms, float weight){
		for (Reference2ObjectMap.Entry<ModelRenderer, Transform> entry : transforms.reference2ObjectEntrySet()){
			ModelRenderer box = entry.getKey();
			Transform transform = entry.getValue();
			box.rotateAngleX += weight * transform.rotX;
			box.rotateAngleY += weight * transform.rotY;
			box.rotateAngleZ += weight * transform.rotZ;
			box.rotationPointX += weight * transform.offsetX;
			box.rotationPointY += weight * transform.offsetY;
			box.rotationPointZ += weight * transform.offsetZ;
		}
	}
}
