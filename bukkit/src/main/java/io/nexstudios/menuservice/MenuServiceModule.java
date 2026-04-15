package io.nexstudios.menuservice;

import io.nexstudios.menuservice.api.MenuRegistry;
import io.nexstudios.menuservice.api.MenuService;
import io.nexstudios.menuservice.core.DefaultMenuRegistry;
import io.nexstudios.menuservice.paper.PaperMenuListener;
import io.nexstudios.menuservice.paper.PaperMenuRenderer;
import io.nexstudios.menuservice.paper.PaperMenuService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Registers the menu service into the external service registry.
 */
public class MenuServiceModule implements ServiceModule {

  private final JavaPlugin plugin;

  public MenuServiceModule(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void install(ServiceAccessor serviceAccessor) {

    MenuRegistry registry = new DefaultMenuRegistry();
    PaperMenuRenderer renderer = new PaperMenuRenderer(plugin);
    PaperMenuService menuService = new PaperMenuService(plugin, serviceAccessor, registry, renderer);

    serviceAccessor.register(MenuRegistry.class, registry);
    serviceAccessor.register(MenuService.class, menuService);

    plugin.getServer().getPluginManager().registerEvents(new PaperMenuListener(menuService), plugin);
  }
}
