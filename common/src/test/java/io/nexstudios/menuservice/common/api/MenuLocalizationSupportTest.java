package io.nexstudios.menuservice.common.api;

import io.nexstudios.languageservice.service.path.StringPathService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuLocalizationSupportTest {

  @Test
  void shouldRecognizeDefaultAndLegacyMarkers() {
    MenuLocalizationOptions options = MenuLocalizationOptions.of();

    assertTrue(options.matches("language:test"));
    assertTrue(options.matches("lang:test"));
    assertEquals("test", options.extractKey("language:test"));
    assertEquals("test", options.extractKey("lang:test"));
    assertNull(options.extractKey("plain-text"));
  }

  @Test
  void shouldResolveStringLineBlocks() {
    MenuStringResolver resolver = new MenuStringResolver() {
      @Override
      public String resolve(String key) {
        return switch (key) {
          case "headline" -> "Resolved headline";
          default -> null;
        };
      }

      @Override
      public List<String> resolveLines(String key) {
        return switch (key) {
          case "entry" -> List.of("Line 1", "Line 2");
          default -> List.of();
        };
      }
    };

    List<String> out = MenuLocalizationSupport.resolveLines(
        List.of("before", "language:entry", "after", "lang:headline"),
        MenuLocalizationOptions.of(),
        resolver
    );

    assertEquals(List.of("before", "Line 1", "Line 2", "after", "Resolved headline"), out);
  }

  @Test
  void stringResolverShouldBridgeStringPathService() {
    StringPathService pathService = new StringPathService() {
      @Override
      public String getTranslation(org.bukkit.entity.Player player, String path, String def) {
        return def;
      }

      @Override
      public String getTranslation(String languageId, String path, String def) {
        return switch (path) {
          case "headline" -> "Resolved headline from " + languageId;
          default -> def;
        };
      }

      @Override
      public List<String> getTranslationLines(org.bukkit.entity.Player player, String path, List<String> def) {
        return def;
      }

      @Override
      public List<String> getTranslationLines(String languageId, String path, List<String> def) {
        return switch (path) {
          case "entry" -> List.of("First line from " + languageId, "Second line from " + languageId);
          default -> def;
        };
      }
    };

    MenuStringResolver resolver = MenuLocalizationSupport.stringResolver(pathService, "de");

    assertEquals("Resolved headline from de", resolver.resolve("headline"));
    assertEquals(List.of("First line from de", "Second line from de"), resolver.resolveLines("entry"));
  }

  @Test
  void textResolverShouldBridgeStringPathService() {
    StringPathService pathService = new StringPathService() {
      @Override
      public String getTranslation(org.bukkit.entity.Player player, String path, String def) {
        return def;
      }

      @Override
      public String getTranslation(String languageId, String path, String def) {
        return switch (path) {
          case "headline" -> "<yellow>Resolved headline</yellow>";
          default -> def;
        };
      }

      @Override
      public List<String> getTranslationLines(org.bukkit.entity.Player player, String path, List<String> def) {
        return def;
      }

      @Override
      public List<String> getTranslationLines(String languageId, String path, List<String> def) {
        return switch (path) {
          case "entry" -> List.of("<green>Line 1</green>", "<green>Line 2</green>");
          default -> def;
        };
      }
    };

    MenuTextResolver resolver = MenuLocalizationSupport.textResolver(pathService, "de");

    assertEquals("Resolved headline", PlainTextComponentSerializer.plainText().serialize(resolver.resolve("headline")));
    assertEquals(
        List.of("Line 1", "Line 2"),
        resolver.resolveLines("entry").stream().map(PlainTextComponentSerializer.plainText()::serialize).toList()
    );
  }

  @Test
  void shouldResolveMarkedComponentsAndViewerFreeContext() {
    MenuTextResolver resolver = new MenuTextResolver() {
      @Override
      public Component resolve(String key) {
        return "headline".equals(key) ? Component.text("Resolved headline") : null;
      }

      @Override
      public List<Component> resolveLines(String key, TagResolver tagResolver) {
        return "entry".equals(key)
            ? List.of(Component.text("Resolved line 1"), Component.text("Resolved line 2"))
            : List.of();
      }
    };

    MenuLocalizationContext context = MenuLocalizationContext.of(resolver, TagResolver.empty());
    assertNull(context.player());

    Component component = MenuLocalizationSupport.resolveComponent(
        Component.text("language:headline"),
        MenuLocalizationOptions.of(),
        resolver,
        TagResolver.empty()
    );
    assertNotNull(component);
    assertEquals("Resolved headline", PlainTextComponentSerializer.plainText().serialize(component));

    List<Component> lore = MenuLocalizationSupport.resolveLore(
        List.of(Component.text("before"), Component.text("language:entry"), Component.text("after")),
        MenuLocalizationOptions.of(),
        resolver,
        TagResolver.empty()
    );

    assertEquals(4, lore.size());
    assertEquals("before", PlainTextComponentSerializer.plainText().serialize(lore.get(0)));
    assertEquals("Resolved line 1", PlainTextComponentSerializer.plainText().serialize(lore.get(1)));
    assertEquals("Resolved line 2", PlainTextComponentSerializer.plainText().serialize(lore.get(2)));
    assertEquals("after", PlainTextComponentSerializer.plainText().serialize(lore.get(3)));
  }
}

