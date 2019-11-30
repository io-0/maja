package net.io_0.property.validation;

import java.util.function.Function;

public interface Validator<T> extends Function<T, Validation> {
}
