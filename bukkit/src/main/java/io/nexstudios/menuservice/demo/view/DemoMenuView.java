package io.nexstudios.menuservice.demo.view;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.page.PageBounds;
import io.nexstudios.menuservice.api.page.PageItemRenderer;
import io.nexstudios.menuservice.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.api.page.control.PageSortControl;
import io.nexstudios.menuservice.core.element.StaticMenuElement;
import io.nexstudios.menuservice.core.page.ControlledPagedMenuView;
import io.nexstudios.menuservice.core.page.RefreshElement;
import io.nexstudios.menuservice.core.page.element.NextPageElement;
import io.nexstudios.menuservice.core.page.element.PageIndicatorElement;
import io.nexstudios.menuservice.core.page.element.PreviousPageElement;
import io.nexstudios.menuservice.core.page.control.BasicPageFilterControl;
import io.nexstudios.menuservice.core.page.control.BasicPageSortControl;
import io.nexstudios.menuservice.demo.definition.DemoMenuDefinition;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Demo menu that shows a paged list of artifacts with two filters and one sort control.
 */
public final class DemoMenuView extends ControlledPagedMenuView<DemoMenuView.DemoArtifact> {

  private static final List<DemoArtifact> ITEMS = List.of(
      new DemoArtifact("Ancient Compass", Family.RELIC, Rarity.RARE, 87, Material.COMPASS),
      new DemoArtifact("Explorer Lantern", Family.TOOL, Rarity.UNCOMMON, 25, Material.TORCH),
      new DemoArtifact("Moon Shard", Family.RELIC, Rarity.EPIC, 96, Material.AMETHYST_SHARD),
      new DemoArtifact("Workshop Hammer", Family.TOOL, Rarity.COMMON, 15, Material.IRON_AXE),
      new DemoArtifact("Sun Charm", Family.CHARM, Rarity.RARE, 54, Material.GLOWSTONE_DUST),
      new DemoArtifact("Rune Needle", Family.CHARM, Rarity.UNCOMMON, 31, Material.FEATHER),
      new DemoArtifact("Vault Key", Family.RELIC, Rarity.COMMON, 11, Material.TRIPWIRE_HOOK),
      new DemoArtifact("Cartographer Lens", Family.TOOL, Rarity.RARE, 66, Material.SPYGLASS),
      new DemoArtifact("Frost Sigil", Family.CHARM, Rarity.EPIC, 92, Material.ICE),
      new DemoArtifact("Archivist Pen", Family.TOOL, Rarity.UNCOMMON, 23, Material.CLAY_BALL),
      new DemoArtifact("Obsidian Idol", Family.RELIC, Rarity.EPIC, 99, Material.OBSIDIAN),
      new DemoArtifact("Traveler's Token", Family.CHARM, Rarity.COMMON, 9, Material.GOLD_NUGGET),
      new DemoArtifact("Tinker Saw", Family.TOOL, Rarity.RARE, 61, Material.SHEARS),
      new DemoArtifact("Lodestone Emblem", Family.RELIC, Rarity.UNCOMMON, 38, Material.LODESTONE),
      new DemoArtifact("Whisper Ring", Family.CHARM, Rarity.RARE, 73, Material.ENDER_PEARL)
  );

  private final PageFilterControl<DemoArtifact> familyFilter;
  private final PageFilterControl<DemoArtifact> rarityFilter;
  private final PageSortControl<DemoArtifact> sortControl;

  public DemoMenuView() {
    super(
        DemoMenuDefinition.KEY,
        27,
        PageBounds.of(0, 1, 9, 1),
        "demo-artifacts",
        ITEMS,
        createRenderer()
    );

    fill(createBackgroundPane());

    this.familyFilter = addFilterControl(1, "Family Filter", Material.HOPPER,
        BasicPageFilterControl.<DemoArtifact>builder("family-filter")
            .mode("all", "All Families", artifact -> true)
            .mode("relic", "Only Relics", artifact -> artifact.family() == Family.RELIC)
            .mode("tool", "Only Tools", artifact -> artifact.family() == Family.TOOL)
            .mode("charm", "Only Charms", artifact -> artifact.family() == Family.CHARM)
            .defaultMode("all")
            .build());

    this.rarityFilter = addFilterControl(3, "Rarity Filter", Material.NETHER_STAR,
        BasicPageFilterControl.<DemoArtifact>builder("rarity-filter")
            .mode("all", "All Rarities", artifact -> true)
            .mode("common", "Only Common", artifact -> artifact.rarity() == Rarity.COMMON)
            .mode("uncommon", "Only Uncommon", artifact -> artifact.rarity() == Rarity.UNCOMMON)
            .mode("rare", "Only Rare", artifact -> artifact.rarity() == Rarity.RARE)
            .mode("epic", "Only Epic", artifact -> artifact.rarity() == Rarity.EPIC)
            .defaultMode("all")
            .build());

    this.sortControl = addSortControl(5, "Artifact Sort", Material.COMPASS,
        BasicPageSortControl.<DemoArtifact>builder("artifact-sort")
            .mode("name-asc", "Name A → Z", DemoArtifact::nameComparatorAsc)
            .mode("name-desc", "Name Z → A", DemoArtifact::nameComparatorDesc)
            .mode("power-low", "Power Low → High", DemoArtifact::powerComparatorAsc)
            .mode("power-high", "Power High → Low", DemoArtifact::powerComparatorDesc)
            .defaultMode("name-asc")
            .build());

    setTitle(context -> Component.text("Artifact Browser | "
        + "Family: " + familyFilter.labelForMode(activeModeId(familyFilter, context))
        + " | Rarity: " + rarityFilter.labelForMode(activeModeId(rarityFilter, context))
        + " | Sort: " + sortControl.labelForMode(activeModeId(sortControl, context)))
        .color(NamedTextColor.GOLD)
        .decoration(TextDecoration.ITALIC, false));

    addElement(2, 2, new PreviousPageElement());
    addElement(4, 2, new PageIndicatorElement());
    addElement(6, 2, new RefreshElement());
    addElement(8, 2, new NextPageElement());

    setOnView(this::onView);
    setCloseListener(this::handleClose);
  }

  private void onView(MenuContext context) {
    // No-op for now; hooks are available for future setup or live refresh logic.
  }

  private void handleClose(MenuContext context) {
    // No-op for now; hooks are available for future cleanup logic.
  }

  private static PageItemRenderer<DemoArtifact> createRenderer() {
    return (context, item, globalIndex) -> new StaticMenuElement(createItemStack(item, globalIndex),
        (menuContext, event) -> menuContext.menuService().open(menuContext.viewer(), new DemoDetailMenuView(item.name(), globalIndex)));
  }

  private static ItemStack createItemStack(DemoArtifact item, int globalIndex) {
    ItemStack itemStack = new ItemStack(item.material());
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text(item.name())
          .color(NamedTextColor.AQUA)
          .decoration(TextDecoration.ITALIC, false)
          .decorate(TextDecoration.BOLD));
      meta.lore(List.of(
          Component.text("Family: " + item.family().displayName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
          Component.text("Rarity: " + item.rarity().displayName()).color(item.rarity().color()).decoration(TextDecoration.ITALIC, false),
          Component.text("Power: %d".formatted(item.power())).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
          Component.text("Index: %d".formatted(globalIndex)).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false),
          Component.text("Click to open details").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
      ));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  private static ItemStack createBackgroundPane() {
    ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.empty());
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  record DemoArtifact(String name, Family family, Rarity rarity, int power, Material material) {

    private static int nameComparatorAsc(DemoArtifact left, DemoArtifact right) {
      return left.name.compareToIgnoreCase(right.name);
    }

    private static int nameComparatorDesc(DemoArtifact left, DemoArtifact right) {
      return right.name.compareToIgnoreCase(left.name);
    }

    private static int powerComparatorAsc(DemoArtifact left, DemoArtifact right) {
      return Integer.compare(left.power, right.power);
    }

    private static int powerComparatorDesc(DemoArtifact left, DemoArtifact right) {
      return Integer.compare(right.power, left.power);
    }
  }

  private enum Family {
    RELIC("Relic"),
    TOOL("Tool"),
    CHARM("Charm");

    private final String displayName;

    Family(String displayName) {
      this.displayName = displayName;
    }

    private String displayName() {
      return displayName;
    }
  }

  private enum Rarity {
    COMMON("Common", NamedTextColor.GREEN),
    UNCOMMON("Uncommon", NamedTextColor.BLUE),
    RARE("Rare", NamedTextColor.LIGHT_PURPLE),
    EPIC("Epic", NamedTextColor.GOLD);

    private final String displayName;
    private final NamedTextColor color;

    Rarity(String displayName, NamedTextColor color) {
      this.displayName = displayName;
      this.color = color;
    }

    private String displayName() {
      return displayName;
    }

    private NamedTextColor color() {
      return color;
    }
  }
}



