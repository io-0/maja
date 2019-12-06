package net.io_0.property.validation;

import net.io_0.property.Property;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.RegexValidator;
import java.math.BigDecimal;
import java.util.Collection;

import static net.io_0.property.validation.PropertyPredicate.not;

public interface PropertyPredicates {
  PropertyPredicate assigned = Property::isAssigned;

  PropertyPredicate empty = Property::isEmpty;

  PropertyPredicate assignedAndNotEmpty = assigned.and(not(empty));

  PropertyPredicate unassignedOrEmpty = not(assignedAndNotEmpty);

  PropertyPredicate unassignedOrNotEmpty = not(assigned).or(not(empty));

  static PropertyPredicate unassignedOrNotEmptyAnd(PropertyPredicate predicate) {
    return not(assigned).or(not(empty).and(predicate));
  }

  static PropertyPredicate<Number> lte(Number number) {
    return property -> compare(number, property.getValue()) <= 0;
  }

  static PropertyPredicate<Number> lt(Number number) {
    return property -> compare(number, property.getValue()) < 0;
  }

  static PropertyPredicate<Number> gte(Number number) {
    return property -> compare(number, property.getValue()) >= 0;
  }

  static PropertyPredicate<Number> gt(Number number) {
    return property -> compare(number, property.getValue()) > 0;
  }

  static PropertyPredicate<String> lengthGte(Integer number) {
    return property -> property.getValue().length() >= number;
  }

  static PropertyPredicate<String> lengthLte(Integer number) {
    return property -> property.getValue().length() <= number;
  }

  static PropertyPredicate<Collection<?>> sizeGt(Integer number) {
    return property -> property.getValue().size() > number;
  }

  static PropertyPredicate<Collection<?>> sizeLt(Integer number) {
    return property -> property.getValue().size() < number;
  }

  static PropertyPredicate<String> regexMatch(String pattern) {
    return property -> new RegexValidator(pattern).isValid(property.getValue());
  }

  PropertyPredicate<String> email = property -> EmailValidator.getInstance().isValid(property.getValue());

  PropertyPredicate<String> hostname = property -> DomainValidator.getInstance().isValid(property.getValue());

  PropertyPredicate<String> inet4Address = property -> InetAddressValidator.getInstance().isValidInet4Address(property.getValue());

  PropertyPredicate<String> inet6Address = property -> InetAddressValidator.getInstance().isValidInet6Address(property.getValue());

  static PropertyPredicate<Number> multipleOf(Number number) {
    return property -> new BigDecimal(number.toString()).remainder(new BigDecimal(property.getValue().toString())).abs().doubleValue() < 0.000000000001;
  }

  static Integer compare(Number a, Number b) {
    return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
  }
}
