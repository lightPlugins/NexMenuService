package io.nexstudios.menuservice.common.api.item;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Immutable, platform-agnostic item model for menus.
 */
public record MenuItem(
    String materialKey,
    int amount,
    OptionalInt customModelData,
    String displayName,
    List<String> lore,
    Map<String, Integer> enchantments,
    boolean unbreakable,
    int hideFlagsBitset
) {

  public MenuItem {
    Objects.requireNonNull(materialKey, "materialKey must not be null");
    Objects.requireNonNull(customModelData, "customModelData must not be null");
    Objects.requireNonNull(lore, "lore must not be null");
    Objects.requireNonNull(enchantments, "enchantments must not be null");

    requireNamespacedKey(materialKey, "materialKey");

    if (amount < 1) throw new IllegalArgumentException("amount must be >= 1");
    if (amount > 64) throw new IllegalArgumentException("amount must be <= 64");

    if (displayName != null && displayName.isBlank()) {
      throw new IllegalArgumentException("displayName must not be blank when provided");
    }
  }

  public static MenuItemBuilder builder(String materialKey) {
    return new MenuItemBuilder(materialKey);
  }

  public static MenuItem of(String materialKey) {
    return builder(materialKey).build();
  }

  static void requireNamespacedKey(String key, String fieldName) {
    if (key.isBlank()) throw new IllegalArgumentException(fieldName + " must not be blank");
    if (key.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException(fieldName + " must not contain whitespace");
    }

    int colon = key.indexOf(':');
    if (colon <= 0 || colon == key.length() - 1) {
      throw new IllegalArgumentException(fieldName + " must be a namespaced key in the form 'namespace:value'");
    }
    if (key.indexOf(':', colon + 1) != -1) {
      throw new IllegalArgumentException(fieldName + " must contain exactly one ':'");
    }

    String namespace = key.substring(0, colon);
    String value = key.substring(colon + 1);

    if (namespace.isBlank() || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must be a namespaced key in the form 'namespace:value'");
    }
  }
}