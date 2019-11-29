package net.io_0.property.validation;

import net.io_0.property.Property;

import java.math.BigDecimal;

import static net.io_0.property.validation.PropertyPredicate.not;

public interface Predicates {
  PropertyPredicate assigned = Property::isAssigned;

  PropertyPredicate empty = Property::isEmpty;

  PropertyPredicate present = assigned.and(not(empty));

  PropertyPredicate unassignedOrNotEmpty = not(assigned).or(not(empty));

  static PropertyPredicate unassignedOrNotEmptyAnd(PropertyPredicate p) {
    return not(assigned).or(not(empty).and(p));
  }

  static PropertyPredicate<Number> lte(Number a) {
    return b -> compare(a, b.getValue()) <= 0;
  }

  static PropertyPredicate<Number> lt(Number a) {
    return b -> compare(a, b.getValue()) < 0;
  }

  static PropertyPredicate<Number> gte(Number a) {
    return b -> compare(a, b.getValue()) >= 0;
  }

  static PropertyPredicate<Number> gt(Number a) {
    return b -> compare(a, b.getValue()) > 0;
  }

  static Integer compare(Number a, Number b) {
    return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
  }
}
