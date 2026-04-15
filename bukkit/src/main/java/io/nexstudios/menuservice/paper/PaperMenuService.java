package io.nexstudios.menuservice.paper;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuDefinition;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuRegistry;
import io.nexstudios.menuservice.api.MenuService;
import io.nexstudios.menuservice.api.MenuView;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import io.nexstudios.menuservice.paper.holder.PaperMenuHolder;

/**
 * Paper-backed menu service implementation.
 */
public final class PaperMenuService implements MenuService {

  private final JavaPlugin plugin;
  private final ServiceAccessor serviceAccessor;
  private final MenuRegistry registry;
  private final PaperMenuRenderer renderer;
  private final Map<UUID, PaperMenuSession> sessions = new ConcurrentHashMap<>();

  public PaperMenuService(JavaPlugin plugin, ServiceAccessor serviceAccessor, MenuRegistry registry, PaperMenuRenderer renderer) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.serviceAccessor = Objects.requireNonNull(serviceAccessor, "serviceAccessor");
    this.registry = Objects.requireNonNull(registry, "registry");
    this.renderer = Objects.requireNonNull(renderer, "renderer");
  }

  @Override
  public void open(Player player, MenuView view) {
    open(player, view, true);
  }

  @Override
  public void open(Player player, MenuKey key) {
    MenuDefinition definition = registry.find(Objects.requireNonNull(key, "key"))
        .orElseThrow(() -> new IllegalArgumentException("Unknown menu key: %s".formatted(key)));
    MenuContext context = createContext(player);
    open(player, definition.create(context), true);
  }

  @Override
  public void refresh(Player player) {
    PaperMenuSession session = sessions.get(player.getUniqueId());
    if (session == null || session.currentMenu() == null) {
      return;
    }
    open(player, session.currentMenu(), false);
  }

  @Override
  public void back(Player player) {
    PaperMenuSession session = sessions.get(player.getUniqueId());
    if (session == null || session.history().isEmpty()) {
      close(player);
      return;
    }
    MenuView previous = session.history().pop();
    open(player, previous, false);
  }

  @Override
  public void close(Player player) {
    PaperMenuSession session = sessions.remove(player.getUniqueId());
    if (session != null && session.currentMenu() != null) {
      session.currentMenu().onClose(createContext(player));
    }
    player.closeInventory();
  }

  @Override
  public Optional<MenuKey> currentMenu(UUID viewerId) {
    PaperMenuSession session = sessions.get(Objects.requireNonNull(viewerId, "viewerId"));
    return session == null || session.currentMenu() == null ? Optional.empty() : Optional.of(session.currentMenu().key());
  }

  @Override
  public Optional<MenuView> currentView(UUID viewerId) {
    PaperMenuSession session = sessions.get(Objects.requireNonNull(viewerId, "viewerId"));
    return session == null ? Optional.empty() : Optional.ofNullable(session.currentMenu());
  }

  PaperMenuSession session(UUID viewerId) {
    return sessions.get(viewerId);
  }

  void clearSession(UUID viewerId) {
    sessions.remove(viewerId);
  }

  MenuContext createContext(Player player) {
    return new MenuContext(plugin, serviceAccessor, player, this);
  }

  private void open(Player player, MenuView view, boolean pushHistory) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(view, "view");

    UUID viewerId = player.getUniqueId();
    PaperMenuSession session = sessions.computeIfAbsent(viewerId, id -> new PaperMenuSession(id, new PaperMenuHolder(id, view.key())));
    PaperMenuHolder holder = session.holder();
    holder.menuKey(view.key());
    MenuView currentMenu = session.currentMenu();
    if (currentMenu != null) {
      if (pushHistory && currentMenu != view) {
        session.history().push(currentMenu);
      }
      if (currentMenu != view) {
        currentMenu.onClose(createContext(player));
      }
    }

    MenuContext context = createContext(player);
    Inventory inventory = renderer.render(context, view, holder);
    session.inventory(inventory);
    session.currentView(view);
    session.skipNextCloseCleanup(true);
    player.openInventory(inventory);
    view.onOpen(context);
  }
}




