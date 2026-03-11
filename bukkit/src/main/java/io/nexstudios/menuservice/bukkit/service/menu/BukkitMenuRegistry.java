package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuRegistry;
import io.nexstudios.menuservice.common.api.registry.DuplicateMenuKeyException;
import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.menuservice.common.api.registry.RegistrationResult;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe menu registry for Bukkit runtime.
 */
public final class BukkitMenuRegistry implements MenuRegistry {

  private final Map<MenuKey, MenuDefinition> definitions = new ConcurrentHashMap<>();

  @Override
  public RegistrationResult register(MenuDefinition definition, DuplicateStrategy duplicateStrategy) {
    Objects.requireNonNull(definition, "definition must not be null");
    Objects.requireNonNull(duplicateStrategy, "duplicateStrategy must not be null");

    MenuKey key = Objects.requireNonNull(definition.key(), "definition.key() must not be null");

    return switch (duplicateStrategy) {
      case FAIL -> registerFail(key, definition);
      case REPLACE -> registerReplace(key, definition);
    };
  }

  private RegistrationResult registerFail(MenuKey key, MenuDefinition definition) {
    MenuDefinition existing = definitions.putIfAbsent(key, definition);
    if (existing != null) {
      throw new DuplicateMenuKeyException(key);
    }
    return RegistrationResult.added(key);
  }

  private RegistrationResult registerReplace(MenuKey key, MenuDefinition definition) {
    MenuDefinition prev = definitions.put(key, definition);
    if (prev == null) return RegistrationResult.added(key);
    return RegistrationResult.replaced(key, prev);
  }

  @Override
  public Optional<MenuDefinition> find(MenuKey key) {
    Objects.requireNonNull(key, "key must not be null");
    return Optional.ofNullable(definitions.get(key));
  }

  @Override
  public Collection<MenuDefinition> all() {
    return definitions.values();
  }
}