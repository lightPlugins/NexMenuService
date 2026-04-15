package io.nexstudios.menuservice.api.page.control;

import java.util.List;
import java.util.Objects;

/**
 * Common contract for cycling controls used by paged menu areas.
 */
public interface PageControl {

  String controlId();

  List<String> modeIds();

  String defaultModeId();

  String labelForMode(String modeId);

  default void validate() {
    Objects.requireNonNull(controlId(), "controlId must not be null");
    if (controlId().isBlank()) {
      throw new IllegalArgumentException("controlId must not be blank.");
    }

    Objects.requireNonNull(modeIds(), "modeIds must not be null");
    if (modeIds().isEmpty()) {
      throw new IllegalArgumentException("modeIds must not be empty.");
    }

    Objects.requireNonNull(defaultModeId(), "defaultModeId must not be null");
    if (defaultModeId().isBlank()) {
      throw new IllegalArgumentException("defaultModeId must not be blank.");
    }

    if (!modeIds().contains(defaultModeId())) {
      throw new IllegalArgumentException("defaultModeId must be contained in modeIds.");
    }
  }
}

