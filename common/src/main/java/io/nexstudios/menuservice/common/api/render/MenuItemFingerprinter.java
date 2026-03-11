package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Default fingerprint computation for {@link MenuItem}.
 *
 * This is intentionally deterministic and platform-agnostic.
 */
public final class MenuItemFingerprinter {

  private MenuItemFingerprinter() {}

  public static long fingerprint(MenuItem item) {
    if (item == null) return 0L;

    long h = 1469598103934665603L; // FNV-1a 64 offset
    h = fnv(h, item.materialKey());
    h = fnv(h, item.amount());
    h = fnv(h, item.customModelData());
    h = fnv(h, item.displayName());
    h = fnv(h, item.lore());
    h = fnv(h, item.enchantments());
    h = fnv(h, item.unbreakable() ? 1 : 0);
    h = fnv(h, item.hideFlagsBitset());
    return h;
  }

  private static long fnv(long h, int v) {
    h ^= (v & 0xff);
    h *= 1099511628211L;
    h ^= ((v >> 8) & 0xff);
    h *= 1099511628211L;
    h ^= ((v >> 16) & 0xff);
    h *= 1099511628211L;
    h ^= ((v >> 24) & 0xff);
    h *= 1099511628211L;
    return h;
  }

  private static long fnv(long h, OptionalInt v) {
    return fnv(h, v.isPresent() ? v.getAsInt() : -1);
  }

  private static long fnv(long h, String s) {
    if (s == null) return fnv(h, 0);
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    for (byte b : bytes) {
      h ^= (b & 0xff);
      h *= 1099511628211L;
    }
    return h;
  }

  private static long fnv(long h, Iterable<String> list) {
    if (list == null) return fnv(h, 0);
    for (String s : list) h = fnv(h, s);
    return fnv(h, 1);
  }

  private static long fnv(long h, Map<String, Integer> map) {
    if (map == null) return fnv(h, 0);

    var keys = new ArrayList<>(map.keySet());
    keys.sort(String::compareTo);

    for (String k : keys) {
      h = fnv(h, k);
      Integer v = map.get(k);
      h = fnv(h, v == null ? 0 : v);
    }

    return fnv(h, 1);
  }
}