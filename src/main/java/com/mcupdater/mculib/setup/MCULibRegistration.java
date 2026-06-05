package com.mcupdater.mculib.setup;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
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

public class MCULibRegistration {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

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

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> ITEM_GROUP = TABS.register(MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(MCULIB_ICON.get()))
            .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_ENERGY = DATA_COMPONENTS.register("energy", () -> DataComponentType.<Integer>builder().persistent(Codec.INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_ENERGY = DATA_COMPONENTS.register("max_energy", () -> DataComponentType.<Integer>builder().persistent(Codec.INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_RECEIVE = DATA_COMPONENTS.register("max_receive", () -> DataComponentType.<Integer>builder().persistent(Codec.INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_EXTRACT = DATA_COMPONENTS.register("max_extract", () -> DataComponentType.<Integer>builder().persistent(Codec.INT.orElse(0)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
}
