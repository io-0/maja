package net.io_0.pb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.io_0.pb.jackson.JsonNameAnnotationIntrospector;
import net.io_0.pb.jackson.PropertyIssueCollectorModule;
import java.io.Reader;
import java.util.function.Consumer;

import static net.io_0.pb.jackson.PropertyIssueCollectorModule.PROPERTY_ISSUE_CONSUMER_ATTR;

public interface JsonDeserializer {
  static <T> T deserialize(Reader json, Class<T> type) {
    return deserialize(json, type, dismissed -> {});
  }

  static <T> T deserialize(Reader json, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    ObjectMapper objectMapper = new ObjectMapper()
      .registerModules(
        new JavaTimeModule(),
        new PropertyIssueCollectorModule()
      )
      .setAnnotationIntrospector(new JsonNameAnnotationIntrospector())
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
      )
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    try {
      return objectMapper
        .reader()
        .forType(type)
        .withAttribute(PROPERTY_ISSUE_CONSUMER_ATTR, propertyIssueConsumer)
        .readValue(json);
    } catch (Exception e) {
      throw new DeserializationException(e);
    }
  }
}