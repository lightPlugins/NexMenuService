package io.nexstudios.menuservice.bukkit.adapter;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
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
      applyHideFlags(meta, item.hideFlagsBitset());
    });

    return stack;
  }

  private static Material resolveMaterial(String materialKey) {
    if (materialKey == null || materialKey.isBlank()) return Material.BARRIER;

    // 1) Direct match (Paper/Bukkit may accept different formats depending on version)
    Material match = Material.matchMaterial(materialKey);
    if (match != null) return match;

    // 2) If namespaced, try the "value" part (e.g. "minecraft:stone" -> "stone")
    int colon = materialKey.indexOf(':');
    if (colon > 0 && colon < materialKey.length() - 1) {
      String value = materialKey.substring(colon + 1);
      match = Material.matchMaterial(value);
      if (match != null) return match;

      match = Material.matchMaterial(value.toUpperCase(java.util.Locale.ROOT));
      if (match != null) return match;
    }

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

  private static void applyHideFlags(org.bukkit.inventory.meta.ItemMeta meta, int hideFlagsBitset) {
    if (meta == null) return;

    // Clear known flags first, then apply according to bitset.
    meta.removeItemFlags(ItemFlag.values());

    if (hideFlagsBitset <= 0) return;

    for (ItemFlag flag : ItemFlag.values()) {
      int ord = flag.ordinal();

      int bit = 1 << ord;
      if ((hideFlagsBitset & bit) != 0) {
        meta.addItemFlags(flag);
      }
    }
  }
}