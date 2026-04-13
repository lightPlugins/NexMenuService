package io.nexstudios.menuservice.common.api;

import io.nexstudios.languageservice.service.path.StringPathService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Locale;

/**
 * Shared helper for resolving language markers like {@code language:test}.
 */
public final class MenuLocalizationSupport {

  private MenuLocalizationSupport() {}

  /**
   * Creates a raw-string resolver backed by the given language path service and language id.
   */
  public static MenuStringResolver stringResolver(StringPathService stringPathService, String languageId) {
    Objects.requireNonNull(stringPathService, "stringPathService must not be null");
    Objects.requireNonNull(languageId, "languageId must not be null");

    String normalized = languageId.trim().toLowerCase(Locale.ROOT);
    return new MenuStringResolver() {
      @Override
      public @Nullable String resolve(String key) {
        return stringPathService.getTranslation(normalized, key, null);
      }

      @Override
      public List<String> resolveLines(String key) {
        return stringPathService.getTranslationLines(normalized, key, List.of());
      }
    };
  }

  /**
   * Creates a component resolver backed by the given language path service and language id.
   *
   * The returned resolver parses the resolved raw language strings as MiniMessage.
   */
  public static MenuTextResolver textResolver(StringPathService stringPathService, String languageId) {
    Objects.requireNonNull(stringPathService, "stringPathService must not be null");
    Objects.requireNonNull(languageId, "languageId must not be null");

    MenuStringResolver stringResolver = stringResolver(stringPathService, languageId);
    MiniMessage miniMessage = MiniMessage.miniMessage();

    return new MenuTextResolver() {
      @Override
      public @Nullable Component resolve(String key) {
        String raw = stringResolver.resolve(key);
        return raw == null ? null : miniMessage.deserialize(raw);
      }

      @Override
      public @Nullable Component resolve(String key, TagResolver resolver) {
        String raw = stringResolver.resolve(key);
        return raw == null ? null : miniMessage.deserialize(raw, resolver);
      }

      @Override
      public List<Component> resolveLines(String key) {
        List<String> lines = stringResolver.resolveLines(key);
        if (lines.isEmpty()) return List.of();

        List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
          if (line == null) continue;
          components.add(miniMessage.deserialize(line));
        }
        return List.copyOf(components);
      }

      @Override
      public List<Component> resolveLines(String key, TagResolver resolver) {
        List<String> lines = stringResolver.resolveLines(key);
        if (lines.isEmpty()) return List.of();

        List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
          if (line == null) continue;
          components.add(miniMessage.deserialize(line, resolver));
        }
        return List.copyOf(components);
      }
    };
  }

  public static boolean isMarked(@Nullable String raw, MenuLocalizationOptions options) {
    Objects.requireNonNull(options, "options must not be null");
    return options.matches(raw);
  }

  public static @Nullable String extractKey(@Nullable String raw, MenuLocalizationOptions options) {
    Objects.requireNonNull(options, "options must not be null");
    return options.extractKey(raw);
  }

  public static @Nullable Component resolveComponent(
      @Nullable Component source,
      MenuLocalizationOptions options,
      MenuTextResolver resolver,
      TagResolver tagResolver
  ) {
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");
    Objects.requireNonNull(tagResolver, "tagResolver must not be null");

    if (source == null) return null;

    String key = extractKey(PlainTextComponentSerializer.plainText().serialize(source), options);
    if (key == null || key.isBlank()) return null;

    Component resolved = resolver.resolve(key, tagResolver);
    if (resolved == null) return null;

    return resolved.decoration(TextDecoration.ITALIC, false);
  }

  public static @Nullable List<Component> resolveLoreLine(
      @Nullable Component source,
      MenuLocalizationOptions options,
      MenuTextResolver resolver,
      TagResolver tagResolver
  ) {
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");
    Objects.requireNonNull(tagResolver, "tagResolver must not be null");

    if (source == null) return null;

    String key = extractKey(PlainTextComponentSerializer.plainText().serialize(source), options);
    if (key == null || key.isBlank()) return null;

    List<Component> resolved = resolver.resolveLines(key, tagResolver);
    if (resolved == null || resolved.isEmpty()) return null;

    List<Component> translated = new ArrayList<>(resolved.size());
    for (Component component : resolved) {
      if (component == null) continue;
      translated.add(component.decoration(TextDecoration.ITALIC, false));
    }
    return translated.isEmpty() ? null : List.copyOf(translated);
  }

  public static @Nullable String resolveString(
      @Nullable String raw,
      MenuLocalizationOptions options,
      MenuStringResolver resolver
  ) {
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");

    String key = extractKey(raw, options);
    if (key == null || key.isBlank()) return null;
    return resolver.resolve(key);
  }

  public static @Nullable List<String> resolveLineBlock(
      @Nullable String raw,
      MenuLocalizationOptions options,
      MenuStringResolver resolver
  ) {
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");

    String key = extractKey(raw, options);
    if (key == null || key.isBlank()) return null;

    List<String> resolvedLines = resolver.resolveLines(key, List.of());
    if (!resolvedLines.isEmpty()) {
      return List.copyOf(resolvedLines);
    }

    String resolved = resolver.resolve(key);
    return resolved == null ? null : List.of(resolved);
  }

  public static List<String> resolveLines(
      List<String> source,
      MenuLocalizationOptions options,
      MenuStringResolver resolver
  ) {
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");

    if (source.isEmpty()) return List.of();

    List<String> out = new ArrayList<>(source.size());
    for (String line : source) {
      List<String> resolvedBlock = resolveLineBlock(line, options, resolver);
      if (resolvedBlock == null) {
        out.add(line);
      } else {
        out.addAll(resolvedBlock);
      }
    }

    return List.copyOf(out);
  }

  public static List<Component> resolveLore(
      List<Component> source,
      MenuLocalizationOptions options,
      MenuTextResolver resolver,
      TagResolver tagResolver
  ) {
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");
    Objects.requireNonNull(tagResolver, "tagResolver must not be null");

    if (source.isEmpty()) return List.of();

    List<Component> out = new ArrayList<>(source.size());
    for (Component line : source) {
      List<Component> resolvedBlock = resolveLoreLine(line, options, resolver, tagResolver);
      if (resolvedBlock == null) {
        out.add(line);
      } else {
        out.addAll(resolvedBlock);
      }
    }

    return List.copyOf(out);
  }

  /**
   * Localizes the display-name and lore of a Bukkit item if they start with a language marker.
   *
   * The item is cloned before mutation.
   */
  public static ItemStack localizeItem(
      ItemStack stack,
      MenuLocalizationOptions options,
      MenuTextResolver resolver,
      TagResolver tagResolver
  ) {
    Objects.requireNonNull(stack, "stack must not be null");
    Objects.requireNonNull(options, "options must not be null");
    Objects.requireNonNull(resolver, "resolver must not be null");
    Objects.requireNonNull(tagResolver, "tagResolver must not be null");

    ItemStack out = stack.clone();
    ItemMeta meta = out.getItemMeta();
    if (meta == null) {
      return out;
    }

    boolean changed = false;

    if (meta.hasDisplayName()) {
      Component translated = resolveComponent(meta.displayName(), options, resolver, tagResolver);
      if (translated != null) {
        meta.displayName(translated);
        changed = true;
      }
    }

    if (meta.hasLore()) {
      List<Component> lore = meta.lore();
      if (lore != null && !lore.isEmpty()) {
        List<Component> translatedLore = resolveLore(lore, options, resolver, tagResolver);
        if (translatedLore != null && !translatedLore.equals(lore)) {
          meta.lore(translatedLore);
          changed = true;
        }
      }
    }

    if (changed) {
      out.setItemMeta(meta);
    }

    return out;
  }
}


