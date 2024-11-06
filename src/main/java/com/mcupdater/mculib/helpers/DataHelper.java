package com.mcupdater.mculib.helpers;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class DataHelper {

    public static Map<Direction, String> readDirectionMap(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<Direction, String> output = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Direction side = Direction.values()[buf.readByte()];
            String name = buf.readUtf();
            output.put(side,name);
        }
        return output;
    }

    public static void writeDirectionMap(FriendlyByteBuf buf, Map<Direction, String> directionComponentMap) {
        int size = directionComponentMap.size();
        buf.writeInt(size);
        for (Map.Entry<Direction, String> entry : directionComponentMap.entrySet()) {
            buf.writeByte(entry.getKey().ordinal());
            buf.writeUtf(entry.getValue());
        }
    }

    public static Map<Direction, String> getAdjacentNames(Level pLevel, BlockPos pPos) {
        Map<Direction, String> adjacentNames = new HashMap<>();
        for (Direction side : Direction.values()) {
            String name;
            BlockEntity entity = pLevel.getBlockEntity(pPos.relative(side));
            if (entity instanceof Nameable nameable) {
                name = nameable.getDisplayName().getString();
            } else if (entity instanceof MenuProvider menuProvider) {
                name = menuProvider.getDisplayName().getString();
            } else {
                name = pLevel.getBlockState(pPos.relative(side)).getBlock().getName().getString();
            }
            adjacentNames.put(side,name);
        }
        return adjacentNames;
    }

    public static FluidStack getJsonFluidStack(JsonObject json) {
        if (json.has("fluid") && json.has("fluidAmount")) {
            String fluidName = GsonHelper.getAsString(json, "fluid");
            ResourceLocation resourceLocation = ResourceLocation.parse(fluidName);
            Fluid fluid = BuiltInRegistries.FLUID.get(resourceLocation);
            int fluidAmount = GsonHelper.getAsInt(json, "fluidAmount");
            return new FluidStack(fluid, fluidAmount);
        } else {
            return FluidStack.EMPTY;
        }
    }
}
