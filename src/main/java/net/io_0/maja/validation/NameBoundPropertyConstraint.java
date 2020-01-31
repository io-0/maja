package net.io_0.maja.validation;

import java.util.function.Function;

public interface NameBoundPropertyConstraint<T> extends Function<Object, PropertyConstraint<T>> {
}