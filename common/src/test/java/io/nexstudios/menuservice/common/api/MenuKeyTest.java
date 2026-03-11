package io.nexstudios.menuservice.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuKeyTest {

  @Test
  void shouldCreateValidKey() {
    MenuKey key = MenuKey.of("nex", "test");
    assertEquals("nex", key.namespace());
    assertEquals("test", key.value());
    assertEquals("nex:test", key.asString());
  }

  @Test
  void shouldRejectBlankParts() {
    assertThrows(IllegalArgumentException.class, () -> MenuKey.of(" ", "x"));
    assertThrows(IllegalArgumentException.class, () -> MenuKey.of("nex", " "));
  }

  @Test
  void shouldRejectColonInParts() {
    assertThrows(IllegalArgumentException.class, () -> MenuKey.of("nex:bad", "x"));
    assertThrows(IllegalArgumentException.class, () -> MenuKey.of("nex", "bad:x"));
  }

  @Test
  void shouldRejectNullParts() {
    assertThrows(NullPointerException.class, () -> new MenuKey(null, "x"));
    assertThrows(NullPointerException.class, () -> new MenuKey("nex", null));
  }
}