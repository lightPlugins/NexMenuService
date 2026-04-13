package io.nexstudios.menuservice.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface MenuTextResolver {

  /**
   * Resolves a language key to a component.
   *
   * Return {@code null} if the key is unknown and the original marker should remain unchanged.
   */
  @Nullable Component resolve(String key);

  /**
   * Resolves a language key to a component while allowing MiniMessage tags.
   *
   * Default implementation ignores the resolver and delegates to {@link #resolve(String)}.
   */
  default @Nullable Component resolve(String key, TagResolver resolver) {
    return resolve(key);
  }

  /**
   * Resolves a language key to one or more lore lines.
   *
   * Return an empty list if the key is unknown and the original marker should remain unchanged.
   */
  default List<Component> resolveLines(String key) {
    Component component = resolve(key);
    return component == null ? List.of() : List.of(component);
  }

  /**
   * Resolves a language key to one or more lore lines while allowing MiniMessage tags.
   *
   * Default implementation ignores the resolver and delegates to {@link #resolveLines(String)}.
   */
  default List<Component> resolveLines(String key, TagResolver resolver) {
    return resolveLines(key);
  }
}

