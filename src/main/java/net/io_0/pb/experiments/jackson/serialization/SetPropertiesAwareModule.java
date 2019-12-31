package net.io_0.pb.experiments.jackson.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.experiments.SetPropertiesAware;

import java.util.List;

/**
 * We want to be able to explicitly set null as value for a property but we don't want all properties serialized that were initialised with null.
 * It enables us to work with null e.g. RFC 7386 - JSON Merge Patch.
 * This module only works if SetPropertiesAware.class is implemented, setters use it and the serialization skips properties with null values.
 * The first condition defines us that we serialize null if a property was actively set to null via setter.
 * The second one can be archived for instance with the serializationInclusion(JsonInclude.Include.NON_EMPTY) configuration.
 */
@Slf4j
public class SetPropertiesAwareModule extends Module {
  public static final String MODULE_NAME = "SetPropertiesAwareModule";

  @Override
  public String getModuleName() {
    return MODULE_NAME;
  }

  @Override
  public Version version() {
    return new Version(1, 0, 0, null);
  }

  @Override
  public void setupModule(Module.SetupContext context) {
    context.addBeanSerializerModifier(serializerModifier);
  }

  private static BeanSerializerModifier serializerModifier = new BeanSerializerModifier() {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      if (SetPropertiesAware.class.isAssignableFrom(beanDesc.getBeanClass())) {
        for (int i = 0; i < beanProperties.size(); i++) {
          BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);

          // wrap original BeanPropertyWriter
          BeanPropertyWriter writer = new CustomBeanPropertyWriter(beanPropertyWriter, beanPropertyWriter.getName());
          beanProperties.set(i, writer);
        }
      }

      return super.changeProperties(config, beanDesc, beanProperties);
    }

    class CustomBeanPropertyWriter extends BeanPropertyWriter {
      private CustomBeanPropertyWriter(BeanPropertyWriter base, String newSimpleName) {
        super(base, base.getFullName().withSimpleName(newSimpleName));
      }

      @Override
      public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        if (SetPropertiesAware.class.isAssignableFrom(bean.getClass())) {
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
  };
}
