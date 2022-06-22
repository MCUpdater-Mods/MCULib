package com.mcupdater.mculib.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ArmorSlotItemHandler extends SlotItemHandler {
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");

    private final EquipmentSlot slotType;
    private final Player player;
    private static final Map<EquipmentSlot, ResourceLocation> ARMOR_SLOT_TEXTURES = new HashMap<>();

    static {
        ARMOR_SLOT_TEXTURES.put(EquipmentSlot.OFFHAND, EMPTY_ARMOR_SLOT_SHIELD);
        ARMOR_SLOT_TEXTURES.put(EquipmentSlot.HEAD, EMPTY_ARMOR_SLOT_HELMET);
        ARMOR_SLOT_TEXTURES.put(EquipmentSlot.CHEST, EMPTY_ARMOR_SLOT_CHESTPLATE);
        ARMOR_SLOT_TEXTURES.put(EquipmentSlot.LEGS, EMPTY_ARMOR_SLOT_LEGGINGS);
        ARMOR_SLOT_TEXTURES.put(EquipmentSlot.FEET, EMPTY_ARMOR_SLOT_BOOTS);
    }

    public ArmorSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, EquipmentSlot slotType, Player player) {
        super(itemHandler, index, xPosition, yPosition);
        this.slotType = slotType;
        this.player = player;
        this.setBackground(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES.get(this.slotType));
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.canEquip(slotType, player);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(playerIn);
    }

}