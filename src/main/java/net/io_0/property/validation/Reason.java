package net.io_0.property.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Reason {
  private final String subject;
  private final String argument;
}
