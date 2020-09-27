package net.io_0.maja.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pivovarit.function.ThrowingFunction;
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

public class Mapper {
  public static <T> T fromJson(String json, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> fromJson(json, pIC, type, subTypes));
  }

  /**
   * @deprecated Use {@link #fromJson(String, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T fromJson(String json, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return fromJson(json, propertyIssueConsumer, type);
  }

  public static <T> T fromJson(String json, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithObjectMapper(oM -> prepForJsonMapping(oM, propertyIssueConsumer)
      .readValue(json, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T readJson(Reader reader, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> readJson(reader, pIC, type, subTypes));
  }

  /**
   * @deprecated Use {@link #readJson(Reader, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T readJson(Reader reader, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return readJson(reader, propertyIssueConsumer, type);
  }

  public static <T> T readJson(Reader reader, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithObjectMapper(oM -> prepForJsonMapping(oM, propertyIssueConsumer)
      .readValue(reader, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> T fromMap(Map<String, ?> map, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssues(pIC -> fromMap(map, pIC, type, subTypes));
  }

  /**
   * @deprecated Use {@link #fromMap(Map, Consumer, Class, Class...)} instead
   */
  @Deprecated(forRemoval = true)
  public static <T> T fromMap(Map<String, ?> map, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    return fromMap(map, propertyIssueConsumer, type);
  }

  public static <T> T fromMap(Map<String, ?> map, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return mapWithObjectMapper(oM -> prepForJsonMapping(oM, propertyIssueConsumer)
      .convertValue(map, oM.getTypeFactory().constructParametricType(type, subTypes)));
  }

  public static <T> String toJson(T obj) {
    return mapWithObjectMapper(oM -> prepForPojoMapping(oM).writeValueAsString(obj));
  }

  public static <T> void writeJson(Writer writer, T obj) {
    mapWithObjectMapper(oM -> { prepForPojoMapping(oM).writeValue(writer, obj); return null; });
  }

  public static <T> Map<String, Object> toMap(T obj) {
    return mapWithObjectMapper(oM -> prepForPojoMapping(oM).convertValue(obj, new TypeReference<>() {}));
  }

  public static class MappingException extends RuntimeException {
    public MappingException(Throwable cause) {
      super(cause);
    }
  }

  private static ObjectMapper prepForJsonMapping(ObjectMapper oM, Consumer<PropertyIssue> pIC) {
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

  private static <T> T mapWithObjectMapper(ThrowingFunction<ObjectMapper, T, IOException> cb) {
    try {
      return cb.apply(getPreConfiguredObjectMapper());
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  private static ObjectMapper getPreConfiguredObjectMapper() {
    ObjectMapper mapper = new ObjectMapper()
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