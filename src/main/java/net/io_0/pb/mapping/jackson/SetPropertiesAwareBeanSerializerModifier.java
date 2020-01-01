package net.io_0.pb.mapping.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import net.io_0.pb.SetPropertiesAware;
import java.util.List;

/**
 * We want to be able to explicitly set null as value for a property but we don't want all properties serialized that were initialised with null.
 * It enables us to work with null e.g. RFC 7386 - JSON Merge Patch.
 * This modifier only works if SetPropertiesAware.class is extended, setters use it and the serialization skips properties with null values.
 * The first condition defines us that we serialize null if a property was actively set to null via setter.
 * The second one can be archived for instance with the serializationInclusion(JsonInclude.Include.NON_EMPTY) configuration.
 */
public class SetPropertiesAwareBeanSerializerModifier extends BeanSerializerModifier {
  @Override
  public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
    if (extendsSetPropertiesAware(beanDesc.getBeanClass())) {
      for (int i = 0; i < beanProperties.size(); i++) {
        BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);

        // wrap original BeanPropertyWriter
        BeanPropertyWriter writer = new SetPropertiesAwareBeanPropertyWriter(beanPropertyWriter, beanPropertyWriter.getName());
        beanProperties.set(i, writer);
      }
    }

    return super.changeProperties(config, beanDesc, beanProperties);
  }

  private static boolean extendsSetPropertiesAware(Class<?> type) {
    return SetPropertiesAware.class.isAssignableFrom(type);
  }

  static private class SetPropertiesAwareBeanPropertyWriter extends BeanPropertyWriter {
    private SetPropertiesAwareBeanPropertyWriter(BeanPropertyWriter base, String newSimpleName) {
      super(base, base.getFullName().withSimpleName(newSimpleName));
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
      if (extendsSetPropertiesAware(bean.getClass())) {
        SetPropertiesAware model = (SetPropertiesAware) bean;

        if (model.isPropertySet(getName())) {
          final Object value = (_accessorMethod == null) ? _field.get(bean) : _accessorMethod.invoke(bean, (Object[]) null);

          // write null despite any settings if property was set to null via setter
          if (value == null) {
            gen.writeNullField(getName());
          }
        }
      }

      super.serializeAsField(bean, gen, prov);
    }
  }
}
