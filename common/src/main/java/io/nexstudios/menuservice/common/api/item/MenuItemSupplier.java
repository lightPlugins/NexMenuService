package io.nexstudios.menuservice.common.api.item;

@FunctionalInterface
public interface MenuItemSupplier {
  MenuItem get();
}