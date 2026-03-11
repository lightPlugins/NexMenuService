package io.nexstudios.menuservice.common.api.item;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

  @Test
  void shouldAcceptValidNamespacedKey() {
    var item = new MenuItem(
        "minecraft:stone",
        1,
        OptionalInt.empty(),
        null,
        List.of(),
        Map.of(),
        false,
        0
    );
    assertEquals("minecraft:stone", item.materialKey());
  }

  @Test
  void shouldRejectBlankKey() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of(" "));
  }

  @Test
  void shouldRejectKeyWithoutColon() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of("minecraft"));
  }

  @Test
  void shouldRejectKeyWithMissingNamespaceOrValue() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of(":stone"));
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of("minecraft:"));
  }

  @Test
  void shouldRejectKeyWithMultipleColons() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of("a:b:c"));
  }

  @Test
  void shouldRejectKeyWithWhitespace() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of("minecraft:stone "));
    assertThrows(IllegalArgumentException.class, () -> MenuItem.of("mine craft:stone"));
  }

  @Test
  void shouldRejectInvalidAmount() {
    assertThrows(IllegalArgumentException.class, () -> MenuItem.builder("minecraft:stone").amount(0).build());
    assertThrows(IllegalArgumentException.class, () -> MenuItem.builder("minecraft:stone").amount(65).build());
  }
}