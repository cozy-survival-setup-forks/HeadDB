package io.github.silentdevelopment.headdb.paper.gui.common;

import io.github.silentdevelopment.grafik.gui.GuiContext;
import io.github.silentdevelopment.grafik.gui.GuiSession;
import io.github.silentdevelopment.grafik.page.Page;
import io.github.silentdevelopment.grafik.paper.element.PaperElement;
import io.github.silentdevelopment.grafik.paper.page.PaperPage;
import io.github.silentdevelopment.grafik.paper.page.PaperPageRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Renders a page without ever leaving the inventory half drawn.
 * <p>
 * Every item is built first and the inventory is only touched afterwards, so an element that throws can no longer
 * leave an open menu cleared (the "empty menu" case). A failing element is replaced by a marker item and logged.
 */
public final class SafePageRenderer implements PaperPageRenderer {

    private final Logger logger;
    private long lastWarning;

    public SafePageRenderer(@NotNull Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void render(@NotNull GuiSession<?> session, @NotNull Inventory inventory) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(inventory, "inventory");
        renderTyped(session, inventory);
    }

    @SuppressWarnings("unchecked")
    private <C> void renderTyped(@NotNull GuiSession<?> rawSession, @NotNull Inventory inventory) {
        GuiSession<C> session = (GuiSession<C>) rawSession;
        Page<C> current = session.currentPage();

        if (!(current instanceof PaperPage<?> rawPage)) {
            throw new IllegalStateException("Current page '" + current.key().value() + "' is not a paper page.");
        }

        PaperPage<C> page = (PaperPage<C>) rawPage;
        GuiContext<C> context = session.context();
        Map<Integer, ItemStack> items = new LinkedHashMap<>();

        for (Map.Entry<Integer, PaperElement<C>> entry : page.slotMap().entrySet()) {
            int slot = entry.getKey();

            if (slot < 0 || slot >= inventory.getSize()) {
                warn("Slot " + slot + " is outside the inventory (" + inventory.getSize() + ") on page '" + page.key().value() + "'.", null);
                continue;
            }

            PaperElement<C> element = entry.getValue();

            try {
                if (!element.isVisible(context)) {
                    continue;
                }

                ItemStack item = element.render(context);

                if (item != null) {
                    items.put(slot, item);
                }
            } catch (RuntimeException exception) {
                warn("Element '" + element.key().value() + "' on page '" + page.key().value() + "' failed to render.", exception);
                items.put(slot, failed());
            }
        }

        inventory.clear();

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    private static @NotNull ItemStack failed() {
        ItemStack item = new ItemStack(Material.BARRIER);
        item.editMeta(meta -> meta.displayName(Component.text("Unavailable", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    private void warn(@NotNull String message, Throwable cause) {
        long now = System.currentTimeMillis();

        // one line every few seconds is enough; the same failure repeats on each redraw
        if (now - lastWarning < 5_000L) {
            return;
        }

        lastWarning = now;

        if (cause == null) {
            logger.warn(message);
        } else {
            logger.warn(message, cause);
        }
    }
}
