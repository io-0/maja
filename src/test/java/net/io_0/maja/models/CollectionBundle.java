package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.PropertyBundle;
import java.util.*;

@NoArgsConstructor
@Getter @ToString
public class CollectionBundle extends PropertyBundle {
  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";

  private List<Integer> one = new ArrayList<>();
  private Set<Integer> two = new HashSet<>();
  private Map<Integer, Integer> three = new HashMap<>();

  public CollectionBundle setOne(List<Integer> one) {
    this.one = one;
    markPropertySet(ONE);
    return this;
  }

  public CollectionBundle setTwo(Set<Integer> two) {
    this.two = two;
    markPropertySet(TWO);
    return this;
  }

  public CollectionBundle setThree(Map<Integer, Integer> three) {
    this.three = three;
    markPropertySet(THREE);
    return this;
  }
}
