package io.nexstudios.menuservice.core;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuDefinition;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMenuRegistryTest {

  @Test
  void registersAndResolvesDefinitions() {
    DefaultMenuRegistry registry = new DefaultMenuRegistry();
    MenuDefinition definition = new TestDefinition();

    registry.register(definition);

    assertTrue(registry.find(MenuKey.of("test-menu")).isPresent());
    assertEquals(MenuKey.of("test-menu"), registry.find(MenuKey.of("test-menu")).orElseThrow().key());
  }

  @Test
  void rejectsDuplicateKeys() {
    DefaultMenuRegistry registry = new DefaultMenuRegistry();
    MenuDefinition definition = new TestDefinition();

    registry.register(definition);

    assertThrows(IllegalStateException.class, () -> registry.register(definition));
  }

  private static final class TestDefinition implements MenuDefinition {
    @Override
    public MenuKey key() {
      return MenuKey.of("test-menu");
    }

    @Override
    public MenuView create(MenuContext context) {
      return null;
    }
  }
}

