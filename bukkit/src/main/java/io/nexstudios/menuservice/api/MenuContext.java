package io.nexstudios.menuservice.api;

import io.nexstudios.menuservice.api.MenuService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Carries the runtime context for rendering and interacting with a menu.
 */
public final class MenuContext {

  private final JavaPlugin plugin;
  private final ServiceAccessor serviceAccessor;
  private final Player viewer;
  private final MenuService menuService;

  public MenuContext(JavaPlugin plugin, ServiceAccessor serviceAccessor, Player viewer, MenuService menuService) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.serviceAccessor = Objects.requireNonNull(serviceAccessor, "serviceAccessor");
    this.viewer = Objects.requireNonNull(viewer, "viewer");
    this.menuService = Objects.requireNonNull(menuService, "menuService");
  }

  public JavaPlugin plugin() {
    return plugin;
  }

  public ServiceAccessor serviceAccessor() {
    return serviceAccessor;
  }

  public Player viewer() {
    return viewer;
  }

  public MenuService menuService() {
    return menuService;
  }
}


