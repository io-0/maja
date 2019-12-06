package net.io_0.property.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.io_0.property.PropertyIssue;
import net.io_0.property.PropertyIssues;
import java.util.stream.Stream;

public interface Validation {
  static <T> Valid<T> valid(T value) {
    return new Valid<>(value);
  }

  static Invalid invalid(PropertyIssues propertyIssues) {
    return new Invalid(propertyIssues);
  }

  boolean isValid();
  default boolean isInvalid() {
    return !isValid();
  }
  PropertyIssues getPropertyIssues();
  Validation and(Validation other);

  @RequiredArgsConstructor
  @Getter
  final class Valid<T> implements Validation {
    private final T value;

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public PropertyIssues getPropertyIssues() {
      return PropertyIssues.of();
    }

    @Override
    public Validation and(Validation other) {
      if (other.isValid()) {
        if (!this.getValue().getClass().equals(((Valid<?>) other).getValue().getClass())) {
          throw new IllegalArgumentException("Class mismatch");
        }
        return this;
      }
      return other;
    }
  }

  @RequiredArgsConstructor
  @Getter
  final class Invalid implements Validation {
    private final PropertyIssues propertyIssues;

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public Validation and(Validation other) {
      return other.isValid() ? this : Validation.invalid(PropertyIssues.of(
        Stream.concat(this.propertyIssues.stream(), other.getPropertyIssues().stream())
          .toArray(PropertyIssue[]::new)
      ));
    }
  }
}
