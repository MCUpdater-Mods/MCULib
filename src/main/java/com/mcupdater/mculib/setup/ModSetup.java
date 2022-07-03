package com.mcupdater.mculib.setup;

import com.mcupdater.mculib.MCULib;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = MCULib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final CreativeModeTab MCULIB_ITEM_GROUP = new CreativeModeTab("mculib") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.MCULIB_ICON.get());
        }
    };

    public static void init(final FMLCommonSetupEvent event) {
        //Do nothing
    }
}
