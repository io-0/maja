package net.io_0.maja.mapping.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.io_0.maja.PropertyBuildingUtils;

/**
 * Jackson has "problems" with names like 'aSpecialName' because of the bean naming conventions it applies.
 * This naming strategy will undo the jackson 'ASpecialName' conversion that happens.
 * It requires MapperFeature.USE_STD_BEAN_NAMING to work.
 */
public class FirstCharToLowerPropertyNamingStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {
  @Override
  public String translate(String input) {
    return PropertyBuildingUtils.firstCharToLowerCase(input);
  }
}
