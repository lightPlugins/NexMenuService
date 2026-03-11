package io.nexstudios.menuservice.common.api;

/**
 * Context passed to menu populators.
 */
public interface MenuPopulateContext {

  MenuKey key();

  ViewerRef viewer();

  /**
   * Returns a mutable slot API for the top inventory.
   */
  MenuSlot slot(int slot);

  int size();
}