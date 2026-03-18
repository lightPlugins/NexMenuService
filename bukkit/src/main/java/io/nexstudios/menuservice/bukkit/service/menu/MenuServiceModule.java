package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.common.api.MenuService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Installs the Bukkit implementation of the menu service into NexServiceRegistry.
 *
 * This module must be installed by a host plugin.
 */
public final class MenuServiceModule implements ServiceModule {

  private final Plugin plugin;

  public MenuServiceModule(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
  }

  @Override
  public void install(ServiceAccessor services) {
    Objects.requireNonNull(services, "services must not be null");

    BukkitMenuRegistry registry = new BukkitMenuRegistry();
    BukkitMenuService menuService = new BukkitMenuService(plugin, registry);

    services.register(MenuService.class, menuService);

    menuService.bootstrap();
  }
}