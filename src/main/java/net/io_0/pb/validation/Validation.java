package net.io_0.pb.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.io_0.pb.PropertyIssue;
import net.io_0.pb.PropertyIssues;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Validation<T> {
  boolean isValid();
  PropertyIssues getPropertyIssues();
  Validation<T> and(Validation<T> other);
  Valid<T> proceedIfValid(Function<Invalid<T>, ? extends RuntimeException> orThrow);

  default boolean isInvalid() {
    return !isValid();
  }

  default Valid<T> proceedIfValid() {
    return proceedIfValid(Validator.ValidationException::new);
  }

  static <T> Valid<T> valid(T value) {
    return new Valid<>(value);
  }

  static <T> Invalid<T> invalid(PropertyIssue... propertyIssues) {
    return invalid(PropertyIssues.of(propertyIssues));
  }

  static <T> Invalid<T> invalid(PropertyIssues propertyIssues) {
    return new Invalid<>(propertyIssues);
  }

  static <T> Validation<T> of(T value, PropertyIssues propertyIssues) {
    return propertyIssues.isEmpty() ? valid(value) : invalid(propertyIssues);
  }

  @RequiredArgsConstructor
  @Getter
  final class Valid<T> implements Validation<T> {
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
    public Validation<T> and(Validation<T> other) {
      return other.isValid() ? this : other;
    }

    @Override
    public Valid<T> proceedIfValid(Function<Invalid<T>, ? extends RuntimeException> orThrow) {
      return this;
    }
  }

  @RequiredArgsConstructor
  @Getter
  final class Invalid<T> implements Validation<T> {
    private final PropertyIssues propertyIssues;

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public Validation<T> and(Validation<T> other) {
      return other.isValid() ? this : Validation.invalid(PropertyIssues.of(
        Stream.concat(this.propertyIssues.stream(), other.getPropertyIssues().stream())
          .toArray(PropertyIssue[]::new)
      ));
    }

    @Override
    public Valid<T> proceedIfValid(Function<Invalid<T>, ? extends RuntimeException> orThrow) {
      throw orThrow.apply(this);
    }
  }
}
