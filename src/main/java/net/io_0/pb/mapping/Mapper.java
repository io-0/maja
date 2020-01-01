package net.io_0.pb.mapping;

import com.fasterxml.jackson.databind.*;
import net.io_0.pb.PropertyIssue;
import net.io_0.pb.PropertyIssues;
import net.io_0.pb.mapping.jackson.PropertyIssueCollectingDeserializationProblemHandler;
import net.io_0.pb.mapping.jackson.SetPropertiesAwareBeanSerializerModifier;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;

import static net.io_0.pb.mapping.JacksonMapper.*;

public interface Mapper {
  static <T> T readJson(Reader reader, Class<T> type) {
    PropertyIssues propertyIssues = PropertyIssues.of();
    T t = readJson(reader, type, propertyIssues::add);
    if (!propertyIssues.isEmpty())
      throw new MappingException(new IllegalStateException(propertyIssues.toString()));
    return t;
  }

  static <T> T readJson(Reader reader, Class<T> type, Consumer<PropertyIssue> propertyIssueConsumer) {
    ObjectMapper objectMapper = getPreConfiguredObjectMapper();
    objectMapper.addHandler(new PropertyIssueCollectingDeserializationProblemHandler(propertyIssueConsumer));

    try {
      return objectMapper
        .reader()
        .forType(type)
        .readValue(reader);
    } catch (Exception e) {
      throw new MappingException(e);
    }
  }

  static <T> void writeJson(Writer writer, T obj) {
    ObjectMapper objectMapper = getPreConfiguredObjectMapper();
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier(
      new SetPropertiesAwareBeanSerializerModifier()
    ));

    try {
      objectMapper.writeValue(writer, obj);
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