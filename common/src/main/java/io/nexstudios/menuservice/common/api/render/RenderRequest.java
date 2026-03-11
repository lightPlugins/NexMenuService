package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.time.Instant;
import java.util.Objects;

/**
 * A request to render a menu view.
 */
public record RenderRequest(
    MenuKey menuKey,
    ViewerRef viewer,
    RenderReason reason,
    Instant requestedAt
) {
  public RenderRequest {
    Objects.requireNonNull(menuKey, "menuKey must not be null");
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(reason, "reason must not be null");
    Objects.requireNonNull(requestedAt, "requestedAt must not be null");
  }

  public static RenderRequest of(MenuKey menuKey, ViewerRef viewer, RenderReason reason) {
    return new RenderRequest(menuKey, viewer, reason, Instant.now());
  }
}