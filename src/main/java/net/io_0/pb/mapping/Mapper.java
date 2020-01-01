package net.io_0.pb.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.io_0.pb.PropertyIssue;
import net.io_0.pb.PropertyIssues;
import net.io_0.pb.mapping.jackson.JsonNameAnnotationIntrospector;
import net.io_0.pb.mapping.jackson.PropertyIssueCollectingDeserializationProblemHandler;

import java.io.Reader;
import java.util.function.Consumer;

public interface Mapper {
  static <T> T readJson(Reader json, Class<T> type) {
    PropertyIssues propertyIssues = PropertyIssues.of();
    T t = readJson(json, type, propertyIssues::add);
    if (!propertyIssues.isEmpty())
      throw new MappingException(new IllegalStateException(propertyIssues.toString()));
    return t;
  }

  static <T> T readJson(Reader json, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    ObjectMapper objectMapper = new ObjectMapper()
      .registerModules(
        new JavaTimeModule()
      )
      .setAnnotationIntrospector(new JsonNameAnnotationIntrospector())
      .addHandler(new PropertyIssueCollectingDeserializationProblemHandler(propertyIssueConsumer))
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
        .readValue(json);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  class MappingException extends RuntimeException {
    public MappingException(Throwable cause) {
      super(cause);
    }
  }
}