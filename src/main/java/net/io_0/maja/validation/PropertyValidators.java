package net.io_0.maja.validation;

import net.io_0.maja.PojoProperty;
import net.io_0.maja.Property;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssue.Issue;
import net.io_0.maja.PropertyIssues;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static net.io_0.maja.validation.PropertyValidator.andAll;
import static net.io_0.maja.validation.Validation.invalid;

public interface PropertyValidators {
  static PropertyValidator<String> pattern(String parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(parameter)),
      Issue.of(format("Pattern Violation, '%s'", parameter), format("Must match '%s' pattern", parameter))
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
      Issue.of(format("Max Length Violation, %s", parameter), format("Must be shorter than %s characters", parameter))
    );
  }

  static PropertyValidator<String> minLength(Integer parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lengthGte(parameter)),
      Issue.of(format("Min Length Violation, %s", parameter), format("Must be longer than %s characters", parameter))
    );
  }

  static PropertyValidator<Number> exclusiveMaximum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gt(parameter)),
      Issue.of(format("Exclusive Maximum Violation, %s", parameter), format("Must be lessen than %s", parameter))
    );
  }

  static PropertyValidator<Number> exclusiveMinimum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lt(parameter)),
      Issue.of(format("Exclusive Minimum Violation, %s", parameter), format("Must be greater than %s", parameter))
    );
  }

  static PropertyValidator<Number> maximum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gte(parameter)),
      Issue.of(format("Maximum Violation, %s", parameter), format("Must be %s or lesser", parameter))
    );
  }

  static PropertyValidator<Number> minimum(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lte(parameter)),
      Issue.of(format("Minimum Violation, %s", parameter), format("Must be %s or greater", parameter))
    );
  }

  static PropertyValidator<Number> multipleOf(Number parameter) {
    return PropertyValidator.of(
      PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.multipleOf(parameter)),
      Issue.of(format("Multiple Of Violation, %s", parameter), format("Must be a multiple of %s", parameter))
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
      Issue.of(format("Max Items Violation, %s", parameter), format("Must contain %s items or less", parameter))
    );
  }

  static PropertyValidator<?> minItems(Integer parameter) {
    return mapOrCollection(
      PropertyPredicates.sizeGte(parameter),
      Issue.of(format("Min Items Violation, %s", parameter), format("Must contain %s items or more", parameter))
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
  static <T> PropertyValidator<?> each(PropertyValidator<? extends T>... validators) {
    return property -> {
      if (PropertyPredicates.unassignedOrEmpty.test((Property) property)) {
        return Validation.valid(property);
      }

      Stream<PojoProperty<T>> propertyStream;
      if (property.getValue() instanceof Map) {
        Map<String, T> values = ((Map<String, T>) property.getValue());

        propertyStream = values.entrySet().stream()
          .map(entry -> new PojoProperty<>(format("%s.%s", property.getName(), entry.getKey()), entry.getValue()));
      } else {
        List<T> values = new ArrayList<>((Collection<T>) property.getValue());

        propertyStream = IntStream.range(0, values.size())
          .mapToObj(i -> new PojoProperty<>(format("%s.%d", property.getName(), i), values.get(i)));
      }

      PropertyValidator<T> validator = andAll(validators);

      return propertyStream
        .map(validator::validate)
        .filter(Validation::isInvalid)
        .reduce(Validation::and)
        .map(validation -> Validation.of(property, validation.getPropertyIssues()))
        .orElse(Validation.valid(property));
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
        .map(propertyIssue -> PropertyIssue.of(format("%s.%s", property.getName(), propertyIssue.getPropertyName()), propertyIssue.getIssue()))
        .toArray(PropertyIssue[]::new))
      );
    };
  }

  static <T> Validator<T> lazy(Supplier<Validator<T>> validatorSupplier) {
    return t -> validatorSupplier.get().validate(t);
  }
}
