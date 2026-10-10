package io.github.silentdevelopment.headdb.paper.gui.common;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class GuiTitles {


    private GuiTitles() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static @NotNull Component title(@NotNull String title, boolean adminMode) {
        Objects.requireNonNull(title, "title");

        // titles are red unless the text sets its own colour; MiniMessage tags and &-codes are allowed in gui.yml texts
        return GuiItems.mini(title.trim()).colorIfAbsent(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    /** Escapes a value so it is shown as typed when it is placed in a title. */
    public static @NotNull String escape(@NotNull String value) {
        Objects.requireNonNull(value, "value");
        return MiniMessage.miniMessage().escapeTags(value);
    }

    public static @NotNull Component title(@NotNull String prefix, @NotNull String detail, boolean adminMode) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(detail, "detail");

        String clean = escape(prefix.trim()) + ": " + escape(shortTitle(detail.trim()));
        return title(clean, adminMode);
    }

    private static @NotNull String shortTitle(@NotNull String value) {
        if (value.length() <= 16) {
            return value;
        }

        return value.substring(0, 13) + "...";
    }
}
