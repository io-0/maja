package net.io_0.maja.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pivovarit.function.ThrowingFunction;
import lombok.NoArgsConstructor;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.mapping.jackson.FirstCharCaseIgnoredPropertyNamingStrategy;
import net.io_0.maja.mapping.jackson.WithUnconventionalNameAnnotationIntrospector;
import net.io_0.maja.mapping.jackson.PropertyIssueCollectingDeserializationProblemHandler;
import net.io_0.maja.mapping.jackson.PropertyBundleBeanSerializerModifier;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Mapper {
  public static <T> T fromJson(String json, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> fromJson(json, pIC, type, subTypes));
  }

  public static <T> T fromJson(String json, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithJsonObjectMapper(oM -> prepForJsonOrYamlMapping(oM, propertyIssueConsumer)
      .readValue(json, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T readJson(Reader reader, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> readJson(reader, pIC, type, subTypes));
  }

  public static <T> T readJson(Reader reader, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithJsonObjectMapper(oM -> prepForJsonOrYamlMapping(oM, propertyIssueConsumer)
      .readValue(reader, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T fromYaml(String yaml, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> fromYaml(yaml, pIC, type, subTypes));
  }

  public static <T> T fromYaml(String yaml, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithYamlObjectMapper(oM -> prepForJsonOrYamlMapping(oM, propertyIssueConsumer)
      .readValue(yaml, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T readYaml(Reader reader, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> readYaml(reader, pIC, type, subTypes));
  }

  public static <T> T readYaml(Reader reader, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithYamlObjectMapper(oM -> prepForJsonOrYamlMapping(oM, propertyIssueConsumer)
      .readValue(reader, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T fromMap(Map<String, ?> map, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> fromMap(map, pIC, type, subTypes));
  }

  public static <T> T fromMap(Map<String, ?> map, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithJsonObjectMapper(oM -> prepForJsonOrYamlMapping(oM, propertyIssueConsumer)
      .convertValue(map, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> String toJson(T obj) {
    return mapWithJsonObjectMapper(oM -> prepForPojoMapping(oM).writeValueAsString(obj));
  }

  public static <T> void writeJson(Writer writer, T obj) {
    mapWithJsonObjectMapper(oM -> { prepForPojoMapping(oM).writeValue(writer, obj); return null; });
  }

  public static <T> String toYaml(T obj) {
    return mapWithYamlObjectMapper(oM -> prepForPojoMapping(oM).writeValueAsString(obj));
  }

  public static <T> void writeYaml(Writer writer, T obj) {
    mapWithYamlObjectMapper(oM -> { prepForPojoMapping(oM).writeValue(writer, obj); return null; });
  }

  public static <T> Map<String, Object> toMap(T obj) {
    return mapWithJsonObjectMapper(oM -> prepForPojoMapping(oM).convertValue(obj, new TypeReference<>() {}));
  }

  public static class MappingException extends RuntimeException {
    public MappingException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * @deprecated Use {@link #fromJson(String, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T fromJson(String json, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return fromJson(json, propertyIssueConsumer, type);
  }

  /**
   * @deprecated Use {@link #readJson(Reader, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T readJson(Reader reader, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return readJson(reader, propertyIssueConsumer, type);
  }

  /**
   * @deprecated Use {@link #fromMap(Map, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T fromMap(Map<String, ?> map, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return fromMap(map, propertyIssueConsumer, type);
  }
  
  private static ObjectMapper prepForJsonOrYamlMapping(ObjectMapper oM, Consumer<PropertyIssue> pIC) {
    return oM
      .disable(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
      )
      .addHandler(new PropertyIssueCollectingDeserializationProblemHandler(pIC));
  }

  private static ObjectMapper prepForPojoMapping(ObjectMapper oM) {
    return oM
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      .setSerializerFactory(oM.getSerializerFactory().withSerializerModifier(
        new PropertyBundleBeanSerializerModifier()
      ));
  }

  private static <T> T mapWithJsonObjectMapper(ThrowingFunction<ObjectMapper, T, IOException> cb) {
    return mapWithObjectMapper(new JsonFactory(), cb);
  }

  private static <T> T mapWithYamlObjectMapper(ThrowingFunction<ObjectMapper, T, IOException> cb) {
    return mapWithObjectMapper(new YAMLFactory(), cb);
  }

  private static <T> T mapWithObjectMapper(JsonFactory factory, ThrowingFunction<ObjectMapper, T, IOException> cb) {
    try {
      return cb.apply(getPreConfiguredObjectMapper(factory));
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  private static ObjectMapper getPreConfiguredObjectMapper(JsonFactory factory) {
    ObjectMapper mapper = new ObjectMapper(factory)
      .registerModules(
        new JavaTimeModule()
      )
      .setAnnotationIntrospector(new WithUnconventionalNameAnnotationIntrospector())
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
    return circumventJacksonBeanNamingConventionProblems(mapper);
  }

  private static ObjectMapper circumventJacksonBeanNamingConventionProblems(ObjectMapper mapper) {
    return mapper
      .enable(MapperFeature.USE_STD_BEAN_NAMING)
      .setPropertyNamingStrategy(new FirstCharCaseIgnoredPropertyNamingStrategy());
  }

  private static <T> T throwMappingExceptionIfIssues(Function<Consumer<PropertyIssue>, T> cb) {
    PropertyIssues propertyIssues = PropertyIssues.of();
    T t = cb.apply(propertyIssues::add);
    if (!propertyIssues.isEmpty())
      throw new MappingException(new IllegalStateException(propertyIssues.toString()));
    return t;
  }
}