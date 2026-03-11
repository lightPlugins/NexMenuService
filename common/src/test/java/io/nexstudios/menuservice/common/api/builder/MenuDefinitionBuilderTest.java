package io.nexstudios.menuservice.common.api.builder;

import io.nexstudios.menuservice.common.api.InteractionPolicy;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuPopulator;
import io.nexstudios.menuservice.common.api.interaction.InteractionPolicies;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MenuDefinitionBuilderTest {

  @Test
  void buildShouldRequireMandatoryFields() {
    var b = MenuDefinitionBuilder.create();

    assertThrows(NullPointerException.class, b::build);

    b.key(MenuKey.of("nex", "m1"));
    assertThrows(NullPointerException.class, b::build);

    b.title("Title");
    assertThrows(NullPointerException.class, b::build);

    InteractionPolicy policy = InteractionPolicies.locked();
    b.interactionPolicy(policy);
    assertThrows(NullPointerException.class, b::build);

    MenuPopulator pop = ctx -> {};
    b.populator(pop);

    var def = b.build();
    assertEquals("nex:m1", def.key().asString());
    assertEquals("Title", def.title());
    assertEquals(6, def.rows());
    assertTrue(def.refreshInterval().isEmpty());
  }

  @Test
  void refreshIntervalOverrideShouldBeApplied() {
    var def = MenuDefinitionBuilder.create()
        .key(MenuKey.of("nex", "m1"))
        .title("Title")
        .rows(3)
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {})
        .refreshInterval(Duration.ofSeconds(2))
        .build();

    assertEquals(3, def.rows());
    assertTrue(def.refreshInterval().isPresent());
    assertEquals(Duration.ofSeconds(2), def.refreshInterval().get());
  }

  @Test
  void titleValidation() {
    assertThrows(IllegalArgumentException.class, () ->
        MenuDefinitionBuilder.create().title(" ")
    );
    assertThrows(IllegalArgumentException.class, () ->
        MenuDefinitionBuilder.create().title("a".repeat(65))
    );
  }

  @Test
  void rowsValidation() {
    assertThrows(IllegalArgumentException.class, () ->
        MenuDefinitionBuilder.create().rows(0)
    );
    assertThrows(IllegalArgumentException.class, () ->
        MenuDefinitionBuilder.create().rows(7)
    );
  }
}