package io.nexstudios.menuservice.common.api.page.control;

import java.util.Objects;

public record PageControlBinding(String areaId, PageControl control) {

  public PageControlBinding {
    Objects.requireNonNull(areaId, "areaId must not be null");
    if (areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    Objects.requireNonNull(control, "control must not be null");
    control.validate();
  }

  public String controlId() {
    return control.controlId();
  }
}