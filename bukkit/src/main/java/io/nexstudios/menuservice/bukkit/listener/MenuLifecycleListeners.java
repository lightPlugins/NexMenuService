package io.nexstudios.menuservice.bukkit.listener;

import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuService;
import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuView;
import io.nexstudios.menuservice.common.api.CloseReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.UUID;

/**
 * Bridges server lifecycle events into the menu runtime.
 */
public final class MenuLifecycleListeners implements Listener {

  private final BukkitMenuService service;
  private final Plugin owningPlugin;

  public MenuLifecycleListeners(BukkitMenuService service, Plugin owningPlugin) {
    this.service = Objects.requireNonNull(service, "service must not be null");
    this.owningPlugin = Objects.requireNonNull(owningPlugin, "owningPlugin must not be null");
  }

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (!event.getPlugin().equals(owningPlugin)) return;
    service.shutdown();
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    UUID id = p.getUniqueId();

    BukkitMenuView view = service.findOpenView(id);
    if (view != null) {
      view.close(CloseReason.PLAYER_DISCONNECTED);
    }
  }
}