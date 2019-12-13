package net.io_0.property.validation;

import net.io_0.property.Property;
import net.io_0.property.PropertyIssue;
import net.io_0.property.PropertyIssues;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.io_0.property.validation.PropertyPredicates.*;
import static net.io_0.property.validation.PropertyValidator.of;
import static net.io_0.property.validation.Validation.invalid;

public interface PropertyValidators {
  static PropertyValidator<String> pattern(String parameter) {
    return of(unassignedOrNotEmptyAnd(regexMatch(parameter)), String.format("Must match '%s' pattern", parameter));
  }

  String PASSWORD_PATTERN = "^(?=.*?\\p{Lu})(?=.*?\\p{Ll})(?=.*?\\d)" + "(?=.*?[`~!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?]).*$";
  PropertyValidator<String> passwordFormat = of(
    unassignedOrNotEmptyAnd(regexMatch(PASSWORD_PATTERN)),
    "Must at least contain 1 number, 1 lower case letter, 1 upper case letter and 1 special character"
  );

  String BINARY_PATTERN = "^([A-Fa-f0-9]{2})+$";
  PropertyValidator<String> binaryFormat = of(unassignedOrNotEmptyAnd(regexMatch(BINARY_PATTERN)), "Must be a sequence of octets");

  String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
  PropertyValidator<String> byteFormat = of(unassignedOrNotEmptyAnd(regexMatch(BASE64_PATTERN)), "Must be base64 format");

  PropertyValidator<String> emailFormat = of(unassignedOrNotEmptyAnd(email), "Must fit email format");

  PropertyValidator<String> hostnameFormat = of(unassignedOrNotEmptyAnd(hostname), "Must fit hostname format");

  PropertyValidator<String> ipV4Format = of(unassignedOrNotEmptyAnd(inet4Address), "Must fit IP v4 format");

  PropertyValidator<String> ipV6Format = of(unassignedOrNotEmptyAnd(inet6Address), "Must fit IP v6 format");

  static PropertyValidator<String> maxLength(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(lengthLte(parameter)), String.format("Must be shorter than %s characters", parameter));
  }

  static PropertyValidator<String> minLength(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(lengthGte(parameter)), String.format("Must be longer than %s characters", parameter));
  }

  static PropertyValidator<? extends Number> exclusiveMaximum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(gt(parameter)), String.format("Must be lessen than %s", parameter));
  }

  static PropertyValidator<? extends Number> exclusiveMinimum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(lt(parameter)), String.format("Must be greater than %s", parameter));
  }

  static PropertyValidator<? extends Number> maximum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(gte(parameter)), String.format("Must be %s or lesser", parameter));
  }

  static PropertyValidator<? extends Number> minimum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(lte(parameter)), String.format("Must be %s or greater", parameter));
  }

  static PropertyValidator<? extends Number> multipleOf(Number parameter) {
    return of(unassignedOrNotEmptyAnd(PropertyPredicates.multipleOf(parameter)), String.format("Must be a multiple of %s", parameter));
  }

  PropertyValidator notNull = of(unassignedOrNotEmpty, "Can't be literally null");

  PropertyValidator required = of(assigned, "Is required but missing");

  static PropertyValidator<Collection> maxItems(Integer parameter) {
    return mapOrCollection(sizeLte(parameter), String.format("Must contain %s items or less", parameter));
  }

  static PropertyValidator<Collection> minItems(Integer parameter) {
    return mapOrCollection(sizeGte(parameter), String.format("Must contain %s items or more", parameter));
  }

  static PropertyValidator<Collection> mapOrCollection(PropertyPredicate predicate, String issue) {
    return property -> {
      if (unassignedOrEmpty.test(property)) {
        return Validation.valid(property);
      }

      Property<Collection> propertyToTest = (property.getValue() instanceof Map) ?
        new Property<>(null, ((Map) property.getValue()).entrySet(), true) :
        property;

      return predicate.test(propertyToTest) ?
        Validation.valid(property) :
        invalid(PropertyIssue.of(property.getName(), issue));
    };
  }

  static <T> PropertyValidator<Collection> each(PropertyValidator<T> validator) {
    return property -> {
      if (unassignedOrEmpty.test(property)) {
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
        List<T> values = new ArrayList<>(property.getValue());

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

  static <T> PropertyValidator<T> valid(Validator<T> validator) {
    return property -> {
      if (unassignedOrEmpty.test(property)) {
        return Validation.valid(property);
      }

      Validation validation = validator.validate(property.getValue());
      if (validation.isValid()) {
        return validation;
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
