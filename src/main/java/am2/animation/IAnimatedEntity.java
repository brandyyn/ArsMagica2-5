package am2.animation;

/**
 * An entity with keyframed model animations (the Ender and Lightning Guardians). Formerly provided by the external
 * AnimationAPI mod; the animation ID is synced to nearby clients by {@link AnimationSync}.
 */
public interface IAnimatedEntity{

	void setAnimID(int id);

	void setAnimTick(int tick);

	int getAnimID();

	int getAnimTick();
}
