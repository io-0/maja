package net.io_0.maja.validation;

import net.io_0.maja.PojoProperty;
import net.io_0.maja.Property;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssue.Issue;
import net.io_0.maja.PropertyIssues;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.io_0.maja.validation.Validation.invalid;

public interface PropertyValidators {
  static PropertyValidator<String> pattern(String parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(parameter)),
      Issue.of(String.format("Pattern Violation, '%s'", parameter), String.format("Must match '%s' pattern", parameter))
    );
  }

  String PASSWORD_PATTERN = "^(?=.*?\\p{Lu})(?=.*?\\p{Ll})(?=.*?\\d)" + "(?=.*?[`~!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?]).*$";
  PropertyValidator<String> passwordFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(PASSWORD_PATTERN)),
    Issue.of("Password Format Violation", "Must at least contain 1 number, 1 lower case letter, 1 upper case letter and 1 special character")
  );

  String BINARY_PATTERN = "^([A-Fa-f0-9]{2})+$";
  PropertyValidator<String> binaryFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(BINARY_PATTERN)),
    Issue.of("Binary Format Violation", "Must be a sequence of octets")
  );

  String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
  PropertyValidator<String> byteFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(BASE64_PATTERN)),
    Issue.of("Byte Format Violation", "Must be base64 format")
  );

  PropertyValidator<String> emailFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.email),
    Issue.of("Email Format Violation", "Must fit email format")
  );

  PropertyValidator<String> hostnameFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.hostname),
    Issue.of("Hostname Format Violation", "Must fit hostname format")
  );

  PropertyValidator<String> ipV4Format = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.inet4Address),
    Issue.of("IP V4 Format Violation", "Must fit IP v4 format")
  );

  PropertyValidator<String> ipV6Format = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.inet6Address),
    Issue.of("IP V6 Format Violation", "Must fit IP v6 format")
  );

  static PropertyValidator<String> maxLength(Integer parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lengthLte(parameter)),
      Issue.of(String.format("Max Length Violation, %s", parameter), String.format("Must be shorter than %s characters", parameter))
    );
  }

  static PropertyValidator<String> minLength(Integer parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lengthGte(parameter)),
      Issue.of(String.format("Min Length Violation, %s", parameter), String.format("Must be longer than %s characters", parameter))
    );
  }

  static PropertyValidator<? extends Number> exclusiveMaximum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gt(parameter)),
      Issue.of(String.format("Exclusive Maximum Violation, %s", parameter), String.format("Must be lessen than %s", parameter))
    );
  }

  static PropertyValidator<? extends Number> exclusiveMinimum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lt(parameter)),
      Issue.of(String.format("Exclusive Minimum Violation, %s", parameter), String.format("Must be greater than %s", parameter))
    );
  }

  static PropertyValidator<? extends Number> maximum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gte(parameter)),
      Issue.of(String.format("Maximum Violation, %s", parameter), String.format("Must be %s or lesser", parameter))
    );
  }

  static PropertyValidator<? extends Number> minimum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lte(parameter)),
      Issue.of(String.format("Minimum Violation, %s", parameter), String.format("Must be %s or greater", parameter))
    );
  }

  static PropertyValidator<? extends Number> multipleOf(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.multipleOf(parameter)),
      Issue.of(String.format("Multiple Of Violation, %s", parameter), String.format("Must be a multiple of %s", parameter))
    );
  }

  PropertyValidator<?> notNull = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmpty,
    Issue.of("Not Null Violation", "Can't be literally null")
  );

  PropertyValidator<?> required = PropertyValidator.of(
    PropertyPredicates.assigned,
    Issue.of("Required Violation", "Is required but missing")
  );

  static PropertyValidator<?> maxItems(Integer parameter) {
    return mapOrCollection(
      PropertyPredicates.sizeLte(parameter),
      Issue.of(String.format("Max Items Violation, %s", parameter), String.format("Must contain %s items or less", parameter))
    );
  }

  static PropertyValidator<?> minItems(Integer parameter) {
    return mapOrCollection(
      PropertyPredicates.sizeGte(parameter),
      Issue.of(String.format("Min Items Violation, %s", parameter), String.format("Must contain %s items or more", parameter))
    );
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static PropertyValidator<?> mapOrCollection(PropertyPredicate<?> predicate, Issue issue) {
    return property -> {
      if (PropertyPredicates.unassignedOrEmpty.test((Property) property)) {
        return Validation.valid(property);
      }

      Property<?> propertyToTest = (property.getValue() instanceof Map) ?
        new PojoProperty<>(property.getName(), ((Map) property.getValue()).entrySet()) :
        property;

      return predicate.test((Property) propertyToTest) ?
        Validation.valid(property) :
        invalid(PropertyIssue.of(property.getName(), issue));
    };
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> PropertyValidator<?> each(PropertyValidator<T> validator) {
    return property -> {
      if (PropertyPredicates.unassignedOrEmpty.test((Property) property)) {
        return Validation.valid(property);
      }

      if (property.getValue() instanceof Map) {
        Map<String, T> values = ((Map<String, T>) property.getValue());

        return values.entrySet().stream()
          .map(entry -> new PojoProperty<>(String.format("%s.%s", property.getName(), entry.getKey()), entry.getValue()))
          .map(validator::validate)
          .filter(Validation::isInvalid)
          .reduce(Validation::and)
          .map(validation -> Validation.of(property, validation.getPropertyIssues()))
          .orElse(Validation.valid(property));
      } else {
        List<T> values = new ArrayList<>((Collection<T>) property.getValue());

        return IntStream.range(0, values.size())
          .mapToObj(i -> new PojoProperty<>(String.format("%s.%d", property.getName(), i), values.get(i)))
          .map(validator::validate)
          .filter(Validation::isInvalid)
          .reduce(Validation::and)
          .map(validation -> Validation.of(property, validation.getPropertyIssues()))
          .orElse(Validation.valid(property));
      }
    };
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> PropertyValidator<T> valid(Validator<T> validator) {
    return property -> {
      if (PropertyPredicates.unassignedOrEmpty.test((Property) property)) {
        return Validation.valid(property);
      }

      Validation<T> validation = validator.validate(property.getValue());
      if (validation.isValid()) {
        return Validation.valid(property);
      }

      return invalid(PropertyIssues.of(validation.getPropertyIssues().stream()
        .map(propertyIssue -> PropertyIssue.of(String.format("%s.%s", property.getName(), propertyIssue.getPropertyName()), propertyIssue.getIssue()))
        .toArray(PropertyIssue[]::new))
      );
    };
  }

  static <T> Validator<T> lazy(Supplier<Validator<T>> validatorSupplier) {
    return t -> validatorSupplier.get().validate(t);
  }
}
