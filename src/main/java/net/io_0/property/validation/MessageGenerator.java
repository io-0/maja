package net.io_0.property.validation;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class MessageGenerator<T> implements Function<T, Reason> {
  private static final String defaultMessage = "%s is invalid";
  private final String fieldName;

  @Override
  public Reason apply(T t) {
    return new Reason(fieldName, String.format(defaultMessage, t));
  }
}
