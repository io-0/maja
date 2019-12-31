package net.io_0.pb.experiments.validation;

import net.io_0.pb.experiments.SetPropertiesAware;
import java.util.function.Function;

public interface NameBoundPropertyConstraint<T> extends Function<SetPropertiesAware, PropertyConstraint<T>> {
}