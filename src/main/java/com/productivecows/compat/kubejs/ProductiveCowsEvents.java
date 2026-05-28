package com.productivecows.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ProductiveCowsEvents {
    EventGroup GROUP = EventGroup.of("ProductiveCowsEvents");

    EventHandler REGISTER_COWS = GROUP.server("registerCows", () -> RegisterCowsEventKJS.class);
}
