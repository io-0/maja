package net.io_0.maja.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssues;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Validation<T> {
  boolean isValid();
  T getValue(Function<Invalid<T>, ? extends RuntimeException> orThrow);
  PropertyIssues getPropertyIssues();
  <U extends T> Validation<U> and(Validation<U> other);

  default boolean isInvalid() {
    return !isValid();
  }

  default T getValue() {
    return getValue(Validator.ValidationException::new);
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
  final class Valid<T> implements Validation<T> {
    private final T value;

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public T getValue(Function<Invalid<T>, ? extends RuntimeException> orThrow) {
      return value;
    }

    @Override
    public PropertyIssues getPropertyIssues() {
      return PropertyIssues.of();
    }

    @Override
    public <U extends T> Validation<U> and(Validation<U> other) {
      return other;
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
    public T getValue(Function<Invalid<T>, ? extends RuntimeException> orThrow) {
      throw orThrow.apply(this);
    }

    @Override
    public <U extends T> Validation<U> and(Validation<U> other) {
      return Validation.invalid(other.isValid() ? this.propertyIssues : PropertyIssues.of(
        Stream.concat(this.propertyIssues.stream(), other.getPropertyIssues().stream())
          .toArray(PropertyIssue[]::new)
      ));
    }
  }
}
