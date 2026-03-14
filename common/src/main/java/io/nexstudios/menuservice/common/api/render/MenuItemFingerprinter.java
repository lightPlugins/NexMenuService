package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Fingerprint computation for Bukkit-bound MenuItem.
 */
public final class MenuItemFingerprinter {

  private MenuItemFingerprinter() {}

  public static long fingerprint(MenuItem item) {
    if (item == null) return 0L;

    Map<String, Object> serialized = item.stack().serialize();
    return fnv64(serialized.toString());
  }

  private static long fnv64(String s) {
    long h = 1469598103934665603L;
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    for (byte b : bytes) {
      h ^= (b & 0xff);
      h *= 1099511628211L;
    }
    return h;
  }
}