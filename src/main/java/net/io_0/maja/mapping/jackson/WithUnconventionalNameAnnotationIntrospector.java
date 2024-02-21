package net.io_0.maja.mapping.jackson;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import net.io_0.maja.WithUnconventionalName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WithUnconventionalNameAnnotationIntrospector extends NopAnnotationIntrospector {
  @Override
  public List<com.fasterxml.jackson.databind.PropertyName> findPropertyAliases(Annotated annotated) {
    if (annotated.hasAnnotation(WithUnconventionalName.class)) {
      return List.of(com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(WithUnconventionalName.class).value()));
    }
    return Collections.emptyList();
  }

  @Override
  public String[] findEnumValues(MapperConfig<?> config, AnnotatedClass annotatedClass, Enum<?>[] enumValues, String[] names) {
    return Arrays.stream(annotatedClass.getAnnotated().getFields()).map(field ->
      field.isAnnotationPresent(WithUnconventionalName.class) ? field.getAnnotation(WithUnconventionalName.class).value() : field.getName()
    ).toArray(String[]::new);
  }

  @Override
  public com.fasterxml.jackson.databind.PropertyName findNameForSerialization(Annotated annotated) {
    if (annotated.hasAnnotation(WithUnconventionalName.class)) {
      return com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(WithUnconventionalName.class).value());
    }
    return null;
  }

  /**
   * Jackson has "problems" with names like 'aSpecialName' because of the bean naming conventions it applies.
   * It won't find the annotation if we don't intervene.
   */
  @Override
  public String findImplicitPropertyName(AnnotatedMember member) {
    String name = member.getName();
    if (breaksNamingConvention(name)) {
      return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    return null;
  }

  private boolean breaksNamingConvention(String name) {
    return name.length() > 2 && Character.isLowerCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1));
  }
}
