package io.nexstudios.menuservice.common.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Resolves language keys to raw string values.
 *
 * This is the preferred adapter when the host plugin already has a string-based language service
 * and wants to resolve menu text before converting it to Components.
 */
public interface MenuStringResolver {

  /**
   * Resolves a language key to a single string.
   *
   * @return the resolved text or {@code null} if the key is unknown
   */
  @Nullable String resolve(String key);

  /**
   * Resolves a language key to a single string, falling back to {@code def} when unknown.
   */
  default String resolve(String key, String def) {
    String resolved = resolve(key);
    return resolved == null ? def : resolved;
  }

  /**
   * Resolves a language key to one or more raw lore lines.
   *
   * The default implementation treats the single resolved string as a one-line list.
   */
  default List<String> resolveLines(String key) {
    String resolved = resolve(key);
    return resolved == null ? List.of() : List.of(resolved);
  }

  /**
   * Resolves a language key to one or more raw lore lines, falling back to {@code def} when unknown.
   */
  default List<String> resolveLines(String key, List<String> def) {
    List<String> resolved = resolveLines(key);
    return resolved.isEmpty() ? def : resolved;
  }
}
