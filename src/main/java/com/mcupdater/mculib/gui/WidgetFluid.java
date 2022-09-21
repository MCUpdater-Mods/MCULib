package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.helpers.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;

public class WidgetFluid extends AbstractWidget {
    private final Minecraft minecraft;
    private final int tankIndex;
    //private ResourceLocation ENERGY = new ResourceLocation(MCULib.MODID, "textures/gui/energy.png");
    private IFluidHandler fluidHandler;
    private int COLOR_BACKGROUND = 0xff8b8b8b;
    private int COLOR_TOPLEFT = 0x7f373737;
    private int COLOR_BOTTOMRIGHT = 0x7fffffff;

    public WidgetFluid(int x, int y, int width, int height, IFluidHandler handler, int tankIndex) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.minecraft = Minecraft.getInstance();
        this.fluidHandler = handler;
        this.tankIndex = tankIndex;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        //draw Box
        this.fillGradient(poseStack,x + 1, y + 1, x + width - 1, y + height - 1, COLOR_BACKGROUND, COLOR_BACKGROUND); // interior
        this.hLine(poseStack, x, x+width-1, y, COLOR_TOPLEFT); // top
        this.vLine(poseStack, x, y, y + height-1, COLOR_TOPLEFT); // left
        this.hLine(poseStack, x, x+width-1, y + height-1, COLOR_BOTTOMRIGHT); // bottom
        this.vLine(poseStack, x + width-1, y, y + height-1, COLOR_BOTTOMRIGHT); // right

        //fill Gauge
        RenderHelper.renderFluid(poseStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, fluidHandler.getFluidInTank(this.tankIndex), fluidHandler.getTankCapacity(this.tankIndex));

        if (this.isHoveredOrFocused() && this.visible) {
            renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack poseStack, int x, int y){
        super.renderToolTip(poseStack, x, y);
        Component fluid = fluidHandler.getFluidInTank(tankIndex).isEmpty() ? new TextComponent("Empty") : new TranslatableComponent(fluidHandler.getFluidInTank(tankIndex).getFluid().getAttributes().getTranslationKey());
        Component volume = new TextComponent(fluidHandler.getFluidInTank(tankIndex).getAmount() + " / " + fluidHandler.getTankCapacity(tankIndex) + " mB");
        if (this.minecraft.screen != null) {
            this.minecraft.screen.renderComponentTooltip(poseStack, Arrays.asList(fluid, volume), x, y);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
