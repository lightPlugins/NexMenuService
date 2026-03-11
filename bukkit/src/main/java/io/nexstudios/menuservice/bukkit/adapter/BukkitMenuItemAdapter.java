package io.nexstudios.menuservice.bukkit.adapter;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts {@link MenuItem} to Bukkit {@link ItemStack}.
 *
 * Enchantments are resolved via Paper/Bukkit registries using namespaced keys (e.g. "minecraft:unbreaking").
 */
public final class BukkitMenuItemAdapter {

  public ItemStack toItemStack(MenuItem item) {
    Objects.requireNonNull(item, "item must not be null");

    Material material = resolveMaterial(item.materialKey());
    ItemStack stack = new ItemStack(material, item.amount());

    stack.editMeta(meta -> {
      if (item.displayName() != null) {
        meta.displayName(Component.text(item.displayName()));
      }

      if (!item.lore().isEmpty()) {
        List<Component> lore = new ArrayList<>(item.lore().size());
        for (String line : item.lore()) {
          lore.add(Component.text(line));
        }
        meta.lore(lore);
      }

      item.customModelData().ifPresent(meta::setCustomModelData);
      meta.setUnbreakable(item.unbreakable());

      applyEnchantments(meta, item.enchantments());
      // hideFlagsBitset mapping will be added later.
    });

    return stack;
  }

  private static Material resolveMaterial(String materialKey) {
    Material match = Material.matchMaterial(materialKey);
    if (match != null) return match;

    // Unknown/custom item id without a resolver installed yet.
    return Material.BARRIER;
  }

  private static void applyEnchantments(org.bukkit.inventory.meta.ItemMeta meta, Map<String, Integer> enchantments) {
    if (enchantments == null || enchantments.isEmpty()) return;

    var enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    for (var e : enchantments.entrySet()) {
      String keyStr = e.getKey();
      if (keyStr == null || keyStr.isBlank()) continue;

      NamespacedKey key = NamespacedKey.fromString(keyStr);
      if (key == null) continue;

      Enchantment enchantment = enchantmentRegistry.get(key);
      if (enchantment == null) continue;

      int level = e.getValue() == null ? 1 : e.getValue();
      if (level < 1) level = 1;

      meta.addEnchant(enchantment, level, true);
    }
  }
}