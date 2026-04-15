package io.nexstudios.menuservice.paper;

import io.nexstudios.menuservice.api.MenuView;
import io.nexstudios.menuservice.core.MenuSession;
import io.nexstudios.menuservice.paper.holder.PaperMenuHolder;
import java.util.UUID;
import org.bukkit.inventory.Inventory;

final class PaperMenuSession extends MenuSession {

  private final PaperMenuHolder holder;
  private Inventory inventory;

  PaperMenuSession(UUID viewerId, PaperMenuHolder holder) {
    super(viewerId);
    this.holder = holder;
  }

  PaperMenuHolder holder() {
    return holder;
  }

  Inventory inventory() {
    return inventory;
  }

  void inventory(Inventory inventory) {
    this.inventory = inventory;
  }

  MenuView currentMenu() {
    return currentView();
  }
}

