package io.nexstudios.menuservice.bukkit.adapter;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Converts Bukkit {@link ItemStack} into a platform-agnostic {@link MenuItem} snapshot.
 *
 * This is used for interaction hooks (e.g. bottom inventory click reporting).
 */
public final class BukkitItemSnapshotAdapter {

  public Optional<MenuItem> toMenuItemSnapshot(ItemStack stack) {
    if (stack == null) return Optional.empty();
    if (stack.getType().isAir()) return Optional.empty();

    String materialKey = stack.getType().getKey().toString(); // e.g. "minecraft:stone"
    int amount = Math.max(1, stack.getAmount());

    var customModelData = OptionalInt.empty();
    String displayName = null;
    List<String> lore = List.of();
    Map<String, Integer> ench = Map.of();
    boolean unbreakable = false;
    int hideFlagsBitset = 0;

    var meta = stack.getItemMeta();
    if (meta != null) {
      if (meta.hasCustomModelData()) {
        customModelData = OptionalInt.of(meta.getCustomModelData());
      }

      if (meta.hasDisplayName()) {
        // legacy displayName access is fine for snapshots; we still use editMeta for writing elsewhere
        displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        if (displayName != null && displayName.isBlank()) displayName = null;
      }

      if (meta.hasLore() && meta.lore() != null) {
        var out = new ArrayList<String>(meta.lore().size());
        for (var line : meta.lore()) {
          out.add(PlainTextComponentSerializer.plainText().serialize(line));
        }
        lore = List.copyOf(out);
      }

      if (meta.hasEnchants()) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
          if (e.getKey() == null) continue;
          String k = e.getKey().getKey().toString(); // e.g. "minecraft:unbreaking"
          int lvl = e.getValue() == null ? 1 : Math.max(1, e.getValue());
          out.put(k, lvl);
        }
        ench = Map.copyOf(out);
      }

      unbreakable = meta.isUnbreakable();

      // Minimal hideFlags bitset mapping (optional, used later if needed)
      int bits = 0;
      for (ItemFlag f : meta.getItemFlags()) {
        int ord = f.ordinal();
        if (ord >= 0 && ord < 31) bits |= (1 << ord);
      }
      hideFlagsBitset = bits;
    }

    return Optional.of(new MenuItem(
        materialKey,
        amount,
        customModelData,
        displayName,
        lore,
        ench,
        unbreakable,
        hideFlagsBitset
    ));
  }
}