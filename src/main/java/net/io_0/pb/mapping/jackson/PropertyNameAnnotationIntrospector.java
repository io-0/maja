package net.io_0.pb.mapping.jackson;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import net.io_0.pb.mapping.PropertyName;
import java.util.Arrays;
import java.util.List;

public class PropertyNameAnnotationIntrospector extends NopAnnotationIntrospector {
  @Override
  public List<com.fasterxml.jackson.databind.PropertyName> findPropertyAliases(Annotated annotated) {
    if (annotated.hasAnnotation(PropertyName.class))
      return List.of(com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(PropertyName.class).value()));
    return null;
  }

  @Override
  public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
    return Arrays.stream(enumType.getFields()).map(field ->
      field.isAnnotationPresent(PropertyName.class) ? field.getAnnotation(PropertyName.class).value() : field.getName()
    ).toArray(String[]::new);
  }

  @Override
  public com.fasterxml.jackson.databind.PropertyName findNameForSerialization(Annotated annotated) {
    if (annotated.hasAnnotation(PropertyName.class))
      return com.fasterxml.jackson.databind.PropertyName.construct(annotated.getAnnotation(PropertyName.class).value());
    return null;
  }
}
