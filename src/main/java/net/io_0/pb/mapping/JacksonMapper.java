package net.io_0.pb.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.io_0.pb.mapping.jackson.JsonNameAnnotationIntrospector;

/**
 * Don't look at this, use the Mapper interface.
 * This is a helper for the Mapper interface, it is protected to avoid framework leakage.
 */
class JacksonMapper {
  protected static ObjectMapper getPreConfiguredObjectMapper() {
    return new ObjectMapper()
      .registerModules(
        new JavaTimeModule()
      )
      .setAnnotationIntrospector(new JsonNameAnnotationIntrospector())
      .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
      )
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
  }
}
