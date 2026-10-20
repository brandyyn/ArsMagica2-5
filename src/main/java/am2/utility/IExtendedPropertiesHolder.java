package am2.utility;

import am2.playerextensions.ExtendedProperties;

/**
 * Implemented on EntityLivingBase by am2.mixin: the entity's AM2 data as a field, instead of a string-keyed lookup in
 * Forge's extended properties map on every ExtendedProperties.For call.
 */
public interface IExtendedPropertiesHolder{

	ExtendedProperties am2$getExtendedProperties();

	void am2$setExtendedProperties(ExtendedProperties properties);
}
