package io.nexstudios.menuservice.common.api;

import java.time.Instant;

/**
 * A single open menu instance for one viewer (session/view).
 */
public interface MenuView {

  MenuKey key();

  ViewerRef viewer();

  Instant openedAt();

  /**
   * Requests a refresh; implementation may debounce and/or diff-apply.
   */
  void requestRefresh();

  /**
   * Closes the view.
   */
  void close(CloseReason reason);
}