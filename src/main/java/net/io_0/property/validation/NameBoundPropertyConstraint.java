package net.io_0.property.validation;

import net.io_0.property.SetPropertiesAware;
import java.util.function.Function;

public interface NameBoundPropertyConstraint<T> extends Function<SetPropertiesAware, PropertyConstraint<T>> {
}