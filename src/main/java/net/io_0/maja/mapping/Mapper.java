package net.io_0.maja.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pivovarit.function.ThrowingFunction;
import lombok.Builder;
import lombok.NoArgsConstructor;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.mapping.jackson.FirstCharCaseIgnoredPropertyNamingStrategy;
import net.io_0.maja.mapping.jackson.PropertyBundleBeanSerializerModifier;
import net.io_0.maja.mapping.jackson.PropertyIssueCollectingDeserializationProblemHandler;
import net.io_0.maja.mapping.jackson.WithUnconventionalNameAnnotationIntrospector;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Mapper {
  public static <T> T fromJson(String json, Class<T> type, Class<?>... subTypes) {
    return fromJson(json, Context.of(), type, subTypes);
  }

  public static <T> T fromJson(String json, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return fromJson(json, Context.ofPropertyIssueConsumer(propertyIssueConsumer), type, subTypes);
  }

  public static <T> T fromJson(String json, Context context, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssuesAndNoIssueConsumer(context, ctx ->
      mapWithJsonObjectMapper(oM -> prepForJsonOrYamlMapping(oM, ctx)
        .readValue(json, oM.getTypeFactory().constructParametricType(type, subTypes))
      )
    );
  }

  public static <T> T readJson(Reader reader, Class<T> type, Class<?>... subTypes) {
    return readJson(reader, Context.of(), type, subTypes);
  }

  public static <T> T readJson(Reader reader, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return readJson(reader, Context.ofPropertyIssueConsumer(propertyIssueConsumer), type, subTypes);
  }

  public static <T> T readJson(Reader reader, Context context, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssuesAndNoIssueConsumer(context, ctx ->
      mapWithJsonObjectMapper(oM -> prepForJsonOrYamlMapping(oM, ctx)
        .readValue(reader, oM.getTypeFactory().constructParametricType(type, subTypes))
      )
    );
  }

  public static <T> T fromYaml(String yaml, Class<T> type, Class<?>... subTypes) {
    return fromYaml(yaml, Context.of(), type, subTypes);
  }

  public static <T> T fromYaml(String yaml, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return fromYaml(yaml, Context.ofPropertyIssueConsumer(propertyIssueConsumer), type, subTypes);
  }

  public static <T> T fromYaml(String yaml, Context context, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssuesAndNoIssueConsumer(context, ctx ->
      mapWithYamlObjectMapper(oM -> prepForJsonOrYamlMapping(oM, ctx)
        .readValue(yaml, oM.getTypeFactory().constructParametricType(type, subTypes))
      )
    );
  }

  public static <T> T readYaml(Reader reader, Class<T> type, Class<?>... subTypes) {
    return readYaml(reader, Context.of(), type, subTypes);
  }

  public static <T> T readYaml(Reader reader, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return readYaml(reader, Context.ofPropertyIssueConsumer(propertyIssueConsumer), type, subTypes);
  }

  public static <T> T readYaml(Reader reader, Context context, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssuesAndNoIssueConsumer(context, ctx ->
      mapWithYamlObjectMapper(oM -> prepForJsonOrYamlMapping(oM, ctx)
        .readValue(reader, oM.getTypeFactory().constructParametricType(type, subTypes))
      )
    );
  }

  public static <T> T fromMap(Map<String, ?> map, Class<T> type, Class<?>... subTypes) {
    return fromMap(map, Context.of(), type, subTypes);
  }

  public static <T> T fromMap(Map<String, ?> map, Consumer<PropertyIssue> propertyIssueConsumer, Class<T> type, Class<?>... subTypes) {
    return fromMap(map, Context.ofPropertyIssueConsumer(propertyIssueConsumer), type, subTypes);
  }

  public static <T> T fromMap(Map<String, ?> map, Context context, Class<T> type, Class<?>... subTypes) {
    return throwMappingExceptionIfIssuesAndNoIssueConsumer(context, ctx ->
      mapWithYamlObjectMapper(oM -> prepForJsonOrYamlMapping(oM, ctx)
        .convertValue(map, oM.getTypeFactory().constructParametricType(type, subTypes))
      )
    );
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

  @Builder(toBuilder = true)
  public static class Context {
    private final Consumer<PropertyIssue> propertyIssueConsumer;
    private final List<Instantiator> instantiators;

    public static Context of() {
      return builder().build();
    }

    public static Context ofPropertyIssueConsumer(Consumer<PropertyIssue> pIC) {
      return builder().propertyIssueConsumer(pIC).build();
    }

    public static Context ofInstantiators(Instantiator... instantiators) {
      return builder().instantiators(Arrays.asList(instantiators)).build();
    }

    public Context withPropertyIssueConsumer(Consumer<PropertyIssue> pIC) {
      return toBuilder().propertyIssueConsumer(pIC).build();
    }
  }

  public static class Instantiator {
    private final Class<?> target;
    private final Function<Map<String, Object>, ?> constructor;

    private <T> Instantiator(Class<T> target, Function<Map<String, Object>, ? extends T> constructor) {
      this.target = target;
      this.constructor = constructor;
    }

    public static <T> Instantiator of(Class<T> target, Function<Map<String, Object>, ? extends T> constructor) {
      return new Instantiator(target, constructor);
    }

    @SuppressWarnings("unchecked")
    private static SimpleModule toModule(List<Instantiator> instantiators) {
      SimpleModule sm = new SimpleModule();
      instantiators.forEach(i -> sm.addDeserializer(i.target, new JsonDeserializer() {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
          return i.constructor.apply(p.readValueAs(new TypeReference<Map<String, Object>>() {}));
        }
      }));
      return sm;
    }
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
  
  private static ObjectMapper prepForJsonOrYamlMapping(ObjectMapper oM, Context ctx) {
    if (nonNull(ctx.instantiators)) {
      oM.registerModule(Instantiator.toModule(ctx.instantiators));
    }
    return oM
      .disable(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
      )
      .addHandler(new PropertyIssueCollectingDeserializationProblemHandler(ctx.propertyIssueConsumer));
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
    var m = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .annotationIntrospector(new WithUnconventionalNameAnnotationIntrospector())
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
      .enable(MapperFeature.USE_STD_BEAN_NAMING)                                 // circumventJacksonBeanNamingConventionProblems
      .propertyNamingStrategy(new FirstCharCaseIgnoredPropertyNamingStrategy())  // circumventJacksonBeanNamingConventionProblems
      .build();
    try {
      return cb.apply(m);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  private static <T> T mapWithYamlObjectMapper(ThrowingFunction<ObjectMapper, T, IOException> cb) {
    var m = YAMLMapper.builder()
      .addModule(new JavaTimeModule())
      .annotationIntrospector(new WithUnconventionalNameAnnotationIntrospector())
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
      .enable(MapperFeature.USE_STD_BEAN_NAMING)                                 // circumventJacksonBeanNamingConventionProblems
      .propertyNamingStrategy(new FirstCharCaseIgnoredPropertyNamingStrategy())  // circumventJacksonBeanNamingConventionProblems
      .build();
    try {
      return cb.apply(m);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  private static <T> T throwMappingExceptionIfIssuesAndNoIssueConsumer(Context context, Function<Context, T> cb) {
    return isNull(context.propertyIssueConsumer) ?
      throwMappingExceptionIfIssues(pIC -> cb.apply(context.withPropertyIssueConsumer(pIC))) :
      cb.apply(context);
  }

  private static <T> T throwMappingExceptionIfIssues(Function<Consumer<PropertyIssue>, T> cb) {
    PropertyIssues propertyIssues = PropertyIssues.of();
    T t = cb.apply(propertyIssues::add);
    if (!propertyIssues.isEmpty())
      throw new MappingException(new IllegalStateException(propertyIssues.toString()));
    return t;
  }
}