package net.io_0.pb.experiments.validation;

import net.io_0.pb.experiments.Property;
import net.io_0.pb.experiments.PropertyIssue;
import net.io_0.pb.experiments.PropertyIssues;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.io_0.pb.experiments.validation.PropertyValidator.of;
import static net.io_0.pb.experiments.validation.Validation.invalid;

public interface PropertyValidators {
  static PropertyValidator<String> pattern(String parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(parameter)), String.format("Must match '%s' pattern", parameter));
  }

  String PASSWORD_PATTERN = "^(?=.*?\\p{Lu})(?=.*?\\p{Ll})(?=.*?\\d)" + "(?=.*?[`~!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?]).*$";
  PropertyValidator<String> passwordFormat = PropertyValidator.of(
    PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(PASSWORD_PATTERN)),
    "Must at least contain 1 number, 1 lower case letter, 1 upper case letter and 1 special character"
  );

  String BINARY_PATTERN = "^([A-Fa-f0-9]{2})+$";
  PropertyValidator<String> binaryFormat = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(BINARY_PATTERN)), "Must be a sequence of octets");

  String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
  PropertyValidator<String> byteFormat = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.regexMatch(BASE64_PATTERN)), "Must be base64 format");

  PropertyValidator<String> emailFormat = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.email), "Must fit email format");

  PropertyValidator<String> hostnameFormat = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.hostname), "Must fit hostname format");

  PropertyValidator<String> ipV4Format = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.inet4Address), "Must fit IP v4 format");

  PropertyValidator<String> ipV6Format = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.inet6Address), "Must fit IP v6 format");

  static PropertyValidator<String> maxLength(Integer parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lengthLte(parameter)), String.format("Must be shorter than %s characters", parameter));
  }

  static PropertyValidator<String> minLength(Integer parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lengthGte(parameter)), String.format("Must be longer than %s characters", parameter));
  }

  static PropertyValidator<? extends Number> exclusiveMaximum(Number parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gt(parameter)), String.format("Must be lessen than %s", parameter));
  }

  static PropertyValidator<? extends Number> exclusiveMinimum(Number parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lt(parameter)), String.format("Must be greater than %s", parameter));
  }

  static PropertyValidator<? extends Number> maximum(Number parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.gte(parameter)), String.format("Must be %s or lesser", parameter));
  }

  static PropertyValidator<? extends Number> minimum(Number parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.lte(parameter)), String.format("Must be %s or greater", parameter));
  }

  static PropertyValidator<? extends Number> multipleOf(Number parameter) {
    return PropertyValidator.of(PropertyPredicates.unassignedOrNotEmptyAnd(PropertyPredicates.multipleOf(parameter)), String.format("Must be a multiple of %s", parameter));
  }

  PropertyValidator<?> notNull = PropertyValidator.of(PropertyPredicates.unassignedOrNotEmpty, "Can't be literally null");

  PropertyValidator<?> required = PropertyValidator.of(PropertyPredicates.assigned, "Is required but missing");

  static PropertyValidator<?> maxItems(Integer parameter) {
    return mapOrCollection(PropertyPredicates.sizeLte(parameter), String.format("Must contain %s items or less", parameter));
  }

  static PropertyValidator<?> minItems(Integer parameter) {
    return mapOrCollection(PropertyPredicates.sizeGte(parameter), String.format("Must contain %s items or more", parameter));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static PropertyValidator<?> mapOrCollection(PropertyPredicate<?> predicate, String issue) {
    return property -> {
      if (PropertyPredicates.unassignedOrEmpty.test((Property) property)) {
        return Validation.valid(property);
      }

      Property<?> propertyToTest = (property.getValue() instanceof Map) ?
        new Property<>(property.getName(), ((Map) property.getValue()).entrySet(), true) :
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
          .map(entry -> new Property<>(String.format("%s.%s", property.getName(), entry.getKey()), entry.getValue(), true))
          .map(validator::validate)
          .filter(Validation::isInvalid)
          .reduce(Validation::and)
          .map(validation -> Validation.of(property, validation.getPropertyIssues()))
          .orElse(Validation.valid(property));
      } else {
        List<T> values = new ArrayList<>((Collection<T>) property.getValue());

        return IntStream.range(0, values.size())
          .mapToObj(i -> new Property<>(String.format("%s.%d", property.getName(), i), values.get(i), true))
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
