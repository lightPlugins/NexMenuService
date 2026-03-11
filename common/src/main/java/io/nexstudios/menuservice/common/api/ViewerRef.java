package io.nexstudios.menuservice.common.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Platform-agnostic viewer reference.
 */
public interface ViewerRef {

  UUID uniqueId();

  String name();

  static ViewerRef of(UUID uniqueId, String name) {
    Objects.requireNonNull(uniqueId, "uniqueId must not be null");
    Objects.requireNonNull(name, "name must not be null");
    if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");

    return new ViewerRef() {
      @Override public UUID uniqueId() { return uniqueId; }
      @Override public String name() { return name; }
      @Override public String toString() { return "ViewerRef[" + uniqueId + "," + name + "]"; }
    };
  }
}