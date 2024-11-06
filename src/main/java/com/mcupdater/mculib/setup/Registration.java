package com.mcupdater.mculib.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.mcupdater.mculib.MCULib.MODID;

public class Registration {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
        SOUNDS.register(eventBus);
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    public static final Supplier<Item> MCULIB_ICON = ITEMS.registerSimpleItem("mculib", new Item.Properties());

    public static final DeferredHolder<SoundEvent,SoundEvent> MACHINE_HUM = SOUNDS.register(
            "machine_hum",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "machine_hum")));

}
