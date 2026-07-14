package net.zic.zenithlib.creative.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.creative.api.CreativeTabSection;
import net.zic.zenithlib.creative.api.CreativeTabSections;
import net.zic.zenithlib.mixin.creative.AbstractContainerScreenAccessor;
import net.zic.zenithlib.mixin.creative.CreativeModeInventoryScreenAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Client-side rendering, selection, and filtering for creative-tab sections. */
@EventBusSubscriber(modid = ZenithLib.MOD_ID, value = Dist.CLIENT)
public final class CreativeSectionClientEvents {
    private static final Component ALL_ITEMS = Component.translatable("creative_section.zenithlib.all");

    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_GAP = 1;
    private static final int MAX_VISIBLE_BUTTONS = 6;
    private static final int RAIL_TOP_PADDING = 5;

    private static final int OUTLINE = 0xFF373737;
    private static final int LIGHT_EDGE = 0xFFFFFFFF;
    private static final int DARK_EDGE = 0xFF555555;
    private static final int FACE = 0xFFC6C6C6;
    private static final int HOVERED_FACE = 0xFFD8D8D8;
    private static final int SELECTED_FACE = 0xFFE4E4E4;
    private static final int TOOLTIP_BACKGROUND = 0xF0100010;
    private static final int TOOLTIP_BORDER = 0xFF505000;

    private static final Map<ResourceKey<CreativeModeTab>, Identifier> SELECTED_SECTIONS = new HashMap<>();

    private static CreativeModeInventoryScreen activeScreen;
    private static CreativeModeTab activeTab;
    private static ResourceKey<CreativeModeTab> activeTabKey;
    private static List<CreativeTabSection> activeSections = List.of();
    private static Identifier activeSectionId;
    private static int firstVisibleButton;

    private CreativeSectionClientEvents() {}

    @SubscribeEvent
    public static void initializeScreen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen screen) {
            synchronize(screen, true);
        }
    }

    @SubscribeEvent
    public static void renderSections(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen) || !synchronize(screen)) {
            return;
        }

        renderRail(event.getGuiGraphics(), screen, event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void selectSection(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0
                || !(event.getScreen() instanceof CreativeModeInventoryScreen screen)
                || !synchronize(screen)) {
            return;
        }

        RailLayout layout = railLayout(screen);
        int viewIndex = buttonAt(layout, event.getMouseX(), event.getMouseY());
        if (viewIndex < 0) {
            return;
        }

        CreativeTabSection section = sectionForView(viewIndex);
        activeSectionId = section == null ? null : section.id();
        if (activeSectionId == null) {
            SELECTED_SECTIONS.remove(activeTabKey);
        } else {
            SELECTED_SECTIONS.put(activeTabKey, activeSectionId);
        }

        applySelection(screen);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void scrollSections(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen) || !synchronize(screen)) {
            return;
        }

        int totalButtons = totalButtons();
        if (totalButtons <= MAX_VISIBLE_BUTTONS) {
            return;
        }

        RailLayout layout = railLayout(screen);
        if (!layout.contains(event.getMouseX(), event.getMouseY())) {
            return;
        }

        if (event.getScrollDeltaY() > 0.0) {
            firstVisibleButton--;
        } else if (event.getScrollDeltaY() < 0.0) {
            firstVisibleButton++;
        }
        clampFirstVisible();

        if (event.getScrollDeltaY() != 0.0) {
            event.setCanceled(true);
        }
    }

    private static boolean synchronize(CreativeModeInventoryScreen screen) {
        return synchronize(screen, false);
    }

    private static boolean synchronize(CreativeModeInventoryScreen screen, boolean forceApply) {
        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.zenithlib$getSelectedTab();
        if (selectedTab == null || selectedTab.hasSearchBar()) {
            clearActive(screen);
            return false;
        }

        Optional<ResourceKey<CreativeModeTab>> tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(selectedTab);
        if (tabKey.isEmpty()) {
            clearActive(screen);
            return false;
        }

        List<CreativeTabSection> sections = CreativeTabSections.get(tabKey.get());
        if (sections.isEmpty()) {
            restoreUnfilteredIfNeeded(screen, selectedTab);
            clearActive(screen);
            return false;
        }

        Identifier selectedId = SELECTED_SECTIONS.get(tabKey.get());
        if (selectedId != null && !containsSection(sections, selectedId)) {
            SELECTED_SECTIONS.remove(tabKey.get());
            selectedId = null;
        }

        boolean changed = forceApply
                || activeScreen != screen
                || activeTab != selectedTab
                || !Objects.equals(activeTabKey, tabKey.get())
                || !sameSections(activeSections, sections)
                || !Objects.equals(activeSectionId, selectedId);
        if (!changed) {
            return true;
        }

        activeScreen = screen;
        activeTab = selectedTab;
        activeTabKey = tabKey.get();
        activeSections = sections;
        activeSectionId = selectedId;
        firstVisibleButton = 0;
        keepSelectionVisible();
        applySelection(screen);
        return true;
    }

    private static void restoreUnfilteredIfNeeded(
            CreativeModeInventoryScreen screen,
            CreativeModeTab selectedTab
    ) {
        if (activeScreen != screen || activeTab != selectedTab || activeSectionId == null) {
            return;
        }

        CreativeModeInventoryScreenAccessor accessor = (CreativeModeInventoryScreenAccessor) screen;
        accessor.zenithlib$setScrollOffset(0.0F);
        accessor.zenithlib$refreshCurrentTabContents(selectedTab.getDisplayItems());
    }

    private static void clearActive(CreativeModeInventoryScreen screen) {
        if (activeScreen != screen || activeTab != null || !activeSections.isEmpty()) {
            activeScreen = screen;
            activeTab = null;
            activeTabKey = null;
            activeSections = List.of();
            activeSectionId = null;
            firstVisibleButton = 0;
        }
    }

    private static boolean containsSection(List<CreativeTabSection> sections, Identifier id) {
        for (CreativeTabSection section : sections) {
            if (section.id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameSections(List<CreativeTabSection> first, List<CreativeTabSection> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).id().equals(second.get(i).id())) {
                return false;
            }
        }
        return true;
    }

    private static void applySelection(CreativeModeInventoryScreen screen) {
        if (activeTab == null) {
            return;
        }

        Collection<ItemStack> source = activeTab.getDisplayItems();
        Collection<ItemStack> displayItems = source;
        CreativeTabSection selected = selectedSection();
        if (selected != null) {
            List<ItemStack> filtered = new ArrayList<>();
            for (ItemStack stack : source) {
                if (selected.matches(stack)) {
                    filtered.add(stack);
                }
            }
            displayItems = filtered;
        }

        CreativeModeInventoryScreenAccessor accessor = (CreativeModeInventoryScreenAccessor) screen;
        accessor.zenithlib$setScrollOffset(0.0F);
        accessor.zenithlib$refreshCurrentTabContents(displayItems);
    }

    private static void renderRail(
            GuiGraphicsExtractor graphics,
            CreativeModeInventoryScreen screen,
            double mouseX,
            double mouseY
    ) {
        RailLayout layout = railLayout(screen);
        int visibleButtons = Math.min(MAX_VISIBLE_BUTTONS, totalButtons() - firstVisibleButton);
        int hoveredView = -1;

        for (int visibleIndex = 0; visibleIndex < visibleButtons; visibleIndex++) {
            int viewIndex = firstVisibleButton + visibleIndex;
            int x = layout.x();
            int y = layout.y() + visibleIndex * (BUTTON_SIZE + BUTTON_GAP);
            boolean hovered = contains(x, y, BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY);
            boolean selected = isSelectedView(viewIndex);

            renderButton(graphics, x, y, layout.rightSide(), hovered, selected);
            ItemStack icon = iconForView(viewIndex);
            if (!icon.isEmpty()) {
                graphics.item(icon, x + 2, y + 2);
            }

            if (hovered) {
                hoveredView = viewIndex;
            }
        }

        renderOverflowIndicators(graphics, layout);
        if (hoveredView >= 0) {
            renderLabel(graphics, screen, titleForView(hoveredView), mouseX, mouseY);
        }
    }

    private static void renderButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            boolean rightSide,
            boolean hovered,
            boolean selected
    ) {
        int face = selected ? SELECTED_FACE : hovered ? HOVERED_FACE : FACE;

        graphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, OUTLINE);
        graphics.fill(x + 1, y + 1, x + BUTTON_SIZE - 1, y + BUTTON_SIZE - 1, face);
        graphics.fill(x + 1, y + 1, x + BUTTON_SIZE - 1, y + 2, LIGHT_EDGE);
        graphics.fill(x + 1, y + 1, x + 2, y + BUTTON_SIZE - 1, LIGHT_EDGE);
        graphics.fill(x + 1, y + BUTTON_SIZE - 2, x + BUTTON_SIZE - 1, y + BUTTON_SIZE - 1, DARK_EDGE);
        graphics.fill(x + BUTTON_SIZE - 2, y + 1, x + BUTTON_SIZE - 1, y + BUTTON_SIZE - 1, DARK_EDGE);

        if (!selected) {
            return;
        }

        if (rightSide) {
            graphics.fill(x - 1, y + 1, x + 2, y + BUTTON_SIZE - 1, face);
            graphics.fill(x - 1, y + 1, x, y + BUTTON_SIZE - 1, LIGHT_EDGE);
        } else {
            graphics.fill(x + BUTTON_SIZE - 2, y + 1, x + BUTTON_SIZE + 1, y + BUTTON_SIZE - 1, face);
            graphics.fill(x + BUTTON_SIZE, y + 1, x + BUTTON_SIZE + 1, y + BUTTON_SIZE - 1, DARK_EDGE);
        }
    }

    private static void renderOverflowIndicators(GuiGraphicsExtractor graphics, RailLayout layout) {
        int centerX = layout.x() + BUTTON_SIZE / 2;
        if (firstVisibleButton > 0) {
            int y = layout.y() - 4;
            graphics.fill(centerX, y, centerX + 1, y + 1, 0xFFFFFFFF);
            graphics.fill(centerX - 1, y + 1, centerX + 2, y + 2, 0xFFFFFFFF);
        }

        if (firstVisibleButton + MAX_VISIBLE_BUTTONS < totalButtons()) {
            int y = layout.y() + MAX_VISIBLE_BUTTONS * (BUTTON_SIZE + BUTTON_GAP) - BUTTON_GAP + 2;
            graphics.fill(centerX - 1, y, centerX + 2, y + 1, 0xFFFFFFFF);
            graphics.fill(centerX, y + 1, centerX + 1, y + 2, 0xFFFFFFFF);
        }
    }

    private static void renderLabel(
            GuiGraphicsExtractor graphics,
            CreativeModeInventoryScreen screen,
            Component title,
            double mouseX,
            double mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        FormattedCharSequence text = title.getVisualOrderText();
        int width = font.width(text);
        int x = Math.min((int) mouseX + 12, screen.width - width - 8);
        int y = Math.min((int) mouseY - 12, screen.height - font.lineHeight - 8);
        x = Math.max(4, x);
        y = Math.max(4, y);

        graphics.fill(x - 4, y - 4, x + width + 4, y + font.lineHeight + 4, TOOLTIP_BACKGROUND);
        graphics.fill(x - 4, y - 4, x + width + 4, y - 3, TOOLTIP_BORDER);
        graphics.fill(x - 4, y + font.lineHeight + 3, x + width + 4, y + font.lineHeight + 4, TOOLTIP_BORDER);
        graphics.fill(x - 4, y - 4, x - 3, y + font.lineHeight + 4, TOOLTIP_BORDER);
        graphics.fill(x + width + 3, y - 4, x + width + 4, y + font.lineHeight + 4, TOOLTIP_BORDER);
        graphics.text(font, text, x, y, 0xFFFFFFFF, false);
    }

    private static RailLayout railLayout(CreativeModeInventoryScreen screen) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int panelLeft = accessor.zenithlib$getLeftPos();
        int panelTop = accessor.zenithlib$getTopPos();
        int panelWidth = accessor.zenithlib$getImageWidth();
        boolean rightSide = panelLeft < BUTTON_SIZE + 2
                && screen.width - (panelLeft + panelWidth) >= BUTTON_SIZE + 2;
        int x = rightSide ? panelLeft + panelWidth - 1 : panelLeft - BUTTON_SIZE + 1;
        x = Math.max(0, Math.min(x, screen.width - BUTTON_SIZE));
        return new RailLayout(x, panelTop + RAIL_TOP_PADDING, rightSide);
    }

    private static int buttonAt(RailLayout layout, double mouseX, double mouseY) {
        int visibleButtons = Math.min(MAX_VISIBLE_BUTTONS, totalButtons() - firstVisibleButton);
        for (int visibleIndex = 0; visibleIndex < visibleButtons; visibleIndex++) {
            int y = layout.y() + visibleIndex * (BUTTON_SIZE + BUTTON_GAP);
            if (contains(layout.x(), y, BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY)) {
                return firstVisibleButton + visibleIndex;
            }
        }
        return -1;
    }

    private static boolean contains(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int totalButtons() {
        return activeSections.size() + 1;
    }

    private static CreativeTabSection sectionForView(int viewIndex) {
        return viewIndex == 0 ? null : activeSections.get(viewIndex - 1);
    }

    private static CreativeTabSection selectedSection() {
        if (activeSectionId == null) {
            return null;
        }
        for (CreativeTabSection section : activeSections) {
            if (section.id().equals(activeSectionId)) {
                return section;
            }
        }
        return null;
    }

    private static ItemStack iconForView(int viewIndex) {
        CreativeTabSection section = sectionForView(viewIndex);
        if (section != null) {
            return section.icon();
        }
        return activeTab == null ? ItemStack.EMPTY : activeTab.getIconItem().copy();
    }

    private static Component titleForView(int viewIndex) {
        CreativeTabSection section = sectionForView(viewIndex);
        return section == null ? ALL_ITEMS : section.title();
    }

    private static boolean isSelectedView(int viewIndex) {
        CreativeTabSection section = sectionForView(viewIndex);
        return section == null ? activeSectionId == null : section.id().equals(activeSectionId);
    }

    private static void keepSelectionVisible() {
        int selectedView = 0;
        if (activeSectionId != null) {
            for (int i = 0; i < activeSections.size(); i++) {
                if (activeSections.get(i).id().equals(activeSectionId)) {
                    selectedView = i + 1;
                    break;
                }
            }
        }

        if (selectedView < firstVisibleButton) {
            firstVisibleButton = selectedView;
        } else if (selectedView >= firstVisibleButton + MAX_VISIBLE_BUTTONS) {
            firstVisibleButton = selectedView - MAX_VISIBLE_BUTTONS + 1;
        }
        clampFirstVisible();
    }

    private static void clampFirstVisible() {
        firstVisibleButton = Math.max(0, Math.min(firstVisibleButton, Math.max(0, totalButtons() - MAX_VISIBLE_BUTTONS)));
    }

    private record RailLayout(int x, int y, boolean rightSide) {
        private boolean contains(double mouseX, double mouseY) {
            int visibleButtons = Math.min(MAX_VISIBLE_BUTTONS, totalButtons() - firstVisibleButton);
            int height = visibleButtons * (BUTTON_SIZE + BUTTON_GAP) - BUTTON_GAP;
            return CreativeSectionClientEvents.contains(x, y, BUTTON_SIZE, height, mouseX, mouseY);
        }
    }
}
