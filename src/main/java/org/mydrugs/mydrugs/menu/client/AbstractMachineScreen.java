package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.client.util.AbstractMachineDrawMethods;
import org.mydrugs.mydrugs.menu.client.util.MachineStatusRenderer;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;
import org.mydrugs.mydrugs.network.CycleMachineTransferSidePayload;
import org.mydrugs.mydrugs.network.MachineTransferConfigSnapshotPayload;
import org.mydrugs.mydrugs.network.OpenMachineTransferConfigPayload;
import org.mydrugs.mydrugs.network.RequestMachineTransferOverlayPayload;
import org.mydrugs.mydrugs.pipe.machine.MachineLocalSide;
import org.mydrugs.mydrugs.pipe.machine.MachineOrientation;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferSideRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all custom machine screens.
 * <p>
 * All primitive machine drawing now comes from {@link AbstractMachineDrawMethods}.
 * JEI recipe categories use the same abstraction, so changing shared window,
 * panel, slot, tank, bar, button, arrow, color, or fluid/gas drawing in that
 * abstraction automatically updates both real screens and recipe GUIs.
 */
public abstract class AbstractMachineScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T>
        implements AbstractMachineDrawMethods {
    protected static final int SIEVE_INV_PANEL_FILL = 0xFF2C2C2C;
    protected static final int SIEVE_INV_PANEL_LIGHT_BORDER = 0xFF595959;
    protected static final int SIEVE_INV_PANEL_DARK_BORDER = 0xFF101010;
    private static final int TRANSFER_PANEL_W = 226;
    private static final int TRANSFER_SIDE_SIZE = 38;
    private static final int TRANSFER_PORT_W = 70;
    private static final int TRANSFER_PORT_H = 24;
    private static final int TRANSFER_PORT_GAP = 5;
    private static final int TRANSFER_PANEL_MIN_W = 226;
    private static final int TRANSFER_PANEL_MIN_H = 190;
    private static final int TRANSFER_PANEL_HEADER_H = 24;
    private static final int TRANSFER_PANEL_RESIZE_SIZE = 10;
    private static final TransferSideButton[] TRANSFER_SIDE_BUTTONS = {
            new TransferSideButton(MachineLocalSide.TOP, "T", 138, 34),
            new TransferSideButton(MachineLocalSide.LEFT, "L", 96, 76),
            new TransferSideButton(MachineLocalSide.FRONT, "F", 138, 76),
            new TransferSideButton(MachineLocalSide.RIGHT, "R", 180, 76),
            new TransferSideButton(MachineLocalSide.BACK, "Ba", 117, 118),
            new TransferSideButton(MachineLocalSide.BOTTOM, "Bo", 159, 118)
    };
    private boolean transferOverlayOpen;
    private int transferSelectedPort;
    private List<MachineTransferConfigSnapshotPayload.PortState> transferPorts = List.of();
    private int transferPanelX = Integer.MIN_VALUE;
    private int transferPanelY = Integer.MIN_VALUE;
    private int transferPanelW = TRANSFER_PANEL_W;
    private int transferPanelH = TRANSFER_PANEL_MIN_H;
    private boolean draggingTransferPanel;
    private boolean resizingTransferPanel;
    private int transferPanelDragOffsetX;
    private int transferPanelDragOffsetY;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    protected void init() {
        super.init();
        addTransferConfigButton();
    }

    protected void addTransferConfigButton() {
        if (!shouldShowTransferConfigButton()) {
            return;
        }

        Button button = Button.builder(
                        Component.translatable("screen.mydrugs.machine_transfer.open_short"),
                        ignored -> ClientPacketDistributor.sendToServer(new OpenMachineTransferConfigPayload(this.menu.containerId))
                )
                .bounds(transferConfigButtonX(), transferConfigButtonY(), 18, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.machine_transfer.tooltip")))
                .build();
        this.addRenderableWidget(button);
    }

    protected boolean shouldShowTransferConfigButton() {
        return true;
    }

    protected int transferConfigButtonX() {
        return Math.max(4, this.leftPos - 22);
    }

    protected int transferConfigButtonY() {
        return this.topPos;
    }

    @Override
    public int drawOriginX() {
        return this.leftPos;
    }

    @Override
    public int drawOriginY() {
        return this.topPos;
    }

    @Override
    public int labelX(int localX) {
        return localX;
    }

    @Override
    public int labelY(int localY) {
        return localY;
    }

    @Override
    public int drawWidth() {
        return this.imageWidth;
    }

    @Override
    public int drawHeight() {
        return this.imageHeight;
    }

    protected void drawSieveInventoryPanels(GuiGraphics graphics, int inventoryPanelX, int inventoryPanelY) {
        drawPanel(
                graphics,
                inventoryPanelX,
                inventoryPanelY,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                SIEVE_INV_PANEL_FILL,
                SIEVE_INV_PANEL_LIGHT_BORDER,
                SIEVE_INV_PANEL_DARK_BORDER
        );

        drawPanel(
                graphics,
                inventoryPanelX,
                StandardInventoryLayout.hotbarPanelY(inventoryPanelY),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                SIEVE_INV_PANEL_FILL,
                SIEVE_INV_PANEL_LIGHT_BORDER,
                SIEVE_INV_PANEL_DARK_BORDER
        );
    }

    protected int standardInventoryLabelY(int inventoryPanelY) {
        return StandardInventoryLayout.inventoryLabelY(inventoryPanelY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderSharedEnergyBar(graphics);
        renderSharedMachineStatus(graphics);
        renderTransferPortHighlight(graphics);
        renderTransferOverlay(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderSharedEnergyTooltip(graphics, mouseX, mouseY);
        this.renderExtraTooltips(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0 && this.transferOverlayOpen) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            if (isOverTransferResizeHandle(mouseX, mouseY)) {
                this.resizingTransferPanel = true;
                return true;
            }
            if (isOverTransferHeader(mouseX, mouseY)) {
                this.draggingTransferPanel = true;
                this.transferPanelDragOffsetX = mouseX - transferPanelX();
                this.transferPanelDragOffsetY = mouseY - transferPanelY();
                return true;
            }
            if (handleTransferOverlayClick(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingTransferPanel && event.button() == 0) {
            this.transferPanelX = (int) event.x() - this.transferPanelDragOffsetX;
            this.transferPanelY = (int) event.y() - this.transferPanelDragOffsetY;
            clampTransferPanelToScreen();
            return true;
        }

        if (this.resizingTransferPanel && event.button() == 0) {
            this.transferPanelW = Math.max(TRANSFER_PANEL_MIN_W, (int) event.x() - transferPanelX());
            this.transferPanelH = Math.max(TRANSFER_PANEL_MIN_H, (int) event.y() - transferPanelY());
            clampTransferPanelToScreen();
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && (this.draggingTransferPanel || this.resizingTransferPanel)) {
            this.draggingTransferPanel = false;
            this.resizingTransferPanel = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    /**
     * Child classes override this for custom non-item tooltips.
     * Keeps render() identical across screens.
     */
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    protected boolean shouldRenderSharedEnergyBar() {
        return true;
    }

    private void renderSharedEnergyBar(GuiGraphics graphics) {
        if (shouldRenderSharedEnergyBar()
                && this.menu instanceof org.mydrugs.mydrugs.menu.AbstractMachineMenu machineMenu
                && machineMenu.hasSyncedEnergyStorage()) {
            drawExternalEnergyBar(graphics, machineMenu.syncedEnergyStored(), machineMenu.syncedEnergyCapacity());
        }
    }

    private void renderSharedEnergyTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (shouldRenderSharedEnergyBar()
                && this.menu instanceof org.mydrugs.mydrugs.menu.AbstractMachineMenu machineMenu
                && machineMenu.hasSyncedEnergyStorage()) {
            renderExternalEnergyTooltip(graphics, mouseX, mouseY, machineMenu.syncedEnergyStored(), machineMenu.syncedEnergyCapacity());
        }
    }

    private void renderSharedMachineStatus(GuiGraphics graphics) {
        if (this.menu instanceof org.mydrugs.mydrugs.menu.AbstractMachineMenu machineMenu) {
            MachineStatusRenderer.render(graphics, this.font, this.leftPos + 8, this.topPos + 18, Math.max(40, this.imageWidth - 16), machineMenu.syncedMachineStatus());
        }
    }

    protected void pressMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    public void applyMachineTransferSnapshot(MachineTransferConfigSnapshotPayload payload) {
        if (payload.menuId() != this.menu.containerId) {
            return;
        }

        this.transferPorts = payload.ports();
        this.lastTransferFront = payload.frontDirection();
        if (this.transferSelectedPort >= this.transferPorts.size()) {
            this.transferSelectedPort = 0;
        }
    }

    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        List<TransferHighlight> highlights = new ArrayList<>();
        int machineBottom = Math.max(0, this.imageHeight - 96);
        for (Slot slot : this.menu.slots) {
            if (slot.y < machineBottom) {
                highlights.add(new TransferHighlight(slot.x, slot.y, 16, 16));
            }
        }
        return highlights;
    }

    private void toggleTransferOverlay() {
        this.transferOverlayOpen = !this.transferOverlayOpen;
        if (this.transferOverlayOpen) {
            ensureTransferPanelInitialized();
            ClientPacketDistributor.sendToServer(new RequestMachineTransferOverlayPayload(this.menu.containerId));
        }
    }

    private void renderTransferPortHighlight(GuiGraphics graphics) {
        if (!this.transferOverlayOpen || this.transferPorts.isEmpty()) {
            return;
        }

        String portIdPath = this.transferPorts.get(this.transferSelectedPort).idPath();
        for (TransferHighlight highlight : transferPortHighlights(portIdPath)) {
            int x = this.leftPos + highlight.x();
            int y = this.topPos + highlight.y();
            graphics.fill(x - 2, y - 2, x + highlight.width() + 2, y + highlight.height() + 2, 0x80FFE35A);
            graphics.fill(x - 1, y - 1, x + highlight.width() + 1, y, 0xFFFFF3A0);
            graphics.fill(x - 1, y + highlight.height(), x + highlight.width() + 1, y + highlight.height() + 1, 0xFFFFF3A0);
            graphics.fill(x - 1, y, x, y + highlight.height(), 0xFFFFF3A0);
            graphics.fill(x + highlight.width(), y, x + highlight.width() + 1, y + highlight.height(), 0xFFFFF3A0);
        }
    }

    private void renderTransferOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.transferOverlayOpen) {
            return;
        }

        int x = transferPanelX();
        int y = transferPanelY();
        int h = transferPanelHeight();
        graphics.fill(x, y, x + this.transferPanelW, y + h, 0xF0181818);
        graphics.fill(x + 4, y + 4, x + this.transferPanelW - 4, y + h - 4, 0xF022262A);
        graphics.fill(x + 4, y + 4, x + this.transferPanelW - 4, y + TRANSFER_PANEL_HEADER_H, 0xFF303942);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.machine_transfer.title"), x + 8, y + 10, 0xFFFFFFFF, false);

        renderTransferPorts(graphics, x, y);
        renderTransferSides(graphics, x, y);
        renderTransferResizeHandle(graphics, x, y);
    }

    private void renderTransferPorts(GuiGraphics graphics, int panelX, int panelY) {
        int y = panelY + 30;
        for (int i = 0; i < this.transferPorts.size(); i++) {
            int border = i == this.transferSelectedPort ? 0xFFE8F1E8 : 0xFF101214;
            int fill = i == this.transferSelectedPort ? 0xFF3B4954 : 0xFF252A30;
            graphics.fill(panelX + 8, y, panelX + 8 + TRANSFER_PORT_W, y + TRANSFER_PORT_H, border);
            graphics.fill(panelX + 10, y + 2, panelX + 6 + TRANSFER_PORT_W, y + TRANSFER_PORT_H - 2, fill);
            graphics.drawCenteredString(this.font, transferPortLabel(i), panelX + 8 + TRANSFER_PORT_W / 2, y + 8, 0xFFFFFFFF);
            y += TRANSFER_PORT_H + TRANSFER_PORT_GAP;
        }
    }

    private void renderTransferSides(GuiGraphics graphics, int panelX, int panelY) {
        graphics.fill(panelX + 86, panelY + 26, panelX + this.transferPanelW - 8, panelY + transferPanelHeight() - 8, 0xFF101214);
        graphics.fill(panelX + 88, panelY + 28, panelX + this.transferPanelW - 10, panelY + transferPanelHeight() - 10, 0xFF242A30);
        for (TransferSideButton sideButton : TRANSFER_SIDE_BUTTONS) {
            MachineTransferSideRule rule = selectedRule(sideButton.side());
            int x = panelX + sideButton.x();
            int y = panelY + sideButton.y();
            graphics.fill(x, y, x + TRANSFER_SIDE_SIZE, y + TRANSFER_SIDE_SIZE, 0xFF16191D);
            graphics.fill(x + 1, y + 1, x + TRANSFER_SIDE_SIZE - 1, y + TRANSFER_SIDE_SIZE - 1, transferRuleColor(rule));
            graphics.drawCenteredString(this.font, Component.literal(sideButton.label()), x + TRANSFER_SIDE_SIZE / 2, y + 10, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, transferRuleLabel(rule), x + TRANSFER_SIDE_SIZE / 2, y + 23, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, Component.literal(MachineOrientation.toWorld(this.transferPorts.isEmpty()
                    ? net.minecraft.core.Direction.NORTH
                    : getTransferFrontDirection(), sideButton.side()).getSerializedName().substring(0, 1).toUpperCase()), x + TRANSFER_SIDE_SIZE / 2, y + 2, 0xFFD6DEE8);
        }
    }

    private void renderTransferResizeHandle(GuiGraphics graphics, int panelX, int panelY) {
        int right = panelX + this.transferPanelW - 4;
        int bottom = panelY + transferPanelHeight() - 4;
        graphics.fill(right - TRANSFER_PANEL_RESIZE_SIZE, bottom - 1, right, bottom, 0xFFB8C7D8);
        graphics.fill(right - 1, bottom - TRANSFER_PANEL_RESIZE_SIZE, right, bottom, 0xFFB8C7D8);
        graphics.fill(right - 6, bottom - 4, right, bottom - 3, 0xFF7F8D99);
        graphics.fill(right - 4, bottom - 6, right - 3, bottom, 0xFF7F8D99);
    }

    private boolean handleTransferOverlayClick(int mouseX, int mouseY) {
        int panelX = transferPanelX();
        int panelY = transferPanelY();
        if (mouseX < panelX || mouseX >= panelX + this.transferPanelW || mouseY < panelY || mouseY >= panelY + transferPanelHeight()) {
            return false;
        }

        int portY = panelY + 30;
        for (int i = 0; i < this.transferPorts.size(); i++) {
            if (mouseX >= panelX + 8 && mouseX < panelX + 8 + TRANSFER_PORT_W && mouseY >= portY && mouseY < portY + TRANSFER_PORT_H) {
                this.transferSelectedPort = i;
                return true;
            }
            portY += TRANSFER_PORT_H + TRANSFER_PORT_GAP;
        }

        if (!this.transferPorts.isEmpty()) {
            for (TransferSideButton sideButton : TRANSFER_SIDE_BUTTONS) {
                int x = panelX + sideButton.x();
                int y = panelY + sideButton.y();
                if (mouseX >= x && mouseX < x + TRANSFER_SIDE_SIZE && mouseY >= y && mouseY < y + TRANSFER_SIDE_SIZE) {
                    ClientPacketDistributor.sendToServer(new CycleMachineTransferSidePayload(
                            this.menu.containerId,
                            this.transferSelectedPort,
                            sideButton.side().getSerializedName()
                    ));
                    return true;
                }
            }
        }

        return true;
    }

    private int transferPanelX() {
        ensureTransferPanelInitialized();
        return this.transferPanelX;
    }

    private int transferPanelY() {
        ensureTransferPanelInitialized();
        return this.transferPanelY;
    }

    private int transferPanelHeight() {
        return Math.max(this.transferPanelH, TRANSFER_PANEL_MIN_H);
    }

    private boolean isOverTransferHeader(int mouseX, int mouseY) {
        int x = transferPanelX();
        int y = transferPanelY();
        return mouseX >= x && mouseX < x + this.transferPanelW && mouseY >= y && mouseY < y + TRANSFER_PANEL_HEADER_H;
    }

    private boolean isOverTransferResizeHandle(int mouseX, int mouseY) {
        int x = transferPanelX() + this.transferPanelW - TRANSFER_PANEL_RESIZE_SIZE;
        int y = transferPanelY() + transferPanelHeight() - TRANSFER_PANEL_RESIZE_SIZE;
        return mouseX >= x && mouseX < x + TRANSFER_PANEL_RESIZE_SIZE && mouseY >= y && mouseY < y + TRANSFER_PANEL_RESIZE_SIZE;
    }

    private void ensureTransferPanelInitialized() {
        if (this.transferPanelX != Integer.MIN_VALUE && this.transferPanelY != Integer.MIN_VALUE) {
            return;
        }

        this.transferPanelW = TRANSFER_PANEL_W;
        this.transferPanelH = Math.max(this.imageHeight, TRANSFER_PANEL_MIN_H);
        this.transferPanelX = Math.max(4, this.leftPos - this.transferPanelW - 8);
        this.transferPanelY = this.topPos;
        clampTransferPanelToScreen();
    }

    private void clampTransferPanelToScreen() {
        int screenW = this.width;
        int screenH = this.height;
        this.transferPanelW = Math.max(TRANSFER_PANEL_MIN_W, Math.min(this.transferPanelW, Math.max(TRANSFER_PANEL_MIN_W, screenW - 8)));
        this.transferPanelH = Math.max(TRANSFER_PANEL_MIN_H, Math.min(this.transferPanelH, Math.max(TRANSFER_PANEL_MIN_H, screenH - 8)));
        this.transferPanelX = Math.max(4, Math.min(this.transferPanelX, screenW - this.transferPanelW - 4));
        this.transferPanelY = Math.max(4, Math.min(this.transferPanelY, screenH - this.transferPanelH - 4));
    }

    private net.minecraft.core.Direction getTransferFrontDirection() {
        return this.transferPorts.isEmpty() ? net.minecraft.core.Direction.NORTH : this.lastTransferFront;
    }

    private net.minecraft.core.Direction lastTransferFront = net.minecraft.core.Direction.NORTH;

    private MachineTransferSideRule selectedRule(MachineLocalSide side) {
        if (this.transferPorts.isEmpty()) {
            return MachineTransferSideRule.DISABLED;
        }

        int[] rules = this.transferPorts.get(this.transferSelectedPort).rules();
        if (side.networkId() < 0 || side.networkId() >= rules.length) {
            return MachineTransferSideRule.DISABLED;
        }

        return MachineTransferSideRule.byNetworkId(rules[side.networkId()]);
    }

    private Component transferPortLabel(int portIndex) {
        String path = this.transferPorts.get(portIndex).idPath();
        if (path.contains("output")) {
            return Component.translatable("screen.mydrugs.machine_transfer.output_n", transferPortNumber(portIndex, "output"));
        }
        if (path.contains("input")) {
            return Component.translatable("screen.mydrugs.machine_transfer.input_n", transferPortNumber(portIndex, "input"));
        }
        return Component.translatable(this.transferPorts.get(portIndex).translationKey());
    }

    private int transferPortNumber(int portIndex, String token) {
        int number = 0;
        for (int i = 0; i <= portIndex; i++) {
            if (this.transferPorts.get(i).idPath().contains(token)) {
                number++;
            }
        }
        return number;
    }

    private static Component transferRuleLabel(MachineTransferSideRule rule) {
        return Component.translatable("screen.mydrugs.machine_transfer.toggle." + (rule == MachineTransferSideRule.DISABLED ? "off" : "on"));
    }

    private static int transferRuleColor(MachineTransferSideRule rule) {
        return switch (rule) {
            case DISABLED -> 0xFF3A4047;
            case INPUT -> 0xFF1E7EEA;
            case OUTPUT -> 0xFFE07A1F;
        };
    }

    /**
     * Generic textured blit helper.
     * Useful for texture-based screens.
     */
    protected void blitTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int localX,
            int localY,
            int u,
            int v,
            int width,
            int height,
            int texW,
            int texH
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                guiX(localX),
                guiY(localY),
                u,
                v,
                width,
                height,
                texW,
                texH
        );
    }

    protected void renderTooltipLines(GuiGraphics graphics, int mouseX, int mouseY, Component... lines) {
        List<ClientTooltipComponent> components = new ArrayList<>(lines.length);
        for (Component line : lines) {
            components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }

        graphics.renderTooltip(
                this.font,
                components,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    protected void renderFluidTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            String title,
            Fluid fluid,
            int amount,
            int capacity
    ) {
        renderTooltipLines(
                graphics,
                mouseX,
                mouseY,
                Component.literal(title),
                Component.literal(getFluidName(fluid)),
                Component.literal(amount + " / " + capacity + " mB")
        );
    }

    protected void renderSimpleAmountTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            String title,
            int amount,
            int capacity,
            String unit
    ) {
        renderTooltipLines(
                graphics,
                mouseX,
                mouseY,
                Component.literal(title),
                Component.literal(amount + " / " + capacity + " " + unit)
        );
    }

    protected void drawExternalEnergyBar(GuiGraphics graphics, int stored, int capacity) {
        int x = -18;
        int y = 24;
        int w = 10;
        int h = 50;
        int fill = capacity > 0 ? stored * (h - 2) / capacity : 0;
        graphics.fill(guiX(x), guiY(y), guiX(x + w), guiY(y + h), 0xFF101216);
        graphics.fill(guiX(x + 1), guiY(y + h - 1 - fill), guiX(x + w - 1), guiY(y + h - 1), 0xFF9A4DFF);
        graphics.fill(guiX(x), guiY(y), guiX(x + w), guiY(y + 1), 0xFF767C88);
        graphics.fill(guiX(x), guiY(y + h - 1), guiX(x + w), guiY(y + h), 0xFF0E1014);
        graphics.fill(guiX(x), guiY(y), guiX(x + 1), guiY(y + h), 0xFF767C88);
        graphics.fill(guiX(x + w - 1), guiY(y), guiX(x + w), guiY(y + h), 0xFF0E1014);
    }

    protected void renderExternalEnergyTooltip(GuiGraphics graphics, int mouseX, int mouseY, int stored, int capacity) {
        if (isHoveringBox(-18, 24, 10, 50, mouseX, mouseY)) {
            renderSimpleAmountTooltip(graphics, mouseX, mouseY, "Psychotrope Energy", stored, capacity, "PE");
        }
    }

    protected static class InvisibleButton extends Button {
        public InvisibleButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Intentionally empty: only used for click handling.
        }
    }

    protected static TransferHighlight slotHighlight(int x, int y) {
        return new TransferHighlight(x, y, 16, 16);
    }

    protected static TransferHighlight tankHighlight(int x, int y, int width, int height) {
        return new TransferHighlight(x, y, width, height);
    }

    protected record TransferHighlight(int x, int y, int width, int height) {
    }

    private record TransferSideButton(MachineLocalSide side, String label, int x, int y) {
    }
}
