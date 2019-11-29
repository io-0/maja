package net.io_0.property.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Stream;

public interface Validation {
  static <T> Valid<T> valid(T value) {
    return new Valid<>(value);
  }

  static Invalid invalid(Reason... reason) {
    Objects.requireNonNull(reason);
    return new Invalid(Arrays.asList(reason));
  }

  boolean isValid();
  default boolean isInvalid() {
    return !isValid();
  }
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
    public Validation and(Validation other) {
      if (other.isValid()) {
        if (!this.getValue().getClass().equals(((Valid) other).getValue().getClass())) {
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
    private final List<Reason> reasons;

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public Validation and(Validation other) {
      return other.isValid() ? this : Validation.invalid(
        Stream.concat(this.reasons.stream(), ((Invalid)other).getReasons().stream())
          .toArray(Reason[]::new)
      );
    }
  }
}
