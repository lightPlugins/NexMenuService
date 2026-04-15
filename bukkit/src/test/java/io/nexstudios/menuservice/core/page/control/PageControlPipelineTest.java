package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageControlPipelineTest {

  private static final UUID VIEWER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final MenuKey MENU_KEY = MenuKey.of("artifact-browser");

  private record Artifact(String name, String family, int power) {
  }

  @Test
  void appliesMultipleFiltersAndSorts() {
    List<Artifact> items = List.of(
        new Artifact("Moon Shard", "relic", 96),
        new Artifact("Ancient Compass", "relic", 87),
        new Artifact("Workshop Hammer", "tool", 15),
        new Artifact("Cartographer Lens", "tool", 66)
    );

    var familyFilter = BasicPageFilterControl.<Artifact>builder("family")
        .mode("all", "All Families", artifact -> true)
        .mode("relic", "Only Relics", artifact -> artifact.family().equals("relic"))
        .mode("tool", "Only Tools", artifact -> artifact.family().equals("tool"))
        .defaultMode("relic")
        .build();

    var powerFilter = BasicPageFilterControl.<Artifact>builder("power")
        .mode("all", "All Power", artifact -> true)
        .mode("high", "Power 60+", artifact -> artifact.power() >= 60)
        .mode("low", "Power < 60", artifact -> artifact.power() < 60)
        .defaultMode("high")
        .build();

    var sortControl = BasicPageSortControl.<Artifact>builder("sort")
        .mode("name-asc", "Name A → Z", (left, right) -> left.name().compareToIgnoreCase(right.name()))
        .mode("name-desc", "Name Z → A", (left, right) -> right.name().compareToIgnoreCase(left.name()))
        .defaultMode("name-asc")
        .build();

    InMemoryPageControlStateStore store = new InMemoryPageControlStateStore();
    store.setActiveModeId(VIEWER_ID, MENU_KEY, "main", familyFilter.controlId(), "relic");
    store.setActiveModeId(VIEWER_ID, MENU_KEY, "main", powerFilter.controlId(), "high");
    store.setActiveModeId(VIEWER_ID, MENU_KEY, "main", sortControl.controlId(), "name-desc");

    List<Artifact> result = PageControlPipeline.apply(
        items,
        VIEWER_ID,
        MENU_KEY,
        "main",
        store,
        List.of(familyFilter, powerFilter),
        List.of(sortControl)
    );

    assertEquals(List.of(
        new Artifact("Moon Shard", "relic", 96),
        new Artifact("Ancient Compass", "relic", 87)
    ), result);
  }
}

