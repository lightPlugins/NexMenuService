package io.nexstudios.menuservice.common.api;

@FunctionalInterface
public interface MenuPopulator {

  /**
   * Called during rendering/population. Must be thread-safe.
   *
   * Implementations should not touch platform objects directly.
   */
  void populate(MenuPopulateContext context);
}