package net.io_0.pb.mapping.jackson;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import net.io_0.pb.mapping.JsonName;
import java.util.Arrays;
import java.util.List;

public class JsonNameAnnotationIntrospector extends NopAnnotationIntrospector {
  @Override
  public List<PropertyName> findPropertyAliases(Annotated annotated) {
    if (annotated.hasAnnotation(JsonName.class))
      return List.of(PropertyName.construct(annotated.getAnnotation(JsonName.class).value()));
    return null;
  }

  @Override
  public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
    return Arrays.stream(enumType.getFields()).map(field ->
      field.isAnnotationPresent(JsonName.class) ? field.getAnnotation(JsonName.class).value() : field.getName()
    ).toArray(String[]::new);
  }

  @Override
  public PropertyName findNameForSerialization(Annotated annotated) {
    if (annotated.hasAnnotation(JsonName.class))
      return PropertyName.construct(annotated.getAnnotation(JsonName.class).value());
    return null;
  }
}
