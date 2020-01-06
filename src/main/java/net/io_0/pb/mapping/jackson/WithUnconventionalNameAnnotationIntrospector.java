package net.io_0.pb.mapping.jackson;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import net.io_0.pb.WithUnconventionalName;
import java.util.Arrays;
import java.util.List;

public class WithUnconventionalNameAnnotationIntrospector extends NopAnnotationIntrospector {
  @Override
  public List<com.fasterxml.jackson.databind.PropertyName> findPropertyAliases(Annotated annotated) {
    if (annotated.hasAnnotation(WithUnconventionalName.class))
      return List.of(com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(WithUnconventionalName.class).value()));
    return null;
  }

  @Override
  public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
    return Arrays.stream(enumType.getFields()).map(field ->
      field.isAnnotationPresent(WithUnconventionalName.class) ? field.getAnnotation(WithUnconventionalName.class).value() : field.getName()
    ).toArray(String[]::new);
  }

  @Override
  public com.fasterxml.jackson.databind.PropertyName findNameForSerialization(Annotated annotated) {
    if (annotated.hasAnnotation(WithUnconventionalName.class))
      return com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(WithUnconventionalName.class).value());
    return null;
  }
}
