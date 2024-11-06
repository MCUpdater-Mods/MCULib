package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.helpers.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class WidgetFluid extends AbstractWidget {
    private final Minecraft minecraft;
    private final int tankIndex;
    private IFluidHandler fluidHandler;
    private int COLOR_BACKGROUND = 0xff8b8b8b;
    private int COLOR_TOPLEFT = 0x7f373737;
    private int COLOR_BOTTOMRIGHT = 0x7fffffff;

    public WidgetFluid(int x, int y, int width, int height, IFluidHandler handler, int tankIndex) {
        super(x, y, width, height, Component.empty());
        this.minecraft = Minecraft.getInstance();
        this.fluidHandler = handler;
        this.tankIndex = tankIndex;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //draw Box
        guiGraphics.fillGradient(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, COLOR_BACKGROUND, COLOR_BACKGROUND); // interior
        guiGraphics.hLine(getX(), getX()+width-1, getY(), COLOR_TOPLEFT); // top
        guiGraphics.vLine(getX(), getY(), getY() + height-1, COLOR_TOPLEFT); // left
        guiGraphics.hLine(getX(), getX()+width-1, getY() + height-1, COLOR_BOTTOMRIGHT); // bottom
        guiGraphics.vLine(getX() + width-1, getY(), getY() + height-1, COLOR_BOTTOMRIGHT); // right

        //fill Gauge
        RenderHelper.renderFluid(guiGraphics, this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2, fluidHandler.getFluidInTank(this.tankIndex), fluidHandler.getTankCapacity(this.tankIndex));

        if (this.isHoveredOrFocused() && this.visible) {
            renderFluidToolTip();
        }
    }

    public void renderFluidToolTip(){
        Component fluid = fluidHandler.getFluidInTank(tankIndex).isEmpty() ? Component.literal("Empty") : Component.translatable(fluidHandler.getFluidInTank(tankIndex).getFluid().getFluidType().getDescriptionId());
        Component volume = Component.literal(fluidHandler.getFluidInTank(tankIndex).getAmount() + " / " + fluidHandler.getTankCapacity(tankIndex) + " mB");
        if (this.minecraft.screen != null) {
            List<FormattedCharSequence> fluidTooltip = Lists.newArrayList();
            fluidTooltip.addAll(this.minecraft.font.split(fluid,64));
            fluidTooltip.addAll(this.minecraft.font.split(volume, 64));
            this.minecraft.screen.setTooltipForNextRenderPass(fluidTooltip);
            //renderComponentTooltip(poseStack, Arrays.asList(fluid, volume), x, y);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }
}
