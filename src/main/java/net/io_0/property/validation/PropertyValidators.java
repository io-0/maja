package net.io_0.property.validation;

import java.util.Collection;

import static net.io_0.property.validation.PropertyPredicates.*;
import static net.io_0.property.validation.PropertyValidator.of;
import static net.io_0.property.validation.Validation.invalid;
import static net.io_0.property.validation.Validation.valid;

public interface PropertyValidators {
  String BINARY_PATTERN = "^([A-Fa-f0-9]{2})+$";
  PropertyValidator<String> binaryFormat = of(unassignedOrNotEmptyAnd(regexMatch(BINARY_PATTERN)), "Must be a sequence of octets");

  String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
  PropertyValidator<String> byteFormat = of(unassignedOrNotEmptyAnd(regexMatch(BASE64_PATTERN)), "Must be base64 format");

  PropertyValidator<String> emailFormat = of(unassignedOrNotEmptyAnd(email), "Must fit email format");

  static PropertyValidator<Number> exclusiveMaximum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(gt(parameter)), String.format("Must be lessen than %s", parameter));
  }

  static PropertyValidator<Number> exclusiveMinimum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(lt(parameter)), String.format("Must be greater than %s", parameter));
  }

  PropertyValidator<String> hostnameFormat = of(unassignedOrNotEmptyAnd(hostname), "Must fit hostname format");

  PropertyValidator<String> ipV4Format = of(unassignedOrNotEmptyAnd(inet4Address), "Must fit IP v4 format");

  PropertyValidator<String> ipV6Format = of(unassignedOrNotEmptyAnd(inet6Address), "Must fit IP v6 format");

  static PropertyValidator<Number> maximum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(gte(parameter)), String.format("Must be %s or lesser", parameter));
  }

  static PropertyValidator<Collection> maxItems(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(sizeLt(parameter)), String.format("Must contain %s items or less", parameter));
  }

  static PropertyValidator<String> maxLength(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(lengthLte(parameter)), String.format("Must be shorter than %s characters", parameter));
  }

  static PropertyValidator<Number> minimum(Number parameter) {
    return of(unassignedOrNotEmptyAnd(lte(parameter)), String.format("Must be %s or greater", parameter));
  }

  static PropertyValidator<Collection> minItems(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(sizeGt(parameter)), String.format("Must contain %s items or more", parameter));
  }

  static PropertyValidator<String> minLength(Integer parameter) {
    return of(unassignedOrNotEmptyAnd(lengthGte(parameter)), String.format("Must be longer than %s characters", parameter));
  }

  static PropertyValidator<Number> multipleOf(Number parameter) {
    return of(unassignedOrNotEmptyAnd(PropertyPredicates.multipleOf(parameter)), String.format("Must be a multiple of %s", parameter));
  }

  PropertyValidator notNull = of(unassignedOrNotEmpty, "Can't be literally null");

  String PASSWORD_PATTERN = "^(?=.*?\\p{Lu})(?=.*?\\p{Ll})(?=.*?\\d)" + "(?=.*?[`~!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?]).*$";
  PropertyValidator<String> passwordFormat = of(
    unassignedOrNotEmptyAnd(regexMatch(PASSWORD_PATTERN)),
    "Must at least contain 1 number, 1 lower case letter, 1 upper case letter and 1 special character"
  );

  static PropertyValidator<String> pattern(String parameter) {
    return of(unassignedOrNotEmptyAnd(regexMatch(parameter)), String.format("Must match '%s' pattern", parameter));
  }

  PropertyValidator required = of(assigned, "Is required but missing");

  static <T> PropertyValidator<T> validator(Validator<T> validator) {
    return property -> {
      if (unassignedOrEmpty.test(property)) {
        return valid(property);
      }

      Validation validation = validator.apply(property.getValue());
      if (validation.isValid()) {
        return validation;
      }

      return invalid(((Validation.Invalid) validation).getReasons().stream()
        .map(reason -> new Reason(String.format("%s.%s", property.getName(), reason.getSubject()), reason.getArgument()))
        .toArray(Reason[]::new)
      );
    };
  }
}
