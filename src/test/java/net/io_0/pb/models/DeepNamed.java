package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.io_0.pb.mapping.PropertyName;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
@ToString
public class DeepNamed {
  @PropertyName("obj") private Nested objectToPojo;
  private Set<Nested> objectArrayToObjectSet;
  private List<StringEnumNamed> stringArrayToEnumList;
}
