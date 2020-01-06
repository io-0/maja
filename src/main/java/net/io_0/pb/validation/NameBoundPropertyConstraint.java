package net.io_0.pb.validation;

import java.util.function.Function;

public interface NameBoundPropertyConstraint<T> extends Function<Object, PropertyConstraint<T>> {
}