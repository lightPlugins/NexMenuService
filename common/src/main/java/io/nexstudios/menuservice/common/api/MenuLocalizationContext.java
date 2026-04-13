package io.nexstudios.menuservice.common.api;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Optional localization context supplied when opening a menu.
 */
public record MenuLocalizationContext(Player player, MenuTextResolver resolver, TagResolver tagResolver) {

  public MenuLocalizationContext {
    Objects.requireNonNull(player, "player must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");
    tagResolver = tagResolver == null ? TagResolver.empty() : tagResolver;
  }

  public static MenuLocalizationContext of(Player player, MenuTextResolver resolver) {
    return new MenuLocalizationContext(player, resolver, TagResolver.empty());
  }

  public static MenuLocalizationContext of(Player player, MenuTextResolver resolver, TagResolver tagResolver) {
    return new MenuLocalizationContext(player, resolver, tagResolver);
  }
}

