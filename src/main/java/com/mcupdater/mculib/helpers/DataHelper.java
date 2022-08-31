package com.mcupdater.mculib.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public class DataHelper {

    public static Map<Direction, Component> readDirectionMap(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<Direction, Component> output = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Direction side = Direction.values()[buf.readByte()];
            Component component = buf.readComponent();
            output.put(side,component);
        }
        return output;
    }

    public static void writeDirectionMap(FriendlyByteBuf buf, Map<Direction, Component> directionComponentMap) {
        int size = directionComponentMap.size();
        buf.writeInt(size);
        for (Map.Entry<Direction, Component> entry : directionComponentMap.entrySet()) {
            buf.writeByte(entry.getKey().ordinal());
            buf.writeComponent(entry.getValue());
        }
    }

    public static Map<Direction, Component> getAdjacentNames(Level pLevel, BlockPos pPos) {
        Map<Direction, Component> adjacentNames = new HashMap<>();
        for (Direction side : Direction.values()) {
            Component name;
            BlockEntity entity = pLevel.getBlockEntity(pPos.relative(side));
            if (entity instanceof Nameable nameable) {
                name = nameable.getDisplayName();
            } else if (entity instanceof MenuProvider menuProvider) {
                name = menuProvider.getDisplayName();
            } else {
                name = pLevel.getBlockState(pPos.relative(side)).getBlock().getName();
            }
            adjacentNames.put(side,name);
        }
        return adjacentNames;
    }
}
