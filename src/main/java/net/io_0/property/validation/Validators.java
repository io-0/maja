package net.io_0.property.validation;

import static net.io_0.property.validation.Predicates.*;

public interface Validators {

  // TODO make factory with predicate&ms   --  see PropertyValidator
  // TODO property package instead of Property* ?

  PropertyValidator<Object> notNull = property ->
    unassignedOrNotEmpty.test(property) ?
      Validation.valid(property) :
      Validation.invalid(new Reason(property.getName(), "Can't be literally null"));

  PropertyValidator<Object> required = property ->
    assigned.test(property) ?
      Validation.valid(property) :
      Validation.invalid(new Reason(property.getName(), "Is required but missing"));

  static PropertyValidator<String> minLength(Integer parameter) {
    return property ->
      !property.isAssigned() || !property.isEmpty() && property.getValue().length() >= parameter ? // fixme
        Validation.valid(property) :
        Validation.invalid(new Reason(property.getName(), String.format("Must be longer than %s characters", parameter)));
  }

  static PropertyValidator<Number> minimum(Number parameter) {
    return (property) ->
      unassignedOrNotEmptyAnd(lte(parameter)).test(property) ?
        Validation.valid(property) :
        Validation.invalid(new Reason(property.getName(), String.format("Must be %s or greater", parameter)));
  }
}
