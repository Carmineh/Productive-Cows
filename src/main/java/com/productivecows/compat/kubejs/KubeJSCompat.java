package com.productivecows.compat.kubejs;

import com.productivecows.data.CowDefinition;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

public class KubeJSCompat {
    public static void fireRegisterCowsEvent(Map<ResourceLocation, CowDefinition> registry) {
        RegisterCowsEventKJS event = new RegisterCowsEventKJS(registry);
        ProductiveCowsEvents.REGISTER_COWS.post(event);
        event.registerAll();
    }
}
