package io.nexstudios.menuservice.api;

/**
 * Factory contract for creating menu views on demand.
 */
public interface MenuDefinition {

  /**
   * Returns the stable key of the menu definition.
   *
   * @return the menu key
   */
  MenuKey key();

  /**
   * Creates a fresh view instance for the given context.
   *
   * @param context the active menu context
   * @return a new menu view
   */
  MenuView create(MenuContext context);
}

