package io.nexstudios.menuservice.common.api.item;

import java.util.*;
import java.util.function.Consumer;

/**
 * Builder for {@link MenuItem}.
 */
public final class MenuItemBuilder {

  private final String materialKey;

  private int amount = 1;
  private OptionalInt customModelData = OptionalInt.empty();
  private String displayName;
  private final List<String> lore = new ArrayList<>();
  private final Map<String, Integer> enchantments = new LinkedHashMap<>();
  private boolean unbreakable;
  private int hideFlagsBitset;

  public MenuItemBuilder(String materialKey) {
    Objects.requireNonNull(materialKey, "materialKey must not be null");
    MenuItem.requireNamespacedKey(materialKey, "materialKey");
    this.materialKey = materialKey;
  }

  public MenuItemBuilder amount(int amount) {
    if (amount < 1) throw new IllegalArgumentException("amount must be >= 1");
    if (amount > 64) throw new IllegalArgumentException("amount must be <= 64");
    this.amount = amount;
    return this;
  }

  public MenuItemBuilder customModelData(Integer customModelData) {
    if (customModelData == null) {
      this.customModelData = OptionalInt.empty();
      return this;
    }
    if (customModelData < 0) throw new IllegalArgumentException("customModelData must be >= 0");
    this.customModelData = OptionalInt.of(customModelData);
    return this;
  }

  public MenuItemBuilder displayName(String displayName) {
    if (displayName != null && displayName.isBlank()) {
      throw new IllegalArgumentException("displayName must not be blank when provided");
    }
    this.displayName = displayName;
    return this;
  }

  public MenuItemBuilder lore(List<String> lore) {
    Objects.requireNonNull(lore, "lore must not be null");
    this.lore.clear();
    this.lore.addAll(lore);
    return this;
  }

  public MenuItemBuilder addLoreLine(String line) {
    Objects.requireNonNull(line, "line must not be null");
    this.lore.add(line);
    return this;
  }

  public MenuItemBuilder enchant(String enchantmentKey, int level) {
    Objects.requireNonNull(enchantmentKey, "enchantmentKey must not be null");
    MenuItem.requireNamespacedKey(enchantmentKey, "enchantmentKey");
    if (level < 1) throw new IllegalArgumentException("level must be >= 1");
    this.enchantments.put(enchantmentKey, level);
    return this;
  }

  public MenuItemBuilder unbreakable(boolean unbreakable) {
    this.unbreakable = unbreakable;
    return this;
  }

  /**
   * Bitset for hide flags. Bukkit adapter will map this to ItemFlags.
   */
  public MenuItemBuilder hideFlagsBitset(int hideFlagsBitset) {
    if (hideFlagsBitset < 0) throw new IllegalArgumentException("hideFlagsBitset must be >= 0");
    this.hideFlagsBitset = hideFlagsBitset;
    return this;
  }

  /**
   * Convenience method to group meta changes.
   */
  public MenuItemBuilder editMeta(Consumer<MenuItemMetaEditor> editor) {
    Objects.requireNonNull(editor, "editor must not be null");
    editor.accept(new MenuItemMetaEditor(this));
    return this;
  }

  public MenuItem build() {
    return new MenuItem(
        materialKey,
        amount,
        customModelData,
        displayName,
        List.copyOf(lore),
        Map.copyOf(enchantments),
        unbreakable,
        hideFlagsBitset
    );
  }

  /**
   * Fluent meta editor facade.
   */
  public static final class MenuItemMetaEditor {
    private final MenuItemBuilder builder;

    private MenuItemMetaEditor(MenuItemBuilder builder) {
      this.builder = builder;
    }

    public MenuItemMetaEditor displayName(String displayName) {
      builder.displayName(displayName);
      return this;
    }

    public MenuItemMetaEditor lore(List<String> lore) {
      builder.lore(lore);
      return this;
    }

    public MenuItemMetaEditor addLoreLine(String line) {
      builder.addLoreLine(line);
      return this;
    }

    public MenuItemMetaEditor customModelData(Integer customModelData) {
      builder.customModelData(customModelData);
      return this;
    }

    public MenuItemMetaEditor enchant(String enchantmentKey, int level) {
      builder.enchant(enchantmentKey, level);
      return this;
    }

    public MenuItemMetaEditor unbreakable(boolean unbreakable) {
      builder.unbreakable(unbreakable);
      return this;
    }

    public MenuItemMetaEditor hideFlagsBitset(int hideFlagsBitset) {
      builder.hideFlagsBitset(hideFlagsBitset);
      return this;
    }
  }
}