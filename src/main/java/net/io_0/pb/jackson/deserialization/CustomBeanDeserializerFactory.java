package net.io_0.pb.jackson.deserialization;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.FieldProperty;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * BeanDeserializerFactory with CustomMethodProperty instead of MethodProperty
 */
public class CustomBeanDeserializerFactory extends BeanDeserializerFactory {
  public final static CustomBeanDeserializerFactory instance = new CustomBeanDeserializerFactory(new DeserializerFactoryConfig());

  public CustomBeanDeserializerFactory(DeserializerFactoryConfig config) {
    super(config);
  }

  @Override
  public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
    if (_factoryConfig == config) return this;
    return new CustomBeanDeserializerFactory(config);
  }

  @Override
  protected SettableBeanProperty constructSettableProperty(
    DeserializationContext ctxt, BeanDescription beanDesc, BeanPropertyDefinition propDef, JavaType propType0
  ) throws JsonMappingException {
    // need to ensure method is callable (for non-public)
    AnnotatedMember mutator = propDef.getNonConstructorMutator();
    // 08-Sep-2016, tatu: issues like [databind#1342] suggest something fishy
    //   going on; add sanity checks to try to pin down actual problem...
    //   Possibly passing creator parameter?
    if (mutator == null) {
      ctxt.reportBadPropertyDefinition(beanDesc, propDef, "No non-constructor mutator available");
    }
    JavaType type = resolveMemberAndTypeAnnotations(ctxt, mutator, propType0);
    // Does the Method specify the deserializer to use? If so, let's use it.
    TypeDeserializer typeDeser = type.getTypeHandler();
    SettableBeanProperty prop;
    if (mutator instanceof AnnotatedMethod) {
      prop = new CustomMethodProperty(propDef, type, typeDeser,
        beanDesc.getClassAnnotations(), (AnnotatedMethod) mutator);
    } else {
      // 08-Sep-2016, tatu: wonder if we should verify it is `AnnotatedField` to be safe?
      prop = new FieldProperty(propDef, type, typeDeser,
        beanDesc.getClassAnnotations(), (AnnotatedField) mutator);
    }
    JsonDeserializer<?> deser = findDeserializerFromAnnotation(ctxt, mutator);
    if (deser == null) {
      deser = type.getValueHandler();
    }
    if (deser != null) {
      deser = ctxt.handlePrimaryContextualization(deser, prop, type);
      prop = prop.withValueDeserializer(deser);
    }
    // need to retain name of managed forward references:
    AnnotationIntrospector.ReferenceProperty ref = propDef.findReferenceType();
    if (ref != null && ref.isManagedReference()) {
      prop.setManagedReferenceName(ref.getName());
    }
    ObjectIdInfo objectIdInfo = propDef.findObjectIdInfo();
    if (objectIdInfo != null){
      prop.setObjectIdInfo(objectIdInfo);
    }
    return prop;
  }
}
