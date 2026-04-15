package io.nexstudios.menuservice.demo;

import io.nexstudios.menuservice.api.MenuRegistry;
import io.nexstudios.menuservice.demo.definition.DemoMenuDefinition;
import java.util.Objects;

/**
 * Registers the built-in demo menu definitions.
 */
public final class DemoMenus {

  private DemoMenus() {
  }

  /**
   * Registers the demo menu definitions.
   *
   * @param registry the target registry
   */
  public static void register(MenuRegistry registry) {
    Objects.requireNonNull(registry, "registry").register(new DemoMenuDefinition());
  }
}

