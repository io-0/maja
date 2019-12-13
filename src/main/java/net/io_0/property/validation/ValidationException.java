package net.io_0.property.validation;

import lombok.Getter;
import net.io_0.property.validation.Validation.Invalid;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("rawtypes")
public class ValidationException extends RuntimeException {
  private final Invalid validation;

  public ValidationException(Invalid validation) {
    super(toFullMessage(validation.getPropertyIssues().stream()
      .map(propertyIssue -> String.format("%s -> %s", propertyIssue.getPropertyName(), propertyIssue.getIssue()))
      .collect(Collectors.joining(", "))
    ));

    this.validation = validation;
  }

  private static String toFullMessage(String issues) {
    return issues.isBlank() ? "Validation failed." : "Validation failed. Issues: " + issues;
  }
}
