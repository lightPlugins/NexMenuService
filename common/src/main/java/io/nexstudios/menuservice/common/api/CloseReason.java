package io.nexstudios.menuservice.common.api;

/**
 * Why a menu view was closed.
 */
public enum CloseReason {
  PLAYER_CLOSED,
  OPENED_OTHER_MENU,
  PLUGIN_DISABLED,
  PLAYER_DISCONNECTED,
  SERVER_SHUTDOWN,
  UNKNOWN
}