package net.io_0.pb.validation;

import net.io_0.pb.SetPropertiesAware;
import java.util.function.Function;

public interface NameBoundPropertyConstraint<T> extends Function<SetPropertiesAware, PropertyConstraint<T>> {
}