package mcp.mobius.waila.gui.screen;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import mcp.mobius.waila.mixin.TabNavigationBarAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface TabbedScreen {

    Supplier<TabNavigationBar> TABS = () -> bar(
        new Tab<>(WailaConfigScreen.TITLE, WailaConfigScreen.class, WailaConfigScreen::new),
        new Tab<>(PluginToggleScreen.TITLE, PluginToggleScreen.class, PluginToggleScreen::new),
        new Tab<>(PluginConfigScreen.TITLE, PluginConfigScreen.class, PluginConfigScreen::new),
        new Tab<>(CreditsScreen.TITLE, CreditsScreen.class, CreditsScreen::new)
    );

    static TabNavigationBar bar(Tab<?>... tabs) {
        var tabManager = new TabManager(w -> {}, w -> {}, t -> {
            var tab = ((Tab<?>) t);
            var client = Minecraft.getInstance();
            var parent = client.gui.screen();
            if (parent != null && parent.getClass() == tab.clazz) return;
            if (parent instanceof TabbedScreen tabbed) {
                tabbed.changeTab(() -> client.gui.setScreen(tab.ctor.apply(tabbed.getParent())));
            } else {
                client.gui.setScreen(tab.ctor.apply(parent));
            }
        }, t -> {});

        var tabHeight = 24;
        var tabWidth = Math.max(100, 200 / Math.max(1, tabs.length));
        var width = tabs.length * tabWidth;
        var builder = TabNavigationBar.builder(tabManager, 0, 0, width, tabHeight);
        for (var tab : tabs) {
            builder.addTab(new WthitTabButton(tabManager, tab, tabWidth, tabHeight), tab);
        }
        return builder.build();
    }

    @Nullable Screen getParent();

    default void changeTab(Runnable change) {
        change.run();
    }

    default void initBar(int width, Consumer<TabNavigationBar> addRenderableWidget, Consumer<TabButton> setInitialFocus) {
        var clazz = this.getClass();
        var tabs = TABS.get();
        var currentTab = tabs.getTabs().stream()
            .map(it -> (Tab<?>) it)
            .filter(it -> it.clazz == clazz)
            .findFirst().orElse(null);

        tabs.arrangeElements(width);
        addRenderableWidget.accept(tabs);

        if (currentTab != null) {
            tabs.selectTab(tabs.getTabs().indexOf(currentTab), false);
            setInitialFocus.accept(((TabNavigationBarAccess) tabs).wthit_currentTabButton());
        }
    }

    record Tab<T extends Screen & TabbedScreen>(
        Component title,
        Class<T> clazz,
        Function<@Nullable Screen, T> ctor
    ) implements net.minecraft.client.gui.components.tabs.Tab {

        @Override
        public Component getTabTitle() {
            return title;
        }

        @Override
        public Component getTabExtraNarration() {
            return Component.empty();
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
        }

        @Override
        public void doLayout(ScreenRectangle screenRectangle) {
        }

        @Override
        public Layout getLayout() {
            return new GridLayout();
        }

    }

    class WthitTabButton extends TabButton {
        public WthitTabButton(TabManager tabManager, Tab<?> tab, int width, int height) {
            super(tabManager, tab, width, height);
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            int bgColor;
            if (this.isSelected()) {
                bgColor = 0xFFC0C0C0;
            } else if (this.isHoveredOrFocused()) {
                bgColor = 0xFF808080;
            } else {
                bgColor = 0xFF404040;
            }
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

            if (this.isSelected()) {
                Font font = Minecraft.getInstance().font;
                int underlineColor = this.active ? -1 : -6250336;
                int textWidth = Math.min(font.width(this.getMessage()), this.getWidth() - 4);
                int left = this.getX() + (this.getWidth() - textWidth) / 2;
                int top = this.getY() + this.getHeight() - 2;
                graphics.fill(left, top, left + textWidth, top + 1, underlineColor);
            }

            renderLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            this.handleCursor(graphics);
        }

        private void renderLabel(ActiveTextCollector output) {
            int left = this.getX() + 1;
            int top = this.getY() + (this.isSelected() ? 0 : 1);
            int right = this.getX() + this.getWidth() - 1;
            int bottom = this.getY() + this.getHeight();
            output.acceptScrollingWithDefaultCenter(this.getMessage(), left, right, top, bottom);
        }
    }

}
