package net.io_0.pb.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.io_0.pb.experiments.validation.Validator;
import net.io_0.pb.experiments.jackson.deserialization.PropertyIssueCollectorModule;
import net.io_0.pb.experiments.validation.Validation;

import java.io.IOException;
import java.util.function.Function;

import static net.io_0.pb.experiments.jackson.deserialization.PropertyIssueCollectorModule.PROPERTY_ISSUES_ATTR;

public interface Deserializer {
  static <T extends SetPropertiesAware> Function<Validator<T>, Validation<T>> deserialize(ObjectMapper objectMapper, String json, Class<T> type) {
    try {
      PropertyIssues propertyIssues = PropertyIssues.of();

      if (!objectMapper.getRegisteredModuleIds().contains(PropertyIssueCollectorModule.class.getName())) {
        objectMapper.registerModule(new PropertyIssueCollectorModule());
      }

      T t = objectMapper
        .reader()
        .forType(type)
        .withAttribute(PROPERTY_ISSUES_ATTR, propertyIssues)
        .readValue(json);

      return validator -> Validator.<T> of(propertyIssues).and(validator).validate(t);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
