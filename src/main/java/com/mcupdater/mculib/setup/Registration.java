package com.mcupdater.mculib.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.mcupdater.mculib.MCULib.MODID;

public class Registration {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
        SOUNDS.register(eventBus);
        TABS.register(eventBus);
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    public static final Supplier<Item> MCULIB_ICON = ITEMS.registerSimpleItem("mculib", new Item.Properties());

    public static final DeferredHolder<SoundEvent,SoundEvent> MACHINE_HUM = SOUNDS.register(
            "machine_hum",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "machine_hum")));

    public static final Supplier<CreativeModeTab> ITEM_GROUP = TABS.register(MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(MCULIB_ICON.get()))
            .build()
    );
}
