package io.github.silentdevelopment.headdb.paper.gui.common;

import io.github.silentdevelopment.headdb.model.HeadCategory;
import io.github.silentdevelopment.headdb.paper.HeadDBPlugin;
import io.github.silentdevelopment.headdb.paper.gui.config.GuiIconConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/**
 * One place that decides what a category is called in a menu.
 * <p>
 * The name set on the category's button in gui.yml ({@code icons.category.<id>.name}, or the shared
 * {@code icons.category.name} template) wins over the name from the catalog, and the same name is used on the button,
 * in menu titles and in labels, so a rename shows up everywhere.
 */
public final class CategoryNames {

    private static final String TEMPLATE_KEY = "category";

    private CategoryNames() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static @NotNull String iconKey(@NotNull String categoryId) {
        Objects.requireNonNull(categoryId, "categoryId");
        return TEMPLATE_KEY + "." + categoryId.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
    }

    /** Replaces %name%, %id% and %count%; the values are escaped so a name can never be read as MiniMessage. */
    public static @NotNull String fill(@NotNull String template, @NotNull HeadCategory category, int amount) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(category, "category");

        MiniMessage mini = MiniMessage.miniMessage();
        return template
                .replace("%name%", mini.escapeTags(category.name()))
                .replace("%id%", mini.escapeTags(category.id()))
                .replace("%count%", String.valueOf(amount));
    }

    /** The name for the category's button. */
    public static @NotNull Component item(@NotNull HeadDBPlugin plugin, @NotNull HeadCategory category, int amount) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(category, "category");

        String template = template(plugin, category);

        if (template == null) {
            return GuiItems.name(category.name(), NamedTextColor.GOLD);
        }

        return GuiItems.mini(fill(template, category, amount));
    }

    /** The name as plain text, for titles and chat. */
    public static @NotNull String plain(@NotNull HeadDBPlugin plugin, @NotNull HeadCategory category) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(category, "category");

        String template = template(plugin, category);

        if (template == null) {
            return category.name();
        }

        int amount = plugin.headRegistry().categoryCounts(false).getOrDefault(category.id(), 0);
        String plain = PlainTextComponentSerializer.plainText().serialize(GuiItems.mini(fill(template, category, amount))).trim();
        return plain.isEmpty() ? category.name() : plain;
    }

    /** Same as {@link #plain(HeadDBPlugin, HeadCategory)} for an id; an unknown id is returned as it is. */
    public static @NotNull String plain(@NotNull HeadDBPlugin plugin, @NotNull String categoryId) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(categoryId, "categoryId");

        return plugin.headRegistry().categories().stream()
                .filter(category -> category.id().equalsIgnoreCase(categoryId))
                .findFirst()
                .map(category -> plain(plugin, category))
                .orElse(categoryId);
    }

    /**
     * The name template that applies to the category, or null when the catalog name should be used as it is: the
     * category has no icon of its own and the shared template has no %name% in it.
     */
    private static String template(@NotNull HeadDBPlugin plugin, @NotNull HeadCategory category) {
        String key = iconKey(category.id());
        boolean own = plugin.guiConfig().hasIcon(key);
        GuiIconConfig icon = own ? plugin.guiConfig().icon(key) : plugin.guiConfig().icon(TEMPLATE_KEY);
        String name = icon.name();

        if (name == null || name.isBlank()) {
            return null;
        }

        if (!own && !name.contains("%name%")) {
            return null;
        }

        return name;
    }
}
