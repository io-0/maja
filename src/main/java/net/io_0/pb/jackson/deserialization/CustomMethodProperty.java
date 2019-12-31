package net.io_0.pb.jackson.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;
import net.io_0.pb.SetPropertiesAware;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * MethodProperty with logic added to deserializeAndSet(..)
 */
public final class CustomMethodProperty extends SettableBeanProperty {
  private static final long serialVersionUID = 1;

  protected final AnnotatedMethod _annotated;

  /**
   * Setter method for modifying property value; used for
   * "regular" method-accessible properties.
   */
  protected final transient Method _setter;

  /**
   * @since 2.9
   */
  final protected boolean _skipNulls;

  public CustomMethodProperty(
    BeanPropertyDefinition propDef, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedMethod method) {
    super(propDef, type, typeDeser, contextAnnotations);
    _annotated = method;
    _setter = method.getAnnotated();
    _skipNulls = NullsConstantProvider.isSkipper(_nullProvider);
  }

  protected CustomMethodProperty(CustomMethodProperty src, JsonDeserializer<?> deser, NullValueProvider nva) {
    super(src, deser, nva);
    _annotated = src._annotated;
    _setter = src._setter;
    _skipNulls = NullsConstantProvider.isSkipper(nva);
  }

  protected CustomMethodProperty(CustomMethodProperty src, PropertyName newName) {
    super(src, newName);
    _annotated = src._annotated;
    _setter = src._setter;
    _skipNulls = src._skipNulls;
  }

  /**
   * Constructor used for JDK Serialization when reading persisted object
   */
  protected CustomMethodProperty(CustomMethodProperty src, Method m) {
    super(src);
    _annotated = src._annotated;
    _setter = m;
    _skipNulls = src._skipNulls;
  }

  @Override
  public SettableBeanProperty withName(PropertyName newName) {
    return new CustomMethodProperty(this, newName);
  }

  @Override
  public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
    if (_valueDeserializer == deser) {
      return this;
    }
    // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
    NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
    return new CustomMethodProperty(this, deser, nvp);
  }

  @Override
  public SettableBeanProperty withNullProvider(NullValueProvider nva) {
    return new CustomMethodProperty(this, _valueDeserializer, nva);
  }

  @Override
  public void fixAccess(DeserializationConfig config) {
    _annotated.fixAccess(config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
  }

  /*
  /**********************************************************
  /* BeanProperty impl
  /**********************************************************
   */

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> acls) {
    return (_annotated == null) ? null : _annotated.getAnnotation(acls);
  }

  @Override
  public AnnotatedMember getMember() {
    return _annotated;
  }

  /*
  /**********************************************************
  /* Overridden methods
  /**********************************************************
   */

  @Override
  public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
    Object value;
    if (p.hasToken(JsonToken.VALUE_NULL)) {
      if (_skipNulls) {
        return;
      }
      value = _nullProvider.getNullValue(ctxt);
    } else if (_valueTypeDeserializer == null) {
      value = _valueDeserializer.deserialize(p, ctxt);
      // 04-May-2018, tatu: [databind#2023] Coercion from String (mostly) can give null
      if (value == null) {
        if (_skipNulls) {
          return;
        }
        value = _nullProvider.getNullValue(ctxt);
      }
    } else {
      value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
    }
    try {
      _setter.invoke(instance, value);
      if (instance instanceof SetPropertiesAware) {
        ((SetPropertiesAware) instance).markPropertySet(_propName.getSimpleName());
      }
    } catch (Exception e) {
      _throwAsIOE(p, e, value);
    }
  }

  @Override
  public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
    Object value;
    if (p.hasToken(JsonToken.VALUE_NULL)) {
      if (_skipNulls) {
        return instance;
      }
      value = _nullProvider.getNullValue(ctxt);
    } else if (_valueTypeDeserializer == null) {
      value = _valueDeserializer.deserialize(p, ctxt);
      // 04-May-2018, tatu: [databind#2023] Coercion from String (mostly) can give null
      if (value == null) {
        if (_skipNulls) {
          return instance;
        }
        value = _nullProvider.getNullValue(ctxt);
      }
    } else {
      value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
    }
    try {
      Object result = _setter.invoke(instance, value);
      return (result == null) ? instance : result;
    } catch (Exception e) {
      _throwAsIOE(p, e, value);
      return null;
    }
  }

  @Override
  public final void set(Object instance, Object value) throws IOException {
    try {
      _setter.invoke(instance, value);
    } catch (Exception e) {
      // 15-Sep-2015, tatu: How could we get a ref to JsonParser?
      _throwAsIOE(e, value);
    }
  }

  @Override
  public Object setAndReturn(Object instance, Object value) throws IOException {
    try {
      Object result = _setter.invoke(instance, value);
      return (result == null) ? instance : result;
    } catch (Exception e) {
      // 15-Sep-2015, tatu: How could we get a ref to JsonParser?
      _throwAsIOE(e, value);
      return null;
    }
  }

  /*
  /**********************************************************
  /* JDK serialization handling
  /**********************************************************
   */

  Object readResolve() {
    return new CustomMethodProperty(this, _annotated.getAnnotated());
  }
}
