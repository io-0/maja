package net.io_0.maja.validation;

import net.io_0.maja.Property;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.RegexValidator;
import java.math.BigDecimal;
import java.util.Collection;

import static net.io_0.maja.validation.PropertyPredicate.not;

public interface PropertyPredicates {
  PropertyPredicate<?> assigned = Property::isAssigned;

  PropertyPredicate<?> assignedAndNotNull = assigned.and(not(Property::isNull));

  /**
   * @deprecated use assignedAndNotNull
   */
  @Deprecated(forRemoval = true)
  PropertyPredicate<?> assignedAndNotEmpty = assignedAndNotNull;

  PropertyPredicate<?> unassignedOrNull = not(assignedAndNotNull);

  /**
   * @deprecated use unassignedOrNull
   */
  @Deprecated(forRemoval = true)
  PropertyPredicate<?> unassignedOrEmpty = unassignedOrNull;

  PropertyPredicate<?> unassignedOrNotNull = not(assigned).or(not(Property::isNull));

  /**
   * @deprecated use unassignedOrNotNull
   */
  @Deprecated(forRemoval = true)
  PropertyPredicate<?> unassignedOrNotEmpty = unassignedOrNotNull;

  static <T> PropertyPredicate<T> unassignedOrNullOr(PropertyPredicate<T> predicate) {
    return not(Property<T>::isAssigned).or(Property<T>::isNull).or(predicate);
  }

  /**
   * @deprecated use unassignedOrNullOr
   */
  @Deprecated(forRemoval = true)
  static <T> PropertyPredicate<T> unassignedOrEmptyOr(PropertyPredicate<T> predicate) {
    return unassignedOrNullOr(predicate);
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

  static PropertyPredicate<Collection<?>> sizeGte(Integer number) {
    return property -> property.getValue().size() >= number;
  }

  static PropertyPredicate<Collection<?>> sizeLte(Integer number) {
    return property -> property.getValue().size() <= number;
  }

  static PropertyPredicate<String> regexMatch(String pattern) {
    return property -> new RegexValidator(pattern).isValid(property.getValue());
  }

  PropertyPredicate<String> email = property -> EmailValidator.getInstance().isValid(property.getValue());

  PropertyPredicate<String> hostname = property -> DomainValidator.getInstance().isValid(property.getValue());

  PropertyPredicate<String> inet4Address = property -> InetAddressValidator.getInstance().isValidInet4Address(property.getValue());

  PropertyPredicate<String> inet6Address = property -> InetAddressValidator.getInstance().isValidInet6Address(property.getValue());

  static PropertyPredicate<Number> multipleOf(Number number) {
    return property -> new BigDecimal(property.getValue().toString()).remainder(new BigDecimal(number.toString())).abs().floatValue() < 0.0000001;
  }

  static Integer compare(Number a, Number b) {
    return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
  }
}
