package io.nexstudios.menuservice.demo.definition;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuDefinition;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuView;
import io.nexstudios.menuservice.demo.view.DemoMenuView;

/**
 * Demo menu definition used as a simple showcase menu.
 */
public final class DemoMenuDefinition implements MenuDefinition {

  public static final MenuKey KEY = MenuKey.of("demo-menu");

  @Override
  public MenuKey key() {
    return KEY;
  }

  @Override
  public MenuView create(MenuContext context) {
    return new DemoMenuView();
  }
}

