package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.item.PlannedMenuItemSupplier;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class PageRendererPlannedMenuItemSupplierTest {

  @Test
  void renderPageKeepsPlannedHeadSupplierInPageContent() {
    CompletableFuture<ItemStack> future = new CompletableFuture<>();
    PlannedMenuItemSupplier supplier = new PlannedMenuItemSupplier() {
      @Override
      public io.nexstudios.menuservice.common.api.item.MenuItem placeholder() {
        return null;
      }

      @Override
      public CompletableFuture<ItemStack> headFuture() {
        return future;
      }
    };

    PagedAreaDefinition<String> definition = new PagedAreaDefinition<>(
        "heads",
        new PageBounds(0, 0, 1, 1, PageAlignment.LEFT),
        (MenuKey key, ViewerRef viewer) -> List.of("steve"),
        (element, index) -> supplier,
        PageNavigation.none()
    );

    var plan = PageRenderer.renderPage(definition, 0, List.of("steve"));
    var planned = plan.slotsToItems().values().iterator().next();

    assertSame(supplier, planned);
    assertNull(planned.get());
    assertSame(future, ((PlannedMenuItemSupplier) planned).headFuture());
  }

  @Test
  void plannedSupplierExposesPlaceholderAndFuture() {
    CompletableFuture<ItemStack> future = new CompletableFuture<>();

    PlannedMenuItemSupplier supplier = new PlannedMenuItemSupplier() {
      @Override
      public io.nexstudios.menuservice.common.api.item.MenuItem placeholder() {
        return null;
      }

      @Override
      public CompletableFuture<ItemStack> headFuture() {
        return future;
      }
    };

    assertNull(supplier.placeholder());
    assertNull(supplier.get());
    assertSame(future, supplier.headFuture());
  }
}



