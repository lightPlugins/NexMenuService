package io.nexstudios.menuservice.core;

import io.nexstudios.menuservice.api.MenuDefinition;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuRegistry;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory menu registry implementation.
 */
public final class DefaultMenuRegistry implements MenuRegistry {

  private final Map<MenuKey, MenuDefinition> definitions = new LinkedHashMap<>();

  @Override
  public void register(MenuDefinition definition) {
    Objects.requireNonNull(definition, "definition");
    MenuDefinition previous = definitions.putIfAbsent(definition.key(), definition);
    if (previous != null) {
      throw new IllegalStateException("A menu definition is already registered for key %s".formatted(definition.key()));
    }
  }

  @Override
  public Optional<MenuDefinition> find(MenuKey key) {
    return Optional.ofNullable(definitions.get(Objects.requireNonNull(key, "key")));
  }

  @Override
  public Collection<MenuKey> keys() {
    return Map.copyOf(definitions).keySet();
  }
}

