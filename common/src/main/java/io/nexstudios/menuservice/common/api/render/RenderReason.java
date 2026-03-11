package io.nexstudios.menuservice.common.api.render;

/**
 * Why a render was requested.
 */
public enum RenderReason {
  OPEN,
  TICK,
  MANUAL_REFRESH,
  CLICK_UPDATE,
  DEPOSIT_UPDATE,
  PAGE_CHANGED,
  UNKNOWN
}