package net.io_0.maja.mapping.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import java.util.Optional;

import static net.io_0.maja.StringUtils.*;

/**
 * Jackson has "problems" with names like 'aSpecialName' because of the bean naming conventions it applies.
 * This naming strategy tries to find the field with lower or upper case first character.
 * It requires MapperFeature.USE_STD_BEAN_NAMING to work.
 */
public class FirstCharCaseIgnoredPropertyNamingStrategy extends PropertyNamingStrategies.NamingBase {
  @Override
  public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
    return translate(defaultName, method.getDeclaringClass());
  }

  @Override
  public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
    return translate(defaultName, method.getDeclaringClass());
  }

  @Override
  public String translate(String input) {
    return input;
  }

  private String translate(String name, Class<?> type) {
    return toFieldName(name, type)
      .or(() -> toFieldName(firstCharToLowerCase(name), type))
      .or(() -> toFieldName(firstCharToUpperCase(name), type))
      .orElse(null);
  }

  private Optional<String> toFieldName(String name, Class<?> type) {
    try {
      type.getDeclaredField(name);
      return Optional.of(name);
    } catch (NoSuchFieldException e) {
      return Optional.empty();
    }
  }
}
