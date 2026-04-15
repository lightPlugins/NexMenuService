package io.nexstudios.menuservice.core;

import io.nexstudios.menuservice.api.MenuView;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

/**
 * Tracks the active and previous menus for a single viewer.
 */
 public class MenuSession {

  private final UUID viewerId;
  private final Deque<MenuView> history = new ArrayDeque<>();
  private MenuView currentView;
  private boolean skipNextCloseCleanup;

  public MenuSession(UUID viewerId) {
    this.viewerId = Objects.requireNonNull(viewerId, "viewerId");
  }

  public UUID viewerId() {
    return viewerId;
  }

  public Deque<MenuView> history() {
    return history;
  }

  public MenuView currentView() {
    return currentView;
  }

  public void currentView(MenuView currentView) {
    this.currentView = currentView;
  }

  public boolean skipNextCloseCleanup() {
    return skipNextCloseCleanup;
  }

  public void skipNextCloseCleanup(boolean skipNextCloseCleanup) {
    this.skipNextCloseCleanup = skipNextCloseCleanup;
  }
}


